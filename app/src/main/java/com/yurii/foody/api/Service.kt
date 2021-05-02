package com.yurii.foody.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yurii.foody.Application
import kotlinx.coroutines.flow.flow
import okhttp3.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import kotlin.Exception

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object Service {
    private val service by lazy {
        Retrofit.Builder()
            .baseUrl(Application.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private var authenticatedService: Retrofit? = null

    fun createAuthenticatedService(token: String) {
        authenticatedService = service.newBuilder()
            .client(createHttpClient(token))
            .build()
    }

    private fun authenticatedService(): Retrofit {
        checkNotNull(authenticatedService) { "You must call createAuthenticatedService firs" }
        return authenticatedService!!
    }

    private const val HEADER_AUTHORIZATION = "Authorization"

    private fun createHttpClient(token: String) = OkHttpClient.Builder()
        .addInterceptor {
            val newRequest = it.request().newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build()
            it.proceed(newRequest)
        }.build()

    val authService: ApiTokenAuth by lazy { service.create(ApiTokenAuth::class.java) }
    val usersService: ApiUsers by lazy { authenticatedService().create(ApiUsers::class.java) }

    fun <T : Any> asFlow(call: suspend () -> T) = flow {
        try {
            emit(call())
        } catch (exception: Exception) {
            when (exception) {
                is HttpException -> throw ResponseException.ServerError(exception.code(), exception.message())
                is IOException -> throw ResponseException.NetworkError(exception.message ?: "No error message")
                else -> throw ResponseException.UnknownError(exception.message ?: "No error message", exception)
            }
        }
    }
}

sealed class ResponseException : Exception() {
    data class ServerError(val code: Int, val responseMessage: String) : ResponseException()
    data class NetworkError(val responseMessage: String) : ResponseException()
    data class UnknownError(val responseMessage: String, val thrownException: Exception) : ResponseException()
}
