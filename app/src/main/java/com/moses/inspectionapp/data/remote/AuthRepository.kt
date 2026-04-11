package com.moses.inspectionapp.data.remote

import com.moses.inspectionapp.data.model.UserProfile
import com.moses.inspectionapp.data.store.AppPreferences
import com.moses.inspectionapp.data.store.UserSessionStore
import retrofit2.HttpException

class AuthRepository(
    private val authApi: AuthApi = ApiClient.auth,
) {
    suspend fun login(email: String, password: String): Result<UserProfile> {
        return try {
            val response = authApi.login(AuthLoginRequest(email = email, password = password))
            if (!response.success) {
                Result.failure(IllegalStateException(response.message))
            } else {
                val payload = response.payload
                AppPreferences.accessToken = payload.accessToken
                AppPreferences.refreshToken = payload.refreshToken
                AppPreferences.accessTokenExpiry =
                    System.currentTimeMillis() + payload.expiresIn * 1000
                val profile = UserProfile(
                    id = payload.user.id,
                    fullName = payload.user.fullName,
                    email = payload.user.email,
                    role = payload.user.role,
                    district = payload.user.district,
                    sector = payload.user.sector,
                )
                UserSessionStore.update(profile)
                Result.success(profile)
            }
        } catch (exception: Exception) {
            val message = readErrorMessage(exception) ?: exception.message
            Result.failure(IllegalStateException(message))
        }
    }

    suspend fun activateAccount(
        identifier: String,
        activationCode: String,
        password: String,
    ): Result<UserProfile> {
        return try {
            val response = authApi.activate(
                AuthActivateRequest(
                    identifier = identifier,
                    activationCode = activationCode,
                    password = password,
                ),
            )
            if (!response.success) {
                Result.failure(IllegalStateException(response.message))
            } else {
                val payload = response.payload
                AppPreferences.accessToken = payload.accessToken
                AppPreferences.refreshToken = payload.refreshToken
                AppPreferences.accessTokenExpiry =
                    System.currentTimeMillis() + payload.expiresIn * 1000
                val profile = UserProfile(
                    id = payload.user.id,
                    fullName = payload.user.fullName,
                    email = payload.user.email,
                    role = payload.user.role,
                    district = payload.user.district,
                    sector = payload.user.sector,
                )
                UserSessionStore.update(profile)
                Result.success(profile)
            }
        } catch (exception: Exception) {
            val message = readErrorMessage(exception) ?: exception.message
            Result.failure(IllegalStateException(message))
        }
    }

    suspend fun verifyActivation(identifier: String, activationCode: String): Result<Unit> {
        return try {
            val response = authApi.verifyActivation(
                AuthActivateVerifyRequest(
                    identifier = identifier,
                    activationCode = activationCode,
                ),
            )
            if (!response.success || response.payload.valid.not()) {
                Result.failure(IllegalStateException(response.message))
            } else {
                Result.success(Unit)
            }
        } catch (exception: Exception) {
            val message = readErrorMessage(exception) ?: exception.message
            Result.failure(IllegalStateException(message))
        }
    }

    suspend fun updateProfile(firstName: String, lastName: String): Result<UserProfile> {
        return try {
            val response = authApi.updateProfile(
                ProfileUpdateRequest(firstName = firstName, lastName = lastName),
            )
            if (!response.success) {
                Result.failure(IllegalStateException(response.message))
            } else {
                val payload = response.payload
                val profile = UserProfile(
                    id = payload.id,
                    fullName = payload.fullName,
                    email = payload.email,
                    role = payload.role,
                    district = payload.district,
                    sector = payload.sector,
                )
                UserSessionStore.update(profile)
                Result.success(profile)
            }
        } catch (exception: Exception) {
            val message = readErrorMessage(exception) ?: exception.message
            Result.failure(IllegalStateException(message))
        }
    }

    suspend fun changeEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val response = authApi.changeEmail(
                AuthChangeEmailRequest(email = email, password = password),
            )
            if (!response.success) {
                Result.failure(IllegalStateException(response.message))
            } else {
                val payload = response.payload
                val profile = UserProfile(
                    id = payload.id,
                    fullName = payload.fullName,
                    email = payload.email,
                    role = payload.role,
                    district = payload.district,
                    sector = payload.sector,
                )
                UserSessionStore.update(profile)
                Result.success(profile)
            }
        } catch (exception: Exception) {
            val message = readErrorMessage(exception) ?: exception.message
            Result.failure(IllegalStateException(message))
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = authApi.changePassword(
                AuthChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                ),
            )
            if (!response.success) {
                Result.failure(IllegalStateException(response.message))
            } else {
                Result.success(Unit)
            }
        } catch (exception: Exception) {
            val message = readErrorMessage(exception) ?: exception.message
            Result.failure(IllegalStateException(message))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = AppPreferences.refreshToken
            if (!refreshToken.isNullOrBlank()) {
                authApi.logout(AuthRefreshRequest(refreshToken))
            }
            UserSessionStore.clear()
            Result.success(Unit)
        } catch (exception: Exception) {
            UserSessionStore.clear()
            Result.success(Unit)
        }
    }

    private fun readErrorMessage(exception: Exception): String? {
        if (exception !is HttpException) {
            return null
        }
        val rawBody = exception.response()?.errorBody()?.string() ?: return null
        val match = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(rawBody)
        return match?.groupValues?.getOrNull(1)
    }
}
