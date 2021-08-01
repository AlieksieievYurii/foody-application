package com.yurii.foody.screens.cook.execution

import androidx.lifecycle.*
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.convertToAverageTime
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class ProductDetail(
    val id: Long,
    val name: String,
    val description: String,
    val price: Float,
    val cookingTime: Int,
    val available: Int,
    val isAvailable: Boolean,
    val isActive: Boolean,
    val rating: Float,
    val imagesUrls: List<String>
) {
    val averageTime = convertToAverageTime(cookingTime)
}

class OrderExecutionViewModel(private val productsRepository: ProductsRepository, private val orderExecutionId: Long) : ViewModel() {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object CloseScreen : Event()
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            throw exception
            eventChannel.send(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val coroutineContext = viewModelJob + Dispatchers.IO + coroutineExceptionHandler
    private val netWorkScope = CoroutineScope(coroutineContext)

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val _product: MutableLiveData<ProductDetail> = MutableLiveData()
    val product: LiveData<ProductDetail> = _product

    private val _isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    init {
        netWorkScope.launch {
            val orderExecutionId = productsRepository.getOrderExecution(orderExecutionId)
            val order = productsRepository.getOrder(orderExecutionId.order)
            val product = productsRepository.getProduct(order.product)
            val availability = productsRepository.getProductAvailability(product.id)
            val productImages = productsRepository.getImages(product.id)
            val rating = productsRepository.getProductRating(product.id)
            _product.postValue(
                ProductDetail(
                    id = product.id,
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    cookingTime = product.cookingTime,
                    available = availability.available,
                    isActive = availability.isActive,
                    isAvailable = availability.isAvailable,
                    rating = rating,
                    imagesUrls = productImages.map { it.imageUrl }
                )
            )
            _isInitialized.postValue(true)
        }
    }

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