package com.yurii.foody.screens.client.products.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.api.Product
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.*
import timber.log.Timber

class ProductDetailViewModel(private val repository: ProductsRepository, private val productId: Long) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.i(exception.toString())
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    private val _product: MutableLiveData<Product> = MutableLiveData()
    val product: LiveData<Product> = _product

    init {
        netWorkScope.launch {
            awaitAll(
                async { _product.postValue(repository.getProduct(productId)) }
            )
        }
    }

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