package com.yurii.foody.screens.admin.products

import androidx.lifecycle.*
import androidx.paging.*
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class ProductsEditorViewModel(private val repository: ProductsRepository) : ViewModel() {
    sealed class Event {
        object Refresh : Event()
        object ShowItemsRemovedSnackBar : Event()
    }

    private val _products: MutableStateFlow<PagingData<ProductData>> = MutableStateFlow(PagingData.empty())
    val products: StateFlow<PagingData<ProductData>> = _products

    private val _listState: MutableLiveData<ListFragment.State> = MutableLiveData(ListFragment.State.Loading)
    val listState: LiveData<ListFragment.State> = _listState

    private var searchJob: Job? = null

    private val query = ProductPagingSource.Query()

    val selectableMode = MutableStateFlow(false)

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

    var isRefreshing = false

    init {
        searchProduct()
    }

    private fun searchProduct() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.getProductsPager(query).cachedIn(viewModelScope).collectLatest {
                _products.value = it
            }
        }
    }

    fun deleteItems(items: List<ProductData>) {
        viewModelScope.launch {
            _loading.value = true
            repository.deleteProducts(items.map { it.id })
            _eventChannel.send(Event.Refresh)
        }
    }

    fun search(text: String? = null) {
        query.search = text
        searchProduct()
    }

    fun filterActive(enable: Boolean) {
        query.isActive = if (enable) true else null
        searchProduct()
    }

    fun filterAvailable(enable: Boolean) {
        query.isAvailable = if (enable) true else null
        searchProduct()
    }

    fun onLoadStateChange(state: CombinedLoadStates) {
        viewModelScope.launch {
            when (state.refresh) {
                is LoadState.NotLoading -> {
                    isRefreshing = false
                    _listState.value = ListFragment.State.Ready
                    if (_loading.value) {
                        _loading.value = false
                        selectableMode.value = false
                        _eventChannel.send(Event.ShowItemsRemovedSnackBar)
                    }
                }
                LoadState.Loading ->
                    if (!isRefreshing)
                        _listState.value = ListFragment.State.Loading
                is LoadState.Error -> {
                    _loading.value = false
                    val loadStateError = state.refresh as LoadState.Error
                    if (loadStateError.error is EmptyListException)
                        _listState.value = ListFragment.State.Empty
                    else
                        _listState.value = ListFragment.State.Error(loadStateError.error)
                }
            }
        }

    }

    class Factory(private val repository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductsEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductsEditorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}