package com.yurii.foody.api

import retrofit2.http.*

interface ApiTokenAuth {
    @POST("api-token-auth/")
    suspend fun logIn(@Body authData: AuthData): AuthResponseData
}

interface ApiUsers {

    @GET("/users/{id}")
    suspend fun getUser(@Path("id") id: Long): User

    @GET("/users/roles")
    suspend fun getUsersRoles(@Query("user") userId: Int? = null): Pagination<UserRole>
}