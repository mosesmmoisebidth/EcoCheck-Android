package com.moses.inspectionapp.data.validator

import android.util.Patterns
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

object InputValidators {
    private val tinRegex = Regex("^\\d{9}$")
    private val fullNameRegex = Regex("^[\\p{L}][\\p{L} .'-]{1,}$")
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    fun normalizeTin(value: String): String {
        return value.filter { it.isDigit() }.take(9)
    }

    fun normalizePhone(value: String): String {
        return value.trim().replace(Regex("\\s+"), "")
    }

    fun normalizeName(value: String): String {
        return value
            .filter { char ->
                char.isLetter() || char == ' ' || char == '\'' || char == '-' || char == '.'
            }
            .replace(Regex("\\s+"), " ")
            .trimStart()
    }

    fun normalizeEmail(value: String): String {
        return value.trim()
    }

    fun isValidTin(value: String): Boolean {
        return tinRegex.matches(normalizeTin(value))
    }

    fun isValidName(value: String): Boolean {
        val normalized = normalizeName(value).trim()
        if (normalized.length < 3) return false
        return fullNameRegex.matches(normalized)
    }

    fun isValidEmail(value: String): Boolean {
        val normalized = normalizeEmail(value)
        return normalized.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(normalized).matches()
    }

    fun isValidInternationalPhone(value: String): Boolean {
        val normalized = normalizePhone(value)
        if (normalized.isBlank()) return false
        return try {
            val parsed = phoneUtil.parse(
                normalized,
                if (normalized.startsWith("+")) null else Locale.getDefault().country,
            )
            phoneUtil.isValidNumber(parsed)
        } catch (_: Exception) {
            false
        }
    }

    fun toE164(value: String, fallbackRegionIso: String = Locale.getDefault().country): String {
        val normalized = normalizePhone(value)
        if (normalized.isBlank()) return ""
        return try {
            val parsed = phoneUtil.parse(
                normalized,
                if (normalized.startsWith("+")) null else fallbackRegionIso,
            )
            phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (_: Exception) {
            normalized
        }
    }
}
