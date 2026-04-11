package com.moses.inspectionapp.data.validator

object InputValidators {
    private val tinRegex = Regex("^\\d{9}$")
    private val rwandaPhoneRegex = Regex("^(\\+2507|07)\\d{8}$")

    fun normalizeTin(value: String): String {
        return value.trim()
    }

    fun normalizePhone(value: String): String {
        return value.replace(" ", "").trim()
    }

    fun isValidTin(value: String): Boolean {
        return tinRegex.matches(normalizeTin(value))
    }

    fun isValidRwandaPhone(value: String): Boolean {
        return rwandaPhoneRegex.matches(normalizePhone(value))
    }
}
