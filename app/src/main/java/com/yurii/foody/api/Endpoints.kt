package com.yurii.foody.api

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiTokenAuth {
    @POST("api-token-auth/")
    suspend fun logIn(@Body authData: AuthData): AuthResponseData

    @POST("/users/register")
    suspend fun registerUser(@Body user: RegistrationForm): RegistrationForm

}

interface ApiUsers {

    @GET("/users/{id}")
    suspend fun getUser(@Path("id") id: Long): User

    @GET("/users/roles")
    suspend fun getUsersRoles(@Query("user") userId: Long? = null): Pagination<UserRole>
}

interface ApiProducts {
    @GET("/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("availability__is_available") isAvailable: Boolean? = null,
        @Query("availability__is_active") isActive: Boolean? = null,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Pagination<Product>

    @POST("/products/")
    suspend fun createProduct(@Body product: Product): Product

    @DELETE("/products/delete_many/")
    suspend fun deleteProducts(@Query("ids") ids: String): Response<Unit>

    @GET("/products/{id}/")
    suspend fun getProduct(@Path("id") id: Long): Product

    @PUT("/products/{id}/")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: Product): Product
}

interface ApiProductAvailability {
    @GET("/products/availabilities")
    suspend fun getProductAvailability(
        @Query("product_ids") productIds: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int
    ): Pagination<ProductAvailability>

    @POST("/products/availabilities/")
    suspend fun createProductAvailability(@Body productAvailability: ProductAvailability)

    @GET("/products/availabilities/{product}/")
    suspend fun getProductAvailability(@Path("product") productId: Long): ProductAvailability

    @PUT("/products/availabilities/{product}/")
    suspend fun updateProductAvailability(@Path("product") productId: Long, @Body productAvailability: ProductAvailability): ProductAvailability
}

interface ApiProductRating {
    @GET("/products/feedback/product-rating")
    suspend fun getProductsRatings(@Query("product_ids") productIds: String): List<ProductRating>
}

interface ApiProductImage {
    @GET("/products/images/")
    suspend fun getProductsImages(
        @Query("page") page: Int? = null,
        @Query("size") size: Int,
        @Query("product_ids") productIds: String,
        @Query("is_default") isDefault: Boolean,
    ): Pagination<ProductImage>

    @POST("/products/images/")
    suspend fun createProductImage(@Body productImage: ProductImage): ProductImage

    @POST("/products/images/upload/")
    suspend fun uploadImage(@Body image: RequestBody): LoadedImage
}

interface ApiCategories {
    @GET("/products/categories/")
    suspend fun getCategories(): List<Category>
}

interface ApiProductCategory {
    @POST("/products/productCategory/")
    suspend fun createProductCategory(@Body productCategory: ProductCategory)
}