package com.moses.inspectionapp.data.store

import com.moses.inspectionapp.data.model.GeoPoint
import com.moses.inspectionapp.data.model.InspectionDraft
import kotlinx.coroutines.flow.MutableStateFlow

enum class PhotoCaptureTarget {
    FACILITY,
    INSPECTION,
}

data class ReportFiltersPrefill(
    val district: String? = null,
    val sector: String? = null,
    val cell: String? = null,
    val village: String? = null,
)

object DraftStore {
    val facilityPhotoPath = MutableStateFlow<String?>(null)
    val facilityLocation = MutableStateFlow<GeoPoint?>(null)
    val selectedFacilityId = MutableStateFlow<String?>(null)
    val selectedInspectionId = MutableStateFlow<String?>(null)
    val inspectionPhotoPaths = MutableStateFlow<List<String>>(emptyList())
    val photoCaptureTarget = MutableStateFlow(PhotoCaptureTarget.FACILITY)
    val inspectionDraft = MutableStateFlow(InspectionDraft())
    val reportFiltersPrefill = MutableStateFlow<ReportFiltersPrefill?>(null)

    fun resetInspectionDraft() {
        inspectionDraft.value = InspectionDraft()
        inspectionPhotoPaths.value = emptyList()
    }
}
