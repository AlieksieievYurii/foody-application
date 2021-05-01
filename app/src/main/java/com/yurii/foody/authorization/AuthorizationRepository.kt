package com.yurii.foody.authorization

import com.haroldadmin.cnradapter.NetworkResponse
import com.yurii.foody.api.*
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.toAuthDataStorage

class AuthorizationRepository private constructor(
    private val authDataStorage: AuthDataStorage,
    private val api: Service
) {
    suspend fun logIn(authData: AuthData): NetworkResponse<AuthResponseData, Unit> {
        val response = api.authService.logIn(authData)

        if (response is NetworkResponse.Success)
            saveAuthCredentials(response.body)

        return response
    }

    fun setToken(token: String) = api.createAuthenticatedService(token)

    suspend fun clearUserAuth() = authDataStorage.clearUserAuth()

    suspend fun getUser(id: Long) = api.usersService.getUser(id)

    suspend fun getUsersRoles(userId: Int? = null) = api.usersService.getUsersRoles(userId)

    fun getLogInAuthenticatedDataFlow() = authDataStorage.authData

    fun getSelectedUserRoleFlow() = authDataStorage.selectedRole

    suspend fun saveUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

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