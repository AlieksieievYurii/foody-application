package com.yurii.foody.screens.cook.execution

import androidx.lifecycle.*
import com.yurii.foody.api.Order
import com.yurii.foody.api.OrderExecution
import com.yurii.foody.api.OrderExecutionStatus
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

data class OrderDetail(
    val id: Long,
    val product: Long,
    val user: Long,
    val count: Int,
    val price: Float,
    val cookingTime: Int,
    val timestamp: Long
) {
    val timestampDateTime = toSimpleDateTime(timestamp)
    val isDelayed = isOrderDelayed(timestamp, cookingTime)
    val total = price * count
}

class OrderExecutionViewModel(
    private val productsRepository: ProductsRepository,
    private val orderId: Long? = null,
    private var orderExecutionId: Long? = null
) : ViewModel() {
    sealed class Event {
        data class AskUserToChangeOrderExecutionStatus(
            val currentStatus: OrderExecutionStatus,
            val nextOrderStatus: OrderExecutionStatus
        ) : Event()

        data class ShowError(val exception: Throwable) : Event()
        object CloseScreen : Event()
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
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

    private val _order: MutableLiveData<OrderDetail> = MutableLiveData()
    val order: LiveData<OrderDetail> = _order

    private val _isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    private val _isOrderTaken: MutableLiveData<Boolean> = MutableLiveData(orderExecutionId != null)
    val isOrderTaken: LiveData<Boolean> = _isOrderTaken

    private val _changingOrderStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val changingOrderStatus: LiveData<Boolean> = _changingOrderStatus

    private val _orderExecutionStatus: MutableLiveData<OrderExecutionStatus> = MutableLiveData(OrderExecutionStatus.PENDING)
    val orderExecutionStatus: LiveData<OrderStatusComponent.Status> = Transformations.map(_orderExecutionStatus) { status ->
        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        return@map when (status) {
            OrderExecutionStatus.PENDING -> OrderStatusComponent.Status.TAKING
            OrderExecutionStatus.COOKING -> OrderStatusComponent.Status.COOKING
            OrderExecutionStatus.FINISHED -> OrderStatusComponent.Status.FINISHED
            OrderExecutionStatus.DELIVERED -> OrderStatusComponent.Status.DELIVERED
        }
    }

    init {
        netWorkScope.launch {
            when {
                orderId != null -> loadProductDetail(productId = loadOrder(orderId).product)
                orderExecutionId != null -> {
                    val orderExecution = productsRepository.getOrderExecution(orderExecutionId!!)
                    loadProductDetail(productId = loadOrder(orderExecution.order).product)
                    _orderExecutionStatus.postValue(orderExecution.status)
                }
                else -> throw IllegalStateException("OrderID and OrderExecution can not be defined at the same time")
            }
            _isInitialized.postValue(true)
        }
    }

    private suspend fun loadOrder(orderId: Long): Order {
        val order = productsRepository.getOrder(orderId)
        _order.postValue(
            OrderDetail(
                id = order.id,
                product = order.product,
                user = order.user,
                count = order.count,
                price = order.price,
                cookingTime = order.cookingTime,
                timestamp = toTimestampInSeconds(order.timestamp)
            )
        )
        return order
    }

    private suspend fun loadProductDetail(productId: Long) {
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

    fun takeOrder() {
        netWorkScope.launch {
            val orderExecution = productsRepository.createOrderExecution(OrderExecution(orderId = order.value!!.id))
            orderExecutionId = orderExecution.id
            _isOrderTaken.postValue(true)
        }
    }

    fun performChangingOrderStatus(nextOrderStatus: OrderExecutionStatus) {
        viewModelScope.launch {
            _changingOrderStatus.postValue(true)
            productsRepository.updateOrderExecution(orderExecutionId!!, status = nextOrderStatus)
            delay(2000)
            _changingOrderStatus.postValue(false)
            _orderExecutionStatus.postValue(nextOrderStatus)
            if (nextOrderStatus == OrderExecutionStatus.DELIVERED)
                eventChannel.send(Event.CloseScreen)
        }
    }

    fun onOrderStatusChanged(statusComponent: OrderStatusComponent.Status) {
        if (changingOrderStatus.value == true)
            return

        val nextStatus = when (statusComponent) {
            OrderStatusComponent.Status.TAKING -> OrderExecutionStatus.COOKING
            OrderStatusComponent.Status.COOKING -> OrderExecutionStatus.FINISHED
            OrderStatusComponent.Status.FINISHED -> OrderExecutionStatus.DELIVERED
            OrderStatusComponent.Status.DELIVERED -> throw IllegalStateException("Must not be called")
        }

        viewModelScope.launch {
            eventChannel.send(Event.AskUserToChangeOrderExecutionStatus(_orderExecutionStatus.value!!, nextStatus))
        }
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