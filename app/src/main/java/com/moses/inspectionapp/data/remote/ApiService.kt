package com.moses.inspectionapp.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): ApiResponse<AuthResponse>

    @POST("auth/activate")
    suspend fun activate(@Body request: AuthActivateRequest): ApiResponse<AuthResponse>

    @POST("auth/activate/verify")
    suspend fun verifyActivation(
        @Body request: AuthActivateVerifyRequest,
    ): ApiResponse<ActivationVerifyResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: AuthRefreshRequest): ApiResponse<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: AuthRefreshRequest): ApiResponse<Map<String, Any>>

    @GET("auth/me")
    suspend fun me(): ApiResponse<AuthUserResponse>

    @PATCH("auth/me")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): ApiResponse<AuthUserResponse>

    @POST("auth/change-email")
    suspend fun changeEmail(@Body request: AuthChangeEmailRequest): ApiResponse<AuthUserResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: AuthChangePasswordRequest): ApiResponse<Map<String, Any>>
}

interface InspectionApi {
    @GET("faults")
    suspend fun getFaults(): ApiResponse<List<FaultResponse>>

    @GET("inspection-types")
    suspend fun getInspectionTypes(): ApiResponse<List<InspectionTypeResponse>>

    @GET("facilities")
    suspend fun getFacilities(): ApiResponse<List<FacilityResponse>>

    @GET("inspections")
    suspend fun getInspections(): ApiResponse<List<InspectionResponse>>

    @POST("facilities")
    suspend fun createFacility(@Body request: CreateFacilityRequest): ApiResponse<FacilityResponse>

    @PATCH("facilities/{id}")
    suspend fun updateFacility(
        @Path("id") id: String,
        @Body request: UpdateFacilityRequest,
    ): ApiResponse<FacilityResponse>

    @POST("inspections")
    suspend fun createInspection(@Body request: CreateInspectionRequest): ApiResponse<InspectionResponse>

    @PATCH("inspections/{id}")
    suspend fun updateInspection(
        @Path("id") id: String,
        @Body request: UpdateInspectionRequest,
    ): ApiResponse<InspectionResponse>

    @POST("sync/push")
    suspend fun syncPush(@Body request: SyncPushRequest): ApiResponse<SyncPushResponse>

    @GET("sync/pull")
    suspend fun syncPull(@Query("since") since: Long? = null): ApiResponse<SyncPullResponse>

    @GET("sync/conflicts")
    suspend fun syncConflicts(): ApiResponse<SyncConflictsResponse>

    @GET("dashboard/stats")
    suspend fun getDashboardStats(): ApiResponse<DashboardStatsResponse>

    @GET("dashboard/charts/inspections-over-time")
    suspend fun getInspectionsOverTime(): ApiResponse<List<DashboardCountPointResponse>>

    @GET("dashboard/charts/compliance-trend")
    suspend fun getComplianceTrend(): ApiResponse<List<DashboardCompliancePointResponse>>

    @GET("dashboard/charts/decisions-breakdown")
    suspend fun getDecisionsBreakdown(): ApiResponse<List<DashboardDecisionBreakdownResponse>>

    @GET("dashboard/top-offenders")
    suspend fun getTopOffenders(
        @Query("limit") limit: Int = 5,
    ): ApiResponse<List<DashboardTopOffenderResponse>>

    @GET("users")
    suspend fun getUsers(): ApiResponse<List<UserManagementResponse>>

    @POST("users")
    suspend fun createUser(
        @Body request: CreateUserManagementRequest,
    ): ApiResponse<UserManagementResponse>

    @POST("users/{id}/resend-activation")
    suspend fun resendActivation(
        @Path("id") id: String,
    ): ApiResponse<UserManagementResponse>

    @PATCH("users/{id}/deactivate")
    suspend fun deactivateUser(
        @Path("id") id: String,
    ): ApiResponse<UserManagementResponse>

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: String,
    ): ApiResponse<Map<String, Any>>
}
