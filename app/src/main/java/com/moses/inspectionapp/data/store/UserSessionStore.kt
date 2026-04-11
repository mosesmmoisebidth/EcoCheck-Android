package com.moses.inspectionapp.data.store

import com.moses.inspectionapp.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object UserSessionStore {
    private val defaultProfile = UserProfile(
        id = "local",
        fullName = "Inspector",
        email = "inspector@local",
        role = "HSO",
        district = "Kigali",
        sector = "Kacyiru",
    )
    private val _profile = MutableStateFlow(defaultProfile)
    val profile: StateFlow<UserProfile> = _profile

    fun loadFromPrefs() {
        val id = AppPreferences.userId
        val name = AppPreferences.userName
        val email = AppPreferences.userEmail
        val role = AppPreferences.userRole
        val district = AppPreferences.userDistrict
        val sector = AppPreferences.userSector
        if (!id.isNullOrBlank() && !name.isNullOrBlank() && !email.isNullOrBlank() && !role.isNullOrBlank() &&
            !district.isNullOrBlank() && !sector.isNullOrBlank()
        ) {
            _profile.value = UserProfile(
                id = id,
                fullName = name,
                email = email,
                role = role,
                district = district,
                sector = sector,
            )
            AppPreferences.hasSession = true
        } else {
            _profile.value = defaultProfile
        }
    }

    fun update(profile: UserProfile) {
        AppPreferences.userId = profile.id
        AppPreferences.userName = profile.fullName
        AppPreferences.userEmail = profile.email
        AppPreferences.userRole = profile.role
        AppPreferences.userDistrict = profile.district
        AppPreferences.userSector = profile.sector
        AppPreferences.hasSession = true
        _profile.value = profile
    }

    fun clear() {
        AppPreferences.clearSession()
        _profile.value = defaultProfile
    }
}
