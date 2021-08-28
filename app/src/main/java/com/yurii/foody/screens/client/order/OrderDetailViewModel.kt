package com.yurii.foody.screens.client.order

import androidx.lifecycle.*
import com.yurii.foody.api.OrderExecutionStatus
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.toSimpleDateTime
import com.yurii.foody.utils.toTimestampInSeconds
import kotlinx.coroutines.*
import timber.log.Timber

data class OrderExecution(
    val productName: String,
    val productImage: String,
    val status: OrderExecutionStatus,
    val timestamp: String,
    val cookingTime: Int
) {
    private val estimatedTimeInMilSeconds = toTimestampInSeconds(timestamp) + cookingTime * 1000
    val estimatedTime = toSimpleDateTime(estimatedTimeInMilSeconds)
    val remainTime: Int
        get() {
            val time = (estimatedTimeInMilSeconds - System.currentTimeMillis()) / 60000
            return if (time < 0) 0 else time.toInt()
        }
}

class OrderDetailViewModel(private val orderId: Long, private val productsRepository: ProductsRepository) : ViewModel() {
    companion object {
        private const val REFRESH_TIME = 5000L
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            Timber.i(exception.toString())
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    private val _orderExecution: MutableLiveData<OrderExecution> = MutableLiveData()
    val orderExecution: LiveData<OrderExecution> = _orderExecution

    private val _isInitialized: MutableLiveData<Boolean> = MutableLiveData(false)
    val isInitialized: LiveData<Boolean> = _isInitialized

    init {
        netWorkScope.launch {
            while (true) {
                updateInfo()
                delay(REFRESH_TIME)
            }
        }
    }

    private suspend fun updateInfo() {
        val order = productsRepository.getOrder(orderId)
        val product = productsRepository.getProduct(order.product)
        val productImage = productsRepository.getMainProductImage(order.product)
        val orderExecution = productsRepository.getOrderExecutionFromOrder(orderId)
        _orderExecution.postValue(
            OrderExecution(
                productName = product.name,
                productImage = productImage.imageUrl,
                status = orderExecution?.status ?: OrderExecutionStatus.PENDING,
                timestamp = order.timestamp,
                cookingTime = product.cookingTime
            )
        )
        _isInitialized.postValue(true)
    }

    class Factory(private val orderId: Long, private val productsRepository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OrderDetailViewModel(orderId, productsRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}