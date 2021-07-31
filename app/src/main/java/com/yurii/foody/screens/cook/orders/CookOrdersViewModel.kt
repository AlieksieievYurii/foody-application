package com.yurii.foody.screens.cook.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.utils.ProductsRepository

class CookOrdersViewModel(private val productsRepository: ProductsRepository) : ViewModel() {

    class Factory(private val productsRepository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CookOrdersViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CookOrdersViewModel(productsRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}