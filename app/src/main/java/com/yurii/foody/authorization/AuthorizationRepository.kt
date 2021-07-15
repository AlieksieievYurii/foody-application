package com.yurii.foody.authorization

import androidx.annotation.VisibleForTesting
import com.yurii.foody.api.*
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.AuthDataStorageInterface
import com.yurii.foody.utils.toAuthDataStorage
import kotlinx.coroutines.flow.*
import java.lang.IllegalStateException

interface AuthorizationRepositoryInterface {
    suspend fun logIn(authData: AuthData): AuthResponseData
    suspend fun logOut()
    suspend fun register(user: RegistrationForm): Flow<RegistrationForm>
    fun setToken(token: String)
    suspend fun getUser(id: Long): User
    suspend fun getUsersRoles(userId: Long? = null): Flow<Pagination<UserRole>>
    suspend fun getCurrentUserRole(): UserRole

    suspend fun setSelectedUserRole(userRole: UserRoleEnum?)
    suspend fun setUserRole(userRoleEnum: UserRoleEnum)
    suspend fun setUserRoleStatus(isConfirmed: Boolean)
    suspend fun saveUser(user: User?)

    suspend fun getSelectedUserRole(): UserRoleEnum?
    suspend fun getUserRole(): UserRoleEnum?
    suspend fun getAuthenticationData(): AuthDataStorage.Data?
    suspend fun isUserRoleConfirmed(): Boolean
    suspend fun getSavedUser(): User?
}

class AuthorizationRepository @VisibleForTesting constructor(
    private val authDataStorage: AuthDataStorageInterface,
    private val api: ApiServiceInterface
) : AuthorizationRepositoryInterface {

    override suspend fun logIn(authData: AuthData): AuthResponseData {
        val authData = Service.wrapWithResponseException { api.authService.logIn(authData) }
        authDataStorage.saveAuthData(authData.toAuthDataStorage())
        api.createAuthenticatedService(authData.token)
        return authData
    }

    override suspend fun logOut() = authDataStorage.cleanAllAuthData()

    override suspend fun register(user: RegistrationForm) = Service.asFlow { api.authService.registerUser(user) }

    override fun setToken(token: String) = api.createAuthenticatedService(token)

    override suspend fun getUser(id: Long) = Service.wrapWithResponseException { api.usersService.getUser(id) }

    override suspend fun getSavedUser(): User? = authDataStorage.currentUser.first()

    override suspend fun getUsersRoles(userId: Long?) = Service.asFlow { api.usersService.getUsersRoles(userId) }

    override suspend fun setSelectedUserRole(userRole: UserRoleEnum?) = authDataStorage.saveSelectedUserRole(userRole)

    override suspend fun getCurrentUserRole(): UserRole {
        val userId = getAuthenticationData()?.userId ?: throw IllegalStateException("No saved user")
        return Service.wrapWithResponseException { api.usersService.getUsersRoles(userId = userId).results.first() }
    }

    override suspend fun setUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

    override suspend fun setUserRoleStatus(isConfirmed: Boolean) = authDataStorage.setUserRoleStatus(isConfirmed)
    override suspend fun saveUser(user: User?) = authDataStorage.saveUser(user)

    override suspend fun getSelectedUserRole() = authDataStorage.selectedRole.first()

    override suspend fun getUserRole() = authDataStorage.userRole.first()

    override suspend fun getAuthenticationData() = authDataStorage.authData.first()

    override suspend fun isUserRoleConfirmed() = authDataStorage.isRoleConfirmed.first()

    companion object {
        private var INSTANCE: AuthorizationRepository? = null
        fun create(authDataStorage: AuthDataStorage, api: Service): AuthorizationRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = AuthorizationRepository(authDataStorage, api) }

            return INSTANCE!!
        }
    }
}