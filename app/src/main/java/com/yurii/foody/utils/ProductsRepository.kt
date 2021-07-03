package com.yurii.foody.utils

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.api.Service
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.screens.admin.products.ProductPagingSource

class ProductsRepository(private val service: Service) {

    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false)

    fun getProductsPager(query: ProductPagingSource.Query? = null) =
        Pager(config = pagingConfig, pagingSourceFactory = { ProductPagingSource(service, query) }).flow

    suspend fun createProduct(product: Product) = service.productsService.createProduct(product)

    suspend fun createProductAvailability(productAvailability: ProductAvailability) =
        service.productAvailability.createProductAvailability(productAvailability)

    suspend fun deleteProducts(items: List<Int>) = Service.asFlow { service.productsService.deleteProducts(items.joinToString(",")) }

    companion object {
        private var INSTANCE: ProductsRepository? = null
        fun create(api: Service): ProductsRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = ProductsRepository(api) }

            return INSTANCE!!
        }
    }
}