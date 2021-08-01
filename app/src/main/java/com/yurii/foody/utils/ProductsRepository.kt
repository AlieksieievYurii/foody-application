package com.yurii.foody.utils

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.*
import com.yurii.foody.screens.admin.categories.CategoriesPagingSource
import com.yurii.foody.screens.admin.products.ProductPagingSource
import com.yurii.foody.screens.client.products.ProductsPagingSource
import com.yurii.foody.screens.cook.orders.OrdersPagingSource
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.HttpException
import java.net.HttpURLConnection

class ProductsRepository(private val service: Service) {

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getProductsPagerForClient(search: String? = null) =
        Pager(config = pagingConfig, pagingSourceFactory = { ProductsPagingSource(service, search) }).flow

    fun getProductsPager(query: ProductPagingSource.Query? = null) =
        Pager(config = pagingConfig, pagingSourceFactory = { ProductPagingSource(service, query) }).flow

    fun getCategoriesPager() = Pager(config = pagingConfig, pagingSourceFactory = { CategoriesPagingSource(service) }).flow

    fun getOrdersPager() = Pager(config = pagingConfig, pagingSourceFactory = { OrdersPagingSource(service) }).flow

    suspend fun deleteCategories(items: List<Long>) = service.categories.deleteCategories(items.joinToString(","))

    suspend fun createProduct(product: Product) = service.productsService.createProduct(product)

    suspend fun getProduct(id: Long) = service.productsService.getProduct(id)

    suspend fun createProductAvailability(productAvailability: ProductAvailability) =
        service.productAvailability.createProductAvailability(productAvailability)

    suspend fun getProductAvailability(productId: Long) = service.productAvailability.getProductAvailability(productId)

    suspend fun createProductCategory(productCategory: ProductCategory) = service.productCategory.createProductCategory(productCategory)

    suspend fun deleteProducts(items: List<Long>) = service.productsService.deleteProducts(items.joinToString(","))

    suspend fun createProductImage(productImage: ProductImage) = service.productImage.createProductImage(productImage)

    suspend fun updateProduct(product: Product) = service.productsService.updateProduct(id = product.id, product = product)

    suspend fun updateProductAvailability(productAvailability: ProductAvailability) =
        service.productAvailability.updateProductAvailability(productAvailability.productId, productAvailability)

    suspend fun getProductCategory(productId: Long) = service.productCategory.getProductCategory(productId)

    suspend fun updateProductCategory(productId: Long, productCategory: ProductCategory) =
        service.productCategory.updateProductCategory(productId, productCategory)

    suspend fun removeProductCategory(productId: Long) = service.productCategory.removeProductCategory(productId)

    suspend fun getMainProductImage(productId: Long): ProductImage =
        service.productImage.getProductsImages(
            productIds = productId.toString(),
            page = 1, size = 1, isDefault = true
        ).results.first()

    suspend fun getImages(productId: Long): List<ProductImage> {
        val images = mutableListOf(getMainProductImage(productId))
        images.addAll(getAdditionalProductImages(productId))
        return images
    }

    suspend fun getCategories() = getAllCategories(page = 1)

    suspend fun getCurrentOrderExecution(): OrderExecutionResponse? =
        try {
            service.ordersExecution.getCurrentOrderExecution()
        } catch (error: HttpException) {
            if (error.code() == HttpURLConnection.HTTP_NOT_FOUND)
                null
            else
                throw error
        }

    private suspend fun getAllCategories(page: Int): List<Category> {
        val res = service.categories.getCategories(page, size = 100)
        return if (res.next != null)
            res.results + getAllCategories(page + 1)
        else
            res.results
    }

    suspend fun getAdditionalProductImages(productId: Long) = getAdditionalProductImages(productId, page = 1)

    suspend fun deleteProductImage(productImageId: Long) = service.productImage.deleteProductImage(productImageId)

    private suspend fun getAdditionalProductImages(productId: Long, page: Int): List<ProductImage> {
        val res = service.productImage.getProductsImages(
            productIds = productId.toString(),
            page = page, size = 1, isDefault = false
        )

        return if (res.next != null)
            res.results + getAdditionalProductImages(productId, page + 1)
        else
            res.results
    }

    suspend fun uploadImage(bytes: ByteArray): LoadedImage {
        val requestBody = RequestBody.create(MediaType.parse("image/*"), bytes)
        return service.productImage.uploadImage(requestBody)
    }

    suspend fun createCategory(category: Category) = service.categories.createCategory(category)

    suspend fun getCategory(categoryIdToEdit: Long): Category = service.categories.getCategory(categoryIdToEdit)

    suspend fun updateCategory(category: Category): Category = service.categories.updateCategory(category.id, category)

    suspend fun createOrder(orderForm: OrderForm): Order = service.orders.createOrder(orderForm)

    suspend fun getOrder(orderId: Long): Order = service.orders.getOrder(orderId)

    suspend fun getOrderExecution(orderId: Long): OrderExecutionResponse = service.ordersExecution.getOrderExecution(orderId)

    suspend fun createOrderExecution(order: OrderExecution): OrderExecutionResponse = service.ordersExecution.createOrderExecution(order)

    suspend fun getProductRating(productId: Long): Float {
        val result = service.productsRatings.getProductsRatings(productId.toString())
        return result.firstOrNull()?.rating ?: 0f
    }

    companion object {
        private var INSTANCE: ProductsRepository? = null
        fun create(api: Service): ProductsRepository {
            if (INSTANCE == null)
                synchronized(ProductsRepository::class.java) { INSTANCE = ProductsRepository(api) }

            return INSTANCE!!
        }
    }
}