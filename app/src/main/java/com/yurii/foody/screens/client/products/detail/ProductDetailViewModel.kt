package com.yurii.foody.screens.client.products.detail

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.OrderForm
import com.yurii.foody.api.Product
import com.yurii.foody.api.ProductAvailability
import com.yurii.foody.api.ProductImage
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.value
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ProductDetailViewModel(private val repository: ProductsRepository, private val productId: Long) : ViewModel() {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object CloseScreen : Event()
    }

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            _isLoading.value = false
            eventChannel.send(Event.ShowError(exception))
        }
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

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()


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
            _isInitialized.postValue(true)
        }
    }

    fun order() {
        val orderForm = OrderForm(
            product = productId,
            count = count.value
        )
        netWorkScope.launch {
            _isLoading.value = true
            repository.createOrder(orderForm)
            _isLoading.value = false
            eventChannel.send(Event.CloseScreen)
        }

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