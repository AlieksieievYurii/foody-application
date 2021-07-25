package com.yurii.foody.utils

import androidx.annotation.VisibleForTesting
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.*
import com.yurii.foody.screens.admin.requests.UserRoleRequest
import com.yurii.foody.screens.admin.requests.UserRoleRequestPagingSource
import kotlinx.coroutines.flow.*

class UserRepository @VisibleForTesting constructor(
    private val authDataStorage: AuthDataStorageInterface,
    private val authorizedService: Service
) {

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getUnconfirmedUserRolesPager() =
        Pager(config = pagingConfig, pagingSourceFactory = { UserRoleRequestPagingSource(authorizedService) }).flow

    suspend fun confirmUserRole(userRoleRequest: UserRoleRequest): UserRole {
        return authorizedService.usersService.updateUserRole(id = userRoleRequest.id, userRoleRequest.toConfirmedUserRole())
    }

    suspend fun logOut() = authDataStorage.cleanAllAuthData()

    suspend fun getSavedUser(): User? = authDataStorage.currentUser.first()

    suspend fun setSelectedUserRole(userRole: UserRoleEnum?) = authDataStorage.saveSelectedUserRole(userRole)

    suspend fun setUserRole(userRoleEnum: UserRoleEnum) = authDataStorage.saveUserRole(userRoleEnum)

    suspend fun setUserRoleStatus(isConfirmed: Boolean) = authDataStorage.setUserRoleStatus(isConfirmed)

    fun getUserRoleFlow() = authDataStorage.userRole

    suspend fun becomeCook() = authorizedService.usersService.becomeCook()

    suspend fun getCurrentUser(): User {
        val currentUser = authDataStorage.authData.first()
        return authorizedService.usersService.getUser(currentUser!!.userId)
    }

    suspend fun updateUser(user: User) = authorizedService.usersService.updateUser(
        userId = user.id, user = UserPersonalInfo(
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber
        )
    )

    companion object {
        private var INSTANCE: UserRepository? = null
        fun create(authDataStorage: AuthDataStorage, api: Service): UserRepository {
            if (INSTANCE == null)
                synchronized(UserRepository::class.java) { INSTANCE = UserRepository(authDataStorage, api) }

            return INSTANCE!!
        }
    }
}