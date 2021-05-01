package com.yurii.foody.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

enum class UserRoleEnum(val role: String) {
    @Json(name = "client")
    CLIENT("client"),

    @Json(name = "executor")
    EXECUTOR("executor"),

    @Json(name = "administrator")
    ADMINISTRATOR("administrator");

    companion object {
        fun toEnum(value: String) = valueOf(value.toUpperCase(Locale.getDefault()))
    }
}

@JsonClass(generateAdapter = true)
data class Pagination<T>(val count: Int, val next: String?, val previous: String?, val results: List<T>)

data class UserRole(@Json(name = "user") val userId: Int, @Json(name = "is_confirmed") val isConfirmed: Boolean, val role: UserRoleEnum)

@JsonClass(generateAdapter = true)
data class AuthData(val username: String, val password: String)

@JsonClass(generateAdapter = true)
data class AuthResponseData(val token: String, @Json(name = "user_id") val userId: Int, val email: String)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "pk") val id: Int,
    val email: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "is_email_confirmed") val isEmailConfirmed: Boolean
)