package com.yurii.foody.screens.admin.products

import androidx.lifecycle.*
import androidx.paging.*
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductsEditorViewModel(private val repository: ProductsRepository) : ViewModel() {
    sealed class ListState {
        object ShowEmptyList : ListState()
        object ShowLoading : ListState()
        object ShowResult : ListState()
        data class ShowError(val exception: Throwable) : ListState()
    }

    private val _products: MutableStateFlow<PagingData<ProductData>> = MutableStateFlow(PagingData.empty())
    val products: StateFlow<PagingData<ProductData>> = _products

    private val _listState: MutableLiveData<ListState> = MutableLiveData()
    val listState: LiveData<ListState> = _listState

    private var searchJob: Job? = null

    private val query = ProductPagingSource.Query()

    val selectableMode = MutableStateFlow(false)

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
                is LoadState.NotLoading -> _listState.setValue(ListState.ShowResult)
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