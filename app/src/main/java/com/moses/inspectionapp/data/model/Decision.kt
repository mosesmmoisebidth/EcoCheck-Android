package com.moses.inspectionapp.data.model

enum class Decision {
    WARNING,
    CLOSURE_IMMEDIATE,
    CLOSURE_DEADLINE,
    PROSECUTION_RECOMMENDED,
    NO_ACTION,
}

fun decisionOptionsInUiOrder(): List<Decision> {
    return listOf(
        Decision.NO_ACTION,
        Decision.WARNING,
        Decision.CLOSURE_DEADLINE,
        Decision.CLOSURE_IMMEDIATE,
        Decision.PROSECUTION_RECOMMENDED,
    )
}

fun parseDecision(raw: String?): Decision {
    return when (raw?.trim()?.uppercase()) {
        "NO_ACTION", "COMPLIANT" -> Decision.NO_ACTION
        "WARNING" -> Decision.WARNING
        "CLOSURE_DEADLINE", "TEMPORARY_CLOSURE" -> Decision.CLOSURE_DEADLINE
        "CLOSURE_IMMEDIATE", "PERMANENT_CLOSURE" -> Decision.CLOSURE_IMMEDIATE
        "PROSECUTION_RECOMMENDED", "PROSECUTION", "FINE" -> Decision.PROSECUTION_RECOMMENDED
        else -> Decision.WARNING
    }
}

fun Decision.toReportLabel(): String {
    return when (this) {
        Decision.NO_ACTION -> "Compliant"
        Decision.WARNING -> "Warning"
        Decision.CLOSURE_DEADLINE -> "Temporary Closure"
        Decision.CLOSURE_IMMEDIATE -> "Permanent Closure"
        Decision.PROSECUTION_RECOMMENDED -> "Fine"
    }
}
