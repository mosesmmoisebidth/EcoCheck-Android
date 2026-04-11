package com.moses.inspectionapp.data.model

enum class VisitType {
    FIRST_VISIT,
    WARNING_VISIT,
    FOLLOW_UP,
    COMPLIANCE_CHECK;

    fun toApiValue(): String {
        return when (this) {
            FIRST_VISIT -> "FIRST"
            WARNING_VISIT -> "WARNING"
            FOLLOW_UP -> "FOLLOW_UP"
            COMPLIANCE_CHECK -> "COMPLIANCE"
        }
    }

    companion object {
        fun fromApi(raw: String?): VisitType {
            val value = raw?.trim()?.uppercase() ?: return FIRST_VISIT
            return when (value) {
                "FIRST", "FIRST_VISIT" -> FIRST_VISIT
                "WARNING", "WARNING_VISIT" -> WARNING_VISIT
                "FOLLOW_UP" -> FOLLOW_UP
                "COMPLIANCE", "COMPLIANCE_CHECK" -> COMPLIANCE_CHECK
                else -> FIRST_VISIT
            }
        }
    }
}
