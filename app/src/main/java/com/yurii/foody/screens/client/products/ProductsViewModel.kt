package com.yurii.foody.screens.client.products

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

class ProductsViewModel(private val repository: ProductsRepository) : ViewModel() {
    sealed class Event {
        object Refresh : Event()
    }

    private var isRefreshing = false

    private val _products: MutableStateFlow<PagingData<ProductItem>> = MutableStateFlow(PagingData.empty())
    val products: StateFlow<PagingData<ProductItem>> = _products

    private val _listState: MutableLiveData<ListFragment.State> = MutableLiveData(ListFragment.State.Loading)
    val listState: LiveData<ListFragment.State> = _listState

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
    }

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    private var searchJob: Job? = null

    init {
        searchProduct()
    }

    fun searchProduct(keyWords: String? = null) {
        searchJob?.cancel()
        netWorkScope.launch {
            searchJob = launch {
                repository.getProductsPagerForClient(search = keyWords).cachedIn(viewModelScope).collectLatest {
                    _products.value = it
                }
            }
        }
    }

    fun onLoadStateChange(state: CombinedLoadStates) {
        viewModelScope.launch {
            when (state.refresh) {
                is LoadState.NotLoading -> {
                    isRefreshing = false
                    _listState.value = ListFragment.State.Ready
                }
                LoadState.Loading ->
                    if (!isRefreshing)
                        _listState.value = ListFragment.State.Loading
                is LoadState.Error -> {
                    val loadStateError = state.refresh as LoadState.Error
                    if (loadStateError.error is EmptyListException)
                        _listState.value = ListFragment.State.Empty
                    else
                        _listState.value = ListFragment.State.Error(loadStateError.error)
                }
            }
        }

    }

    fun refreshList() {
        viewModelScope.launch {
            isRefreshing = true
            _eventChannel.send(Event.Refresh)
        }
    }


    class Factory(private val repository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}