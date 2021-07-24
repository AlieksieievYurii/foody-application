package com.yurii.foody.utils

import androidx.annotation.VisibleForTesting
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.*
import com.yurii.foody.screens.admin.requests.UserRoleRequest
import com.yurii.foody.screens.admin.requests.UserRoleRequestPagingSource
import kotlinx.coroutines.flow.*
import java.lang.IllegalStateException

class AuthorizationRepository @VisibleForTesting constructor(
    private val authDataStorage: AuthDataStorageInterface,
    private val api: Service
) {

    suspend fun logIn(authData: AuthData): AuthResponseData {
        val result = Service.wrapWithResponseException { api.authService.logIn(authData) }
        authDataStorage.saveAuthData(result.toAuthDataStorage())
        api.createAuthenticatedService(result.token)
        return result
    }

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getUnconfirmedUserRolesPager() =
        Pager(config = pagingConfig, pagingSourceFactory = { UserRoleRequestPagingSource(api) }).flow

    suspend fun confirmUserRole(userRoleRequest: UserRoleRequest): UserRole {
        return api.usersService.updateUserRole(id = userRoleRequest.id, userRoleRequest.toConfirmedUserRole())
    }

    suspend fun logOut() {
        authDataStorage.cleanAllAuthData()
        Service.cleanAuthService()
    }

    suspend fun register(user: RegistrationForm) = Service.wrapWithResponseException { api.authService.registerUser(user) }

    fun setToken(token: String) = api.createAuthenticatedService(token)

    suspend fun getUser(id: Long) = Service.wrapWithResponseException { api.usersService.getUser(id) }

    suspend fun getSavedUser(): User? = authDataStorage.currentUser.first()

    suspend fun setSelectedUserRole(userRole: UserRoleEnum?) = authDataStorage.saveSelectedUserRole(userRole)

    suspend fun getCurrentUserRole(): UserRole {
        val userId = getAuthenticationData()?.userId ?: throw IllegalStateException("No saved user")
        return Service.wrapWithResponseException { api.usersService.getUsersRoles(userId = userId).results.first() }
    }

    suspend fun setUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

    suspend fun setUserRoleStatus(isConfirmed: Boolean) = authDataStorage.setUserRoleStatus(isConfirmed)
    suspend fun saveUser(user: User?) = authDataStorage.saveUser(user)

    suspend fun getSelectedUserRole() = authDataStorage.selectedRole.first()

    suspend fun getUserRole() = authDataStorage.userRole.first()

    fun getUserRoleFlow() = authDataStorage.userRole

    suspend fun getAuthenticationData() = authDataStorage.authData.first()

    suspend fun isUserRoleConfirmed() = authDataStorage.isRoleConfirmed.first()

    suspend fun becomeCook() = api.usersService.becomeCook()

    companion object {
        private var INSTANCE: AuthorizationRepository? = null
        fun create(authDataStorage: AuthDataStorage, api: Service): AuthorizationRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = AuthorizationRepository(authDataStorage, api) }

            return INSTANCE!!
        }
    }
}