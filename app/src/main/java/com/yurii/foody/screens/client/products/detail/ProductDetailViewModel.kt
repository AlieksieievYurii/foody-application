package com.yurii.foody.screens.client.products.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.utils.ProductsRepository

class ProductDetailViewModel(private val repository: ProductsRepository, private val productId: Long) : ViewModel() {

    class Factory(private val repository: ProductsRepository, private val productId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductDetailViewModel(repository, productId) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}