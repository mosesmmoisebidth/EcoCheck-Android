package com.moses.inspectionapp.data.model

data class Fault(
    val id: String,
    val inspectionTypeId: String,
    val name: String,
    val standardFine: Int,
    val active: Boolean,
)
