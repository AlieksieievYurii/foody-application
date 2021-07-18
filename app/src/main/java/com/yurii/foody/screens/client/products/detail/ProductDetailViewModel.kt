package com.yurii.foody.screens.client.products.detail

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.api.ProductImage
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.value
import kotlinx.coroutines.*
import timber.log.Timber

class ProductDetailViewModel(private val repository: ProductsRepository, private val productId: Long) : ViewModel() {
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

    private val _total: MutableLiveData<Float> = MutableLiveData(0f)
    val total: LiveData<Float> = _total

    private val _isOrderingEnable: MutableLiveData<Boolean> = MutableLiveData()
    val isOrderingEnable: LiveData<Boolean> = _isOrderingEnable

    private val _images: MutableLiveData<List<ProductImage>> = MutableLiveData()
    val images: LiveData<List<String>> = Transformations.map(_images) { productImages ->
        productImages.map { it.imageUrl }
    }

    val count = ObservableField(0).also {
        it.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                calculateTotal()
            }

        })
    }

    private fun calculateTotal() {
        _total.postValue(_product.value!!.price * count.value)
        _isOrderingEnable.postValue(count.value != 0)
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

    fun order() {

    }

    fun addToCart() {

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