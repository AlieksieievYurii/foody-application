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

    @GET("/users/")
    suspend fun getUsers(
        @Query("users_ids") userIds: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Pagination<User>

    @GET("/users/roles")
    suspend fun getUsersRoles(
        @Query("user") userId: Long? = null,
        @Query("is_confirmed") isConfirmed: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Pagination<UserRole>

    @PUT("/users/role/{id}")
    suspend fun updateUserRole(@Path("id") id: Long, @Body userRole: UserRole): UserRole

    @PATCH("/users/{id}")
    suspend fun updateUser(@Path("id") userId: Long, @Body user: UserPersonalInfo): User

    @POST("/users/become-cook/")
    suspend fun becomeCook(): Response<Unit>
}

interface ApiProducts {
    @GET("/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("availability__is_available") isAvailable: Boolean? = null,
        @Query("availability__is_active") isActive: Boolean? = null,
        @Query("ids") ids: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
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

    @POST("/products/feedback/")
    suspend fun createFeedback(@Body feedback: ProductUserFeedback): ProductUserFeedback

    @GET("/products/feedback/")
    suspend fun getProductsUserRatings(
        @Query("mine") mine: Boolean? = null,
        @Query("product_ids") productsIds: String? = null
    ): Pagination<ProductUserRating>

    @GET("/products/feedback/product-rating")
    suspend fun getProductsRatings(@Query("product_ids") productIds: String): List<ProductRating>
}

interface ApiProductImage {
    @GET("/products/images/")
    suspend fun getProductsImages(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("product_ids") productIds: String,
        @Query("is_default") isDefault: Boolean,
    ): Pagination<ProductImage>

    @POST("/products/images/")
    suspend fun createProductImage(@Body productImage: ProductImage): ProductImage

    @POST("/products/images/upload/")
    suspend fun uploadImage(@Body image: RequestBody): LoadedImage

    @DELETE("/products/images/{id}/")
    suspend fun deleteProductImage(@Path("id") productImageId: Long): Response<Unit>
}

interface ApiCategories {
    @GET("/products/categories/")
    suspend fun getCategories(@Query("page") page: Int? = null, @Query("size") size: Int): Pagination<Category>

    @DELETE("/products/categories/delete_many/")
    suspend fun deleteCategories(@Query("ids") ids: String): Response<Unit>

    @POST("/products/categories/")
    suspend fun createCategory(@Body category: Category): Category

    @GET("/products/categories/{id}/")
    suspend fun getCategory(@Path("id") categoryIdToEdit: Long): Category

    @PUT("/products/categories/{id}/")
    suspend fun updateCategory(@Path("id") categoryId: Long, @Body category: Category): Category
}

interface ApiProductCategory {
    @POST("/products/productCategory/")
    suspend fun createProductCategory(@Body productCategory: ProductCategory)

    @GET("/products/productCategory/{product}/")
    suspend fun getProductCategory(@Path("product") productId: Long): ProductCategory

    @PUT("/products/productCategory/{product}/")
    suspend fun updateProductCategory(@Path("product") productId: Long, @Body productCategory: ProductCategory): ProductCategory

    @DELETE("/products/productCategory/{product}/")
    suspend fun removeProductCategory(@Path("product") productId: Long): Response<Unit>
}

interface ApiOrders {
    @GET("/orders/{id}/")
    suspend fun getOrder(@Path("id") orderId: Long): Order

    @GET("/orders/")
    suspend fun getOrders(
        @Query("ordering") ordering: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int,
        @Query("mine") mine: Boolean = false
    ): Pagination<Order>

    @POST("/orders/")
    suspend fun createOrder(@Body order: OrderForm): Order
}

interface ApiOrderExecution {
    @GET("/orders/current_order_execution")
    suspend fun getCurrentOrderExecution(): OrderExecutionResponse

    @GET("/orders/execution/")
    suspend fun getOrdersExecutions(@Query("orders_ids") ordersIds: String? = null): Pagination<OrderExecutionResponse>

    @GET("/orders/execution/{id}/")
    suspend fun getOrderExecution(@Path("id") orderExecutionId: Long): OrderExecutionResponse

    @POST("/orders/execution/")
    suspend fun createOrderExecution(@Body orderExecution: OrderExecution): OrderExecutionResponse

    @PATCH("/orders/execution/{id}/")
    suspend fun updateOrderExecution(@Path("id") orderExecutionId: Long, @Body body: OrderExecutionPatch): OrderExecutionResponse

    @GET("/orders/history/")
    suspend fun getUserHistory(@Query("mine") mine: Boolean): Pagination<History>
}