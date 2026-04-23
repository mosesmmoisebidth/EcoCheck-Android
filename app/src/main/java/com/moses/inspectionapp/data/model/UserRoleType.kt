package com.moses.inspectionapp.data.model

enum class UserRoleType {
    HSO,
    DISTRICT_MANAGER,
    CITY_MANAGER,
    OTHER,
}

fun parseUserRole(role: String?): UserRoleType {
    return when (role?.trim()?.uppercase()) {
        "HSO" -> UserRoleType.HSO
        "DISTRICT_MANAGER" -> UserRoleType.DISTRICT_MANAGER
        "CITY_MANAGER" -> UserRoleType.CITY_MANAGER
        else -> UserRoleType.OTHER
    }
}

fun UserRoleType.isManager(): Boolean {
    return this == UserRoleType.DISTRICT_MANAGER || this == UserRoleType.CITY_MANAGER
}

fun UserRoleType.displayLabel(): String {
    return when (this) {
        UserRoleType.HSO -> "HSO"
        UserRoleType.DISTRICT_MANAGER -> "District Manager"
        UserRoleType.CITY_MANAGER -> "City Manager"
        UserRoleType.OTHER -> "Inspector"
    }
}
