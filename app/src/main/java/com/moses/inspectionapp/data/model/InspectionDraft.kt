package com.moses.inspectionapp.data.model

data class InspectionDraft(
    val id: String? = null,
    val facilityId: String? = null,
    val facilityName: String = "",
    val visitType: VisitType? = null,
    val inspectionTypeId: String? = null,
    val teamMembers: List<String> = emptyList(),
    val selectedFaultIds: Set<String> = emptySet(),
    val adjustmentAmount: Int = 0,
    val adjustmentReason: String = "",
    val decision: Decision? = null,
    val comments: String = "",
    val recommendations: String = "",
    val photoPaths: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
)

fun InspectionDraft.totalFine(faults: List<Fault>): Int {
    val faultsTotal = faults.filter { selectedFaultIds.contains(it.id) }.sumOf { it.standardFine }
    return faultsTotal + adjustmentAmount
}
