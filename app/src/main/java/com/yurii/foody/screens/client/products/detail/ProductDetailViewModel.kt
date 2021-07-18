package com.yurii.foody.screens.client.products.detail

import androidx.lifecycle.*
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.api.ProductImage
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.*
import timber.log.Timber

class ProductDetailViewModel(private val repository: ProductsRepository, private val productId: Long) : ViewModel() {

    data class CostBreakDown(
        val price: Float
    ) {
        companion object {
        }
    }

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.i(exception.toString())
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    private val _product: MutableLiveData<Product> = MutableLiveData()
    val product: LiveData<Product> = _product

    private val _availability: MutableLiveData<ProductAvailability> = MutableLiveData()
    val availability: LiveData<ProductAvailability> = _availability

    private val _images: MutableLiveData<List<ProductImage>> = MutableLiveData()
    val images: LiveData<List<String>> = Transformations.map(_images) { productImages ->
        productImages.map { it.imageUrl }
    }

    init {
        netWorkScope.launch {
            awaitAll(
                async { _product.postValue(repository.getProduct(productId)) },
                async { _availability.postValue(repository.getProductAvailability(productId)) },
                async {
                    val images = mutableListOf(repository.getMainProductImage(productId))
                    images.addAll(repository.getAdditionalProductImages(productId))
                    _images.postValue(images)
                }
            )
            _isLoading.postValue(false)
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