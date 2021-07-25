package com.yurii.foody.api

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yurii.foody.Application
import okhttp3.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import kotlin.Exception

class Service {
    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"

        suspend fun <T : Any> wrapWithResponseException(call: suspend () -> T): T {
            try {
                return call()
            } catch (exception: Exception) {
                when (exception) {
                    is HttpException -> throw ResponseException.ServerError(exception.code(), exception.message(), exception.response()?.errorBody())
                    is IOException -> throw ResponseException.NetworkError(exception.message ?: "No error message")
                    else -> throw ResponseException.UnknownError(exception.message ?: "No error message", exception)
                }
            }
        }
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private lateinit var service: Retrofit

    constructor() {
        service = Retrofit.Builder()
            .baseUrl(Application.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    constructor(token: String) {
        service = Retrofit.Builder()
            .baseUrl(Application.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(createHttpClient(token))
            .build()
    }

    val authService: ApiTokenAuth by lazy { service.create(ApiTokenAuth::class.java) }
    val usersService: ApiUsers by lazy { service.create(ApiUsers::class.java) }
    val productsService: ApiProducts by lazy { service.create(ApiProducts::class.java) }
    val productAvailability: ApiProductAvailability by lazy { service.create(ApiProductAvailability::class.java) }
    val productsRatings: ApiProductRating by lazy { service.create(ApiProductRating::class.java) }
    val productImage: ApiProductImage by lazy { service.create(ApiProductImage::class.java) }
    val categories: ApiCategories by lazy { service.create(ApiCategories::class.java) }
    val productCategory: ApiProductCategory by lazy { service.create(ApiProductCategory::class.java) }
    val orders: ApiOrders by lazy { service.create(ApiOrders::class.java) }

    private fun createHttpClient(token: String) = OkHttpClient.Builder()
        .addInterceptor {
            val newRequest = it.request().newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build()
            it.proceed(newRequest)
        }.build()
}

sealed class ResponseException : Exception() {
    data class ServerError(val code: Int, val responseMessage: String, val errorBody: ResponseBody?) : ResponseException() {
        fun getErrorResponse(): JsonObject? = errorBody?.run {
            Parser.default().parse(errorBody.charStream()) as JsonObject
        }
    }

    data class NetworkError(val responseMessage: String) : ResponseException()
    data class UnknownError(val responseMessage: String, val thrownException: Exception) : ResponseException()
}

