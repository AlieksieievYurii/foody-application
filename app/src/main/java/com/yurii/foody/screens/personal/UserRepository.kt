package com.yurii.foody.screens.personal

import com.yurii.foody.api.Service
import com.yurii.foody.api.User
import com.yurii.foody.utils.AuthDataStorageInterface
import kotlinx.coroutines.flow.first

class UserRepository(private val service: Service, private val authDataStorage: AuthDataStorageInterface) {

    suspend fun getCurrentUser(): User {
        val currentUser = authDataStorage.authData.first()
        return service.usersService.getUser(currentUser!!.userId)
    }

    companion object {
        private var INSTANCE: UserRepository? = null
        fun create(api: Service, authDataStorage: AuthDataStorageInterface): UserRepository {
            if (INSTANCE == null)
                synchronized(UserRepository::class.java) { INSTANCE = UserRepository(api, authDataStorage) }

            return INSTANCE!!
        }
    }
}