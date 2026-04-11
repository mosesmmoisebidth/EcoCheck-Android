package com.moses.inspectionapp.data.remote

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val payload: T,
    val path: String,
    val method: String,
    val timestamp: Long,
)

data class AuthLoginRequest(
    val email: String,
    val password: String,
)

data class AuthActivateRequest(
    val identifier: String,
    val activationCode: String,
    val password: String,
)

data class AuthActivateVerifyRequest(
    val identifier: String,
    val activationCode: String,
)

data class ActivationVerifyResponse(
    val valid: Boolean,
    val expiresAt: Long?,
)

data class AuthRefreshRequest(
    val refreshToken: String,
)

data class AuthChangeEmailRequest(
    val email: String,
    val password: String,
)

data class AuthChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class AuthUserResponse(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
    val district: String,
    val sector: String,
)

data class ProfileUpdateRequest(
    val firstName: String,
    val lastName: String,
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: AuthUserResponse,
)

data class FacilityResponse(
    val id: String,
    val name: String,
    val tin: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String?,
    val district: String,
    val sector: String,
    val cell: String,
    val village: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val syncStatus: String,
)

data class InspectionResponse(
    val id: String,
    val facilityId: String,
    val facilityName: String,
    val visitType: String,
    val teamMembers: List<String>,
    val inspectionTypeId: String? = null,
    val faultCount: Int,
    val totalFine: Int,
    val adjustmentAmount: Int,
    val adjustmentReason: String,
    val decision: String,
    val comments: String,
    val recommendations: String,
    val photoPaths: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val syncStatus: String,
)

data class CreateFacilityRequest(
    val name: String,
    val tin: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String?,
    val district: String,
    val sector: String,
    val cell: String,
    val village: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
)

data class UpdateFacilityRequest(
    val name: String?,
    val ownerName: String?,
    val ownerPhone: String?,
    val ownerEmail: String?,
    val cell: String?,
    val village: String?,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
)

data class CreateInspectionRequest(
    val facilityId: String,
    val visitType: String,
    val inspectionTypeId: String?,
    val teamMembers: List<String>,
    val selectedFaultIds: List<String>,
    val adjustmentAmount: Int,
    val adjustmentReason: String?,
    val decision: String,
    val comments: String?,
    val recommendations: String?,
    val photoPaths: List<String>?,
)

data class UpdateInspectionRequest(
    val visitType: String?,
    val inspectionTypeId: String?,
    val teamMembers: List<String>?,
    val selectedFaultIds: List<String>?,
    val adjustmentAmount: Int?,
    val adjustmentReason: String?,
    val decision: String?,
    val comments: String?,
    val recommendations: String?,
    val photoPaths: List<String>?,
)

data class FaultResponse(
    val id: String,
    val inspectionTypeId: String,
    val name: String,
    val standardFine: Int,
    val active: Boolean,
)

data class InspectionTypeResponse(
    val id: String,
    val code: String,
    val name: String,
    val active: Boolean,
)

data class SyncFacilityPayload(
    val clientId: String,
    val serverId: String?,
    val name: String,
    val tin: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerEmail: String?,
    val district: String,
    val sector: String,
    val cell: String,
    val village: String,
    val latitude: Double?,
    val longitude: Double?,
    val photoPath: String?,
    val createdAt: Long?,
    val updatedAt: Long?,
)

data class SyncInspectionPayload(
    val clientId: String,
    val serverId: String?,
    val facilityId: String?,
    val facilityClientId: String?,
    val visitType: String,
    val teamMembers: List<String>,
    val selectedFaultIds: List<String>,
    val inspectionTypeId: String? = null,
    val adjustmentAmount: Int,
    val adjustmentReason: String?,
    val decision: String,
    val comments: String?,
    val recommendations: String?,
    val photoPaths: List<String>?,
    val createdAt: Long?,
    val updatedAt: Long?,
)

data class SyncPushRequest(
    val facilities: List<SyncFacilityPayload>,
    val inspections: List<SyncInspectionPayload>,
    val lastSyncAt: Long?,
)

data class SyncIdMap(
    val clientId: String,
    val serverId: String,
)

data class SyncConflict(
    val clientId: String,
    val serverId: String?,
    val reason: String,
)

data class SyncPushFacilitiesResult(
    val mapped: List<SyncIdMap>,
    val conflicts: List<SyncConflict>,
)

data class SyncPushInspectionsResult(
    val mapped: List<SyncIdMap>?,
    val conflicts: List<SyncConflict>,
)

data class SyncPushResponse(
    val facilities: SyncPushFacilitiesResult,
    val inspections: SyncPushInspectionsResult,
)

data class SyncPullResponse(
    val facilities: List<FacilityResponse>,
    val inspections: List<InspectionResponse>,
)

data class SyncConflictsResponse(
    val facilities: List<FacilityResponse>,
    val inspections: List<InspectionResponse>,
)
