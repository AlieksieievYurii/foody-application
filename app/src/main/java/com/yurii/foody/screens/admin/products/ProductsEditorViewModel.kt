package com.yurii.foody.screens.admin.products

import androidx.lifecycle.*
import androidx.paging.*
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class ProductsEditorViewModel(private val repository: ProductsRepository) : ViewModel() {
    sealed class ListState {
        object ShowEmptyList : ListState()
        object ShowLoading : ListState()
        object ShowResult : ListState()
        data class ShowError(val exception: Throwable) : ListState()
    }

    sealed class Event {
        object Refresh : Event()
        object ShowItemsRemovedSnackBar : Event()
    }

    private val _products: MutableStateFlow<PagingData<ProductData>> = MutableStateFlow(PagingData.empty())
    val products: StateFlow<PagingData<ProductData>> = _products

    private val _listState: MutableLiveData<ListState> = MutableLiveData()
    val listState: LiveData<ListState> = _listState

    private var searchJob: Job? = null

    private val query = ProductPagingSource.Query()

    val selectableMode = MutableStateFlow(false)

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

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
                    _listState.value = ListState.ShowResult
                    if (_loading.value) {
                        _loading.value = false
                        selectableMode.value = false
                        _eventChannel.send(Event.ShowItemsRemovedSnackBar)
                    }
                }
                LoadState.Loading -> _listState.setValue(ListState.ShowLoading)
                is LoadState.Error -> {
                    val loadStateError = state.refresh as LoadState.Error
                    if (loadStateError.error is EmptyListException)
                        _listState.value = ListState.ShowEmptyList
                    else
                        _listState.value = ListState.ShowError(loadStateError.error)
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