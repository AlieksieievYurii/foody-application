package com.yurii.foody.utils

import com.yurii.foody.api.*
import kotlinx.coroutines.flow.first
import java.lang.IllegalStateException

class AuthorizationRepository(
    private val authDataStorage: AuthDataStorageInterface,
    private val unAuthorizedApi: Service
) {
    private var authorizedApi: Service? = null
        get() {
            if (field == null)
                throw IllegalStateException("Authorized api service is not created. You must call logIn first")
            return field
        }

    suspend fun register(user: RegistrationForm) = Service.wrapWithResponseException { unAuthorizedApi.authService.registerUser(user) }

    suspend fun logIn(authData: AuthData): AuthResponseData {
        val result = Service.wrapWithResponseException { unAuthorizedApi.authService.logIn(authData) }
        authDataStorage.saveAuthData(result.toAuthDataStorage())
        createAuthApiService(result.token)
        return result
    }

    suspend fun logOut() = authDataStorage.cleanAllAuthData()

    fun createAuthApiService(token: String) {
        authorizedApi = Service(token)
    }

    suspend fun getUser(id: Long) = Service.wrapWithResponseException { authorizedApi!!.usersService.getUser(id) }

    suspend fun saveUser(user: User?) = authDataStorage.saveUser(user)

    suspend fun getUserRole() = authDataStorage.userRole.first()

    suspend fun getCurrentUserRole(): UserRole {
        val userId = getAuthenticationData()?.userId ?: throw IllegalStateException("No saved user")
        return Service.wrapWithResponseException { authorizedApi!!.usersService.getUsersRoles(userId = userId).results.first() }
    }

    suspend fun getAuthenticationData() = authDataStorage.authData.first()

    suspend fun setUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

    suspend fun setUserRoleStatus(isConfirmed: Boolean) = authDataStorage.setUserRoleStatus(isConfirmed)

    suspend fun getSelectedUserRole() = authDataStorage.selectedRole.first()

    suspend fun setSelectedUserRole(userRole: UserRoleEnum?) = authDataStorage.saveSelectedUserRole(userRole)

    suspend fun isUserRoleConfirmed() = authDataStorage.isRoleConfirmed.first()
}