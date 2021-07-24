package com.yurii.foody.api

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.yurii.foody.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import kotlin.Exception

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

interface ApiServiceInterface {
    fun createAuthenticatedService(token: String)
    val authService: ApiTokenAuth
    val usersService: ApiUsers
    val productsService: ApiProducts
    val productAvailability: ApiProductAvailability
    val productsRatings: ApiProductRating
    val productImage: ApiProductImage
    val categories: ApiCategories
    val productCategory: ApiProductCategory
    val orders: ApiOrders
}

object Service : ApiServiceInterface {
    private val service by lazy {
        Retrofit.Builder()
            .baseUrl(Application.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private var authenticatedService: Retrofit? = null

    override fun createAuthenticatedService(token: String) {
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

    override val authService: ApiTokenAuth by lazy { service.create(ApiTokenAuth::class.java) }
    override val usersService: ApiUsers by lazy { authenticatedService().create(ApiUsers::class.java) }
    override val productsService: ApiProducts by lazy { authenticatedService().create(ApiProducts::class.java) }
    override val productAvailability: ApiProductAvailability by lazy { authenticatedService().create(ApiProductAvailability::class.java) }
    override val productsRatings: ApiProductRating by lazy { authenticatedService().create(ApiProductRating::class.java) }
    override val productImage: ApiProductImage by lazy { authenticatedService().create(ApiProductImage::class.java) }
    override val categories: ApiCategories by lazy { authenticatedService().create(ApiCategories::class.java) }
    override val productCategory: ApiProductCategory by lazy { authenticatedService().create(ApiProductCategory::class.java) }
    override val orders: ApiOrders by lazy { authenticatedService().create(ApiOrders::class.java) }

    fun <T : Any> asFlow(call: suspend () -> T) = flow {
        emit(wrapWithResponseException(call))
    }.flowOn(Dispatchers.IO)

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

sealed class ResponseException : Exception() {
    data class ServerError(val code: Int, val responseMessage: String, val errorBody: ResponseBody?) : ResponseException() {
        fun getErrorResponse(): JsonObject? = errorBody?.run {
            Parser.default().parse(errorBody.charStream()) as JsonObject
        }
    }

    data class NetworkError(val responseMessage: String) : ResponseException()
    data class UnknownError(val responseMessage: String, val thrownException: Exception) : ResponseException()
}

