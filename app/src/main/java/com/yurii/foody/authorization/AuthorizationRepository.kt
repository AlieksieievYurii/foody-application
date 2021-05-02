package com.yurii.foody.authorization

import com.yurii.foody.api.*
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.toAuthDataStorage
import kotlinx.coroutines.flow.*

class AuthorizationRepository private constructor(
    private val authDataStorage: AuthDataStorage,
    private val api: Service
) {
    suspend fun logIn(authData: AuthData): Flow<AuthResponseData> =
        Service.asFlow { api.authService.logIn(authData) }.map {
            saveAuthCredentials(it)
            api.createAuthenticatedService(it.token)
            it
        }

    fun setToken(token: String) = api.createAuthenticatedService(token)

    suspend fun clearUserAuth() = authDataStorage.clearUserAuth()

    suspend fun getUser(id: Long) = Service.asFlow { api.usersService.getUser(id) }

    suspend fun getUsersRoles(userId: Int? = null) = Service.asFlow { api.usersService.getUsersRoles(userId) }

    fun getLogInAuthenticatedDataFlow() = authDataStorage.authData

    suspend fun getSelectedUserRole() = authDataStorage.selectedRole.first()

    suspend fun getUserRole(): UserRoleEnum? = authDataStorage.userRole.first()

    suspend fun isUserRoleConfirmed() = authDataStorage.isRoleConfirmed.first()

    suspend fun saveUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

    suspend fun saveSelectedUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveSelectedUserRole(userRoleEnum)

    suspend fun clearCurrentSelectedUserRole() = authDataStorage.clearCurrentSelectedUserRole()

    suspend fun setUserRoleStatus(isConfirmed: Boolean) = authDataStorage.seUserRoleStatus(isConfirmed)

    private suspend fun saveAuthCredentials(authResponseData: AuthResponseData) = authDataStorage.save(authResponseData.toAuthDataStorage())

    companion object {
        private var INSTANCE: AuthorizationRepository? = null
        fun create(authDataStorage: AuthDataStorage, api: Service): AuthorizationRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = AuthorizationRepository(authDataStorage, api) }

            return INSTANCE!!
        }
    }
}