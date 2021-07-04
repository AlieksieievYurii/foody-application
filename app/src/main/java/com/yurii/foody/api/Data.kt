package com.yurii.foody.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

const val ID_NO_NEEDED = -1L

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

data class UserRole(@Json(name = "user") val userId: Long, @Json(name = "is_confirmed") val isConfirmed: Boolean, val role: UserRoleEnum)

@JsonClass(generateAdapter = true)
data class AuthData(val username: String, val password: String)

@JsonClass(generateAdapter = true)
data class AuthResponseData(val token: String, @Json(name = "user_id") val userId: Long, val email: String)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "pk") val id: Long,
    val email: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "is_email_confirmed") val isEmailConfirmed: Boolean
)

@JsonClass(generateAdapter = true)
data class UserRegistration(
    val email: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    val password: String? = null, // When this data is used as a response, password is not given
)

@JsonClass(generateAdapter = true)
data class RegistrationForm(
    val user: UserRegistration,
    val role: UserRoleEnum
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: Float,
    @Json(name = "cooking_time") val cookingTime: Int
) {
    companion object {
        fun create(name: String, description: String, price: Float, cookingTime: Int): Product {
            return Product(
                id = ID_NO_NEEDED,
                name = name,
                description = description,
                price = price,
                cookingTime = cookingTime
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class ProductAvailability(
    val id: Long,
    val available: Int,
    @Json(name = "is_available") val isAvailable: Boolean,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "product") val productId: Long
) {
    companion object {
        fun create(available: Int, isAvailable: Boolean, isActive: Boolean, productId: Long): ProductAvailability {
            return ProductAvailability(
                id = ID_NO_NEEDED,
                available = available,
                isAvailable = isAvailable,
                isActive = isActive,
                productId = productId
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class ProductRating(@Json(name = "product") val productId: Long, val rating: Float)

data class ProductImage(
    val id: Long,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "is_default") val isDefault: Boolean,
    @Json(name = "is_external") val isExternal: Boolean,
    @Json(name = "product") val productId: Long
) {
    companion object {
        fun create(imageUrl: String, isDefault: Boolean, isExternal: Boolean, productId: Long): ProductImage {
            return ProductImage(
                id = ID_NO_NEEDED,
                imageUrl = imageUrl,
                isDefault = isDefault,
                isExternal = isExternal,
                productId = productId
            )
        }
    }
}

data class Category(
    val id: Long,
    val name: String,
    @Json(name = "icon_url") val iconUrl: String,
    @Json(name = "is_icon_external") val isIconExternal: Boolean
)

@JsonClass(generateAdapter = true)
data class ProductCategory(val product: Long, val category: Long)

@JsonClass(generateAdapter = true)
data class LoadedImage(val url: String)