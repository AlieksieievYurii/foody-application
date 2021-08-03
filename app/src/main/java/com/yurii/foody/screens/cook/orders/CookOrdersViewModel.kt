package com.yurii.foody.screens.cook.orders

import androidx.lifecycle.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class CookOrdersViewModel(private val productsRepository: ProductsRepository) : ViewModel() {
    companion object {
        private const val REFRESHING_TIME_IN_SECONDS = 5L
    }

    sealed class Event {
        data class NavigateToOrderDetail(val orderId: Long) : Event()
        data class ShowError(val exception: Throwable) : Event()
        object RefreshList : Event()
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            _eventChannel.send(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    private val _orders: MutableStateFlow<PagingData<Order>> = MutableStateFlow(PagingData.empty())
    val orders: StateFlow<PagingData<Order>> = _orders

    private val _listState: MutableLiveData<ListFragment.State> = MutableLiveData(ListFragment.State.Loading)
    val listState: LiveData<ListFragment.State> = _listState

    private var isRefreshing = false

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

    init {
        netWorkScope.launch {
            productsRepository.getOrdersPager().cachedIn(viewModelScope).collectLatest {
                _orders.value = it
            }

            while (true) {
                delay(REFRESHING_TIME_IN_SECONDS * 1000)
                refreshList()
            }
        }
    }

    fun openOrder(order: Order) {
        netWorkScope.launch {
            _eventChannel.send(Event.NavigateToOrderDetail(order.id))
        }
    }

    fun refreshList() {
        viewModelScope.launch {
            isRefreshing = true
            _eventChannel.send(Event.RefreshList)
        }
    }

    fun onLoadStateChange(state: CombinedLoadStates) {
        viewModelScope.launch {
            when (state.refresh) {
                is LoadState.NotLoading -> {
                    _listState.value = ListFragment.State.Ready
                    isRefreshing = false
                }
                LoadState.Loading -> {
                    if (!isRefreshing)
                        _listState.value = ListFragment.State.Loading
                }
                is LoadState.Error -> {
                    isRefreshing = false
                    val loadStateError = state.refresh as LoadState.Error
                    if (loadStateError.error is EmptyListException)
                        _listState.value = ListFragment.State.Empty
                    else
                        _listState.value = ListFragment.State.Error(loadStateError.error)
                }
            }
        }
    }

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