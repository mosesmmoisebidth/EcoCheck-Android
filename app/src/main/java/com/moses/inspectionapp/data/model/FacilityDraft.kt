package com.moses.inspectionapp.data.model

data class FacilityDraft(
    val name: String = "",
    val tin: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val ownerEmail: String = "",
    val district: String = "",
    val sector: String = "",
    val cell: String = "",
    val village: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photoPath: String? = null,
)
