package com.yurii.foody.authorization

import com.yurii.foody.api.*
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.AuthDataStorageInterface
import com.yurii.foody.utils.toAuthDataStorage
import kotlinx.coroutines.flow.*

interface AuthorizationRepositoryInterface {
    suspend fun logIn(authData: AuthData): Flow<AuthResponseData>
    suspend fun logOut()
    suspend fun register(user: RegistrationForm): Flow<RegistrationForm>
    fun setToken(token: String)
    suspend fun getUser(id: Long): Flow<User>
    suspend fun getUsersRoles(userId: Int? = null): Flow<Pagination<UserRole>>

    suspend fun setSelectedUserRole(userRole: UserRoleEnum?)
    suspend fun setUserRole(userRoleEnum: UserRoleEnum)
    suspend fun setUserRoleStatus(isConfirmed: Boolean)

    suspend fun getSelectedUserRole(): UserRoleEnum?
    suspend fun getUserRole(): UserRoleEnum?
    suspend fun getAuthenticationData(): AuthDataStorage.Data?
    suspend fun isUserRoleConfirmed(): Boolean
}

class AuthorizationRepository private constructor(
    private val authDataStorage: AuthDataStorageInterface,
    private val api: Service
) : AuthorizationRepositoryInterface {

    override suspend fun logIn(authData: AuthData): Flow<AuthResponseData> =
        Service.asFlow { api.authService.logIn(authData) }.map {
            authDataStorage.saveAuthData(it.toAuthDataStorage())
            api.createAuthenticatedService(it.token)
            it
        }

    override suspend fun logOut() = authDataStorage.cleanAllAuthData()

    override suspend fun register(user: RegistrationForm) = Service.asFlow { api.authService.registerUser(user) }

    override fun setToken(token: String) = api.createAuthenticatedService(token)

    override suspend fun getUser(id: Long) = Service.asFlow { api.usersService.getUser(id) }

    override suspend fun getUsersRoles(userId: Int?) = Service.asFlow { api.usersService.getUsersRoles(userId) }

    override suspend fun setSelectedUserRole(userRole: UserRoleEnum?) = authDataStorage.saveSelectedUserRole(userRole)

    override suspend fun setUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

    override suspend fun setUserRoleStatus(isConfirmed: Boolean) = authDataStorage.setUserRoleStatus(isConfirmed)

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