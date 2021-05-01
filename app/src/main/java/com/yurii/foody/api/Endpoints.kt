package com.yurii.foody.api

import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.*

interface ApiTokenAuth {
    @POST("api-token-auth/")
    suspend fun logIn(@Body authData: AuthData): NetworkResponse<AuthResponseData, Unit>
}

interface ApiUsers {

    @GET("/users/{id}")
    suspend fun getUser(@Path("id") id: Long): NetworkResponse<User, Unit>

    @GET("/users/roles")
    suspend fun getUsersRoles(@Query("user") userId: Int? = null): NetworkResponse<Pagination<UserRole>, Unit>
}