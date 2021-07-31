package com.yurii.foody.screens.cook.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.utils.ProductsRepository

class OrderExecutionViewModel(private val productsRepository: ProductsRepository, private val orderExecutionId: Long) : ViewModel() {

    class Factory(private val productsRepository: ProductsRepository, private val orderExecutionId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderExecutionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OrderExecutionViewModel(productsRepository, orderExecutionId) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}