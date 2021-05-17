package com.yurii.foody.utils

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.yurii.foody.api.ApiProducts
import com.yurii.foody.api.Service
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.screens.admin.products.ProductPagingSource

class ProductsRepository(private val api: ApiProducts) {

    private val pagingConfig = PagingConfig(pageSize = 10, initialLoadSize = 10, enablePlaceholders = false)

    fun getProductsPager() = Pager(config = pagingConfig, pagingSourceFactory = { ProductPagingSource(api) }).flow

    companion object {
        private var INSTANCE: ProductsRepository? = null
        fun create(api: Service): ProductsRepository {
            if (INSTANCE == null)
                synchronized(AuthorizationRepository::class.java) { INSTANCE = ProductsRepository(api.productsService) }

            return INSTANCE!!
        }
    }
}