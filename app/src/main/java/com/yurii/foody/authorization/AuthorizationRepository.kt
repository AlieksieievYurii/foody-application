package com.yurii.foody.authorization

import com.haroldadmin.cnradapter.NetworkResponse
import com.yurii.foody.api.ApiTokenAuth
import com.yurii.foody.api.AuthData
import com.yurii.foody.api.AuthResponseData
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.toAuthDataStorage

class AuthorizationRepository private constructor(
    private val authDataStorage: AuthDataStorage,
    private val apiTokenAuth: ApiTokenAuth
) {
    suspend fun logIn(authData: AuthData): NetworkResponse<AuthResponseData, Unit> {
        val response = apiTokenAuth.logIn(authData)

        if (response is NetworkResponse.Success)
            saveAuthCredentials(response.body)

        return response
    }

    private suspend fun saveAuthCredentials(authResponseData: AuthResponseData) = authDataStorage.save(authResponseData.toAuthDataStorage())

    companion object {
        private var INSTANCE: AuthorizationRepository? = null
        fun create(authDataStorage: AuthDataStorage, apiTokenAuth: ApiTokenAuth): AuthorizationRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = AuthorizationRepository(authDataStorage, apiTokenAuth) }

            return INSTANCE!!
        }
    }
}