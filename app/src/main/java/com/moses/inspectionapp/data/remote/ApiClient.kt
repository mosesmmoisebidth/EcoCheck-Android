package com.moses.inspectionapp.data.remote

import com.moses.inspectionapp.BuildConfig
import com.moses.inspectionapp.data.store.AppPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.os.Build
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authOkHttp = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val lock = Any()
    @Volatile private var currentBaseUrl: String? = null
    @Volatile private var authApi: AuthApi? = null
    @Volatile private var apiService: InspectionApi? = null

    private val apiOkHttp = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator { auth })
        .build()

    val auth: AuthApi
        get() = synchronized(lock) {
            val baseUrl = resolvedBaseUrl()
            if (authApi == null || currentBaseUrl != baseUrl) {
                currentBaseUrl = baseUrl
                authApi = buildAuthApi(baseUrl)
            }
            authApi!!
        }

    val api: InspectionApi
        get() = synchronized(lock) {
            val baseUrl = resolvedBaseUrl()
            if (apiService == null || currentBaseUrl != baseUrl) {
                currentBaseUrl = baseUrl
                apiService = buildApi(baseUrl)
            }
            apiService!!
        }

    fun updateBaseUrl(raw: String) {
        val normalized = normalizeBaseUrl(raw)
        AppPreferences.apiBaseUrl = normalized
        synchronized(lock) {
            currentBaseUrl = normalized
            authApi = null
            apiService = null
        }
    }

    fun normalizeBaseUrl(raw: String): String {
        var url = raw.trim()
        if (url.isBlank()) {
            url = BuildConfig.API_BASE_URL
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) {
            url += "/"
        }
        if (!url.contains("/api/")) {
            url = "${url}api/"
        }
        return url
    }

    private fun resolvedBaseUrl(): String {
        val raw = AppPreferences.apiBaseUrl ?: BuildConfig.API_BASE_URL
        val normalized = normalizeBaseUrl(raw)
        return adjustForRuntime(normalized)
    }

    private fun buildAuthApi(baseUrl: String): AuthApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(authOkHttp)
            .build()
        return retrofit.create(AuthApi::class.java)
    }

    private fun buildApi(baseUrl: String): InspectionApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(apiOkHttp)
            .build()
        return retrofit.create(InspectionApi::class.java)
    }

    private fun adjustForRuntime(url: String): String {
        val emulator = isEmulator()
        return when {
            emulator && (url.contains("127.0.0.1") || url.contains("localhost")) -> {
                url.replace("127.0.0.1", "10.0.2.2").replace("localhost", "10.0.2.2")
            }
            !emulator && url.contains("10.0.2.2") -> url.replace("10.0.2.2", "127.0.0.1")
            !emulator && url.contains("localhost") -> url.replace("localhost", "127.0.0.1")
            else -> url
        }
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk", ignoreCase = true) ||
            Build.MODEL.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
            Build.PRODUCT.contains("sdk", ignoreCase = true)
    }
}

private class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = AppPreferences.accessToken
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

private class TokenAuthenticator(
    private val authProvider: () -> AuthApi,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null
        }
        val refreshToken = AppPreferences.refreshToken ?: return null
        return runBlocking {
            try {
                val refreshResponse = authProvider().refresh(AuthRefreshRequest(refreshToken))
                if (!refreshResponse.success) {
                    return@runBlocking null
                }
                val payload = refreshResponse.payload
                AppPreferences.accessToken = payload.accessToken
                AppPreferences.refreshToken = payload.refreshToken
                AppPreferences.accessTokenExpiry = System.currentTimeMillis() + payload.expiresIn * 1000
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${payload.accessToken}")
                    .build()
            } catch (exception: Exception) {
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count += 1
            prior = prior.priorResponse
        }
        return count
    }
}
