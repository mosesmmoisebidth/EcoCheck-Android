package com.moses.inspectionapp.data.remote

class UserManagementRepository(
    private val api: InspectionApi = ApiClient.api,
) {
    suspend fun getUsers(): Result<List<ManagedUser>> {
        return runCatching {
            val response = api.getUsers()
            if (!response.success) {
                throw IllegalStateException(response.message.ifBlank { "Unable to load users" })
            }
            response.payload.map { it.toManagedUser() }
        }
    }

    suspend fun createUser(request: CreateUserManagementRequest): Result<ManagedUser> {
        return runCatching {
            val response = api.createUser(request)
            if (!response.success) {
                throw IllegalStateException(response.message.ifBlank { "Unable to create user" })
            }
            response.payload.toManagedUser()
        }
    }

    suspend fun resendActivation(userId: String): Result<ManagedUser> {
        return runCatching {
            val response = api.resendActivation(userId)
            if (!response.success) {
                throw IllegalStateException(response.message.ifBlank { "Unable to resend activation" })
            }
            response.payload.toManagedUser()
        }
    }

    suspend fun deactivateUser(userId: String): Result<ManagedUser> {
        return runCatching {
            val response = api.deactivateUser(userId)
            if (!response.success) {
                throw IllegalStateException(response.message.ifBlank { "Unable to deactivate user" })
            }
            response.payload.toManagedUser()
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return runCatching {
            val response = api.deleteUser(userId)
            if (!response.success) {
                throw IllegalStateException(response.message.ifBlank { "Unable to delete user" })
            }
            Unit
        }
    }
}

data class ManagedUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val district: String,
    val sector: String,
    val isActive: Boolean,
    val activationStatus: String?,
    val activationCode: String?,
    val activationExpiresAt: Long?,
)

private fun UserManagementResponse.toManagedUser(): ManagedUser {
    return ManagedUser(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        role = role,
        district = district,
        sector = sector,
        isActive = isActive,
        activationStatus = activationStatus,
        activationCode = activationCode,
        activationExpiresAt = activationExpiresAt,
    )
}
