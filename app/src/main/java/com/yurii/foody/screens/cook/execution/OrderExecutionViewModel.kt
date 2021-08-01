package com.yurii.foody.screens.cook.execution

import androidx.lifecycle.*
import com.yurii.foody.api.Order
import com.yurii.foody.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.lang.IllegalStateException

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
    val imagesUrls: List<String>,
) {
    val averageTime = convertToAverageTime(cookingTime)
}

class OrderExecutionViewModel(
    private val productsRepository: ProductsRepository,
    private val orderId: Long? = null,
    private val orderExecutionId: Long? = null
) : ViewModel() {
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

    private val _order: MutableLiveData<Order> = MutableLiveData()
    val order: LiveData<Order> = _order

    private val _isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    init {
        netWorkScope.launch {
            when {
                orderId != null -> {
                    val order = productsRepository.getOrder(orderId)
                    loadProductDetail(order.product)
                }
                orderExecutionId != null -> {
                    val orderExecution = productsRepository.getOrderExecution(orderExecutionId)
                    val order = productsRepository.getOrder(orderExecution.order)
                    loadProductDetail(order.product)
                }
                else -> throw IllegalStateException("OrderID and OrderExecution can not be defined at the same time")
            }
            _isInitialized.postValue(true)
        }
    }

    private suspend fun loadProductDetail(productId: Long) {
        //TODO(alieksiy) Should be done in the repository
        val product = productsRepository.getProduct(productId)
        val availability = productsRepository.getProductAvailability(productId)
        val productImages = productsRepository.getImages(productId)
        val rating = productsRepository.getProductRating(productId)
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
                imagesUrls = productImages.map { it.imageUrl },
            )
        )
    }

    class Factory(
        private val productsRepository: ProductsRepository,
        private val orderId: Long? = null,
        private val orderExecutionId: Long? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderExecutionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OrderExecutionViewModel(productsRepository, orderId, orderExecutionId) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}