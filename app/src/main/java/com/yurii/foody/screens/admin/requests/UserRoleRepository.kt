package com.yurii.foody.screens.admin.requests

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.Service
import com.yurii.foody.api.UserRole

class UserRoleRepository private constructor(private val service: Service) {

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getUnconfirmedUserRolesPager() =
        Pager(config = pagingConfig, pagingSourceFactory = { UserRoleRequestPagingSource(service) }).flow

    suspend fun confirmUserRole(userRoleRequest: UserRoleRequest): UserRole {
        return service.usersService.updateUserRole(id = userRoleRequest.id, userRoleRequest.toConfirmedUserRole())
    }

    companion object {
        private var INSTANCE: UserRoleRepository? = null
        fun create(api: Service): UserRoleRepository {
            if (INSTANCE == null)
                synchronized(UserRoleRepository::class.java) { INSTANCE = UserRoleRepository(api) }

            return INSTANCE!!
        }
    }
}