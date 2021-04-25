package com.yurii.foody.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@JsonClass(generateAdapter = true)
data class AuthData(val usernfame: String, val password: String)

@JsonClass(generateAdapter = true)
data class AuthResponseData(val token: String, @Json(name = "user_id") val userId: Int, val email: String)

@JsonClass(generateAdapter = true)
data class Error(val error: JvmType.Object)