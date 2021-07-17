package com.yurii.foody.screens.client.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.utils.ProductsRepository

class ProductsViewModel(private val repository: ProductsRepository) : ViewModel() {

    class Factory(private val repository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}