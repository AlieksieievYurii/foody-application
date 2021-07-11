package com.yurii.foody.screens.admin.requests

import com.yurii.foody.api.Service

class UserRoleRepository private constructor(private val service: Service) {

    companion object {
        private var INSTANCE: UserRoleRepository? = null
        fun create(api: Service): UserRoleRepository {
            if (INSTANCE == null)
                synchronized(UserRoleRepository::class.java) { INSTANCE = UserRoleRepository(api) }

            return INSTANCE!!
        }
    }
}