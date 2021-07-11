package com.yurii.foody.screens.admin.requests

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.Service

class UserRoleRepository private constructor(private val service: Service) {

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getUnconfirmedUserRolesPager() =
        Pager(config = pagingConfig, pagingSourceFactory = { UserRoleRequestPagingSource(service) }).flow

    companion object {
        private var INSTANCE: UserRoleRepository? = null
        fun create(api: Service): UserRoleRepository {
            if (INSTANCE == null)
                synchronized(UserRoleRepository::class.java) { INSTANCE = UserRoleRepository(api) }

            return INSTANCE!!
        }
    }
}