package com.yurii.foody.utils

import com.yurii.foody.api.*
import java.net.HttpURLConnection.HTTP_NOT_FOUND

class FakeApiService(
    val users: MutableList<User> = mutableListOf(),
    val userRoles: MutableList<UserRole> = mutableListOf()
) : ApiServiceInterface {

    private var isAuthenticatedServiceCreated: Boolean = false

    override fun createAuthenticatedService(token: String) {
        isAuthenticatedServiceCreated = true
    }

    override val authService: ApiTokenAuth = AuthService()
    override val usersService: ApiUsers = UsersService()

    private inner class AuthService : ApiTokenAuth {
        override suspend fun logIn(authData: AuthData): AuthResponseData {
            return AuthResponseData(token="1234", userId = 1, email = "test@email.com")
        }

        override suspend fun registerUser(user: RegistrationForm): RegistrationForm {
            return user.copy()
        }
    }

    private inner class UsersService : ApiUsers {
        override suspend fun getUser(id: Long): User {
            return users.find { it.id.toLong() == id } ?: throw ResponseException.ServerError(
                code = HTTP_NOT_FOUND,
                responseMessage = "User with ID $id not found", null
            )
        }

        override suspend fun getUsersRoles(userId: Int?): Pagination<UserRole> {
            val userRole = userRoles.find { it.userId == userId } ?: throw ResponseException.ServerError(
                code = HTTP_NOT_FOUND,
                responseMessage = "RoleUser with user ID $userId not found", null
            )
            return Pagination(count = 1, next = null, previous = null, results = listOf(userRole))
        }

    }
}