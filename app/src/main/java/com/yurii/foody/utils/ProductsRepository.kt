package com.yurii.foody.utils

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.*
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.screens.admin.products.ProductPagingSource
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

class ProductsRepository(private val service: Service) {

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getProductsPager(query: ProductPagingSource.Query? = null) =
        Pager(config = pagingConfig, pagingSourceFactory = { ProductPagingSource(service, query) }).flow

    suspend fun createProduct(product: Product) = service.productsService.createProduct(product)

    suspend fun getProduct(id: Long) = service.productsService.getProduct(id)

    suspend fun createProductAvailability(productAvailability: ProductAvailability) =
        service.productAvailability.createProductAvailability(productAvailability)

    suspend fun getProductAvailability(productId: Long) = service.productAvailability.getProductAvailability(productId)

    suspend fun createProductCategory(productCategory: ProductCategory) = service.productCategory.createProductCategory(productCategory)

    suspend fun deleteProducts(items: List<Long>) = Service.asFlow { service.productsService.deleteProducts(items.joinToString(",")) }

    suspend fun createProductImage(productImage: ProductImage) = service.productImage.createProductImage(productImage)

    suspend fun uploadImage(bytes: ByteArray): LoadedImage {
        val requestBody = RequestBody.create(MediaType.parse("image/*"), bytes)
        return service.productImage.uploadImage(requestBody)
    }

    companion object {
        private var INSTANCE: ProductsRepository? = null
        fun create(api: Service): ProductsRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = ProductsRepository(api) }

            return INSTANCE!!
        }
    }
}