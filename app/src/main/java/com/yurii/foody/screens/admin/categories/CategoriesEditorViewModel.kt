package com.yurii.foody.screens.admin.categories

import androidx.lifecycle.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class CategoriesEditorViewModel(private val productsRepository: ProductsRepository) : ViewModel() {
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

    val selectableMode = MutableStateFlow(false)

    private val _categories: MutableStateFlow<PagingData<CategoriesData>> = MutableStateFlow(PagingData.empty())
    val categories: StateFlow<PagingData<CategoriesData>> = _categories

    private val _listState: MutableLiveData<ListState> = MutableLiveData()
    val listState: LiveData<ListState> = _listState

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            productsRepository.getCategoriesPager().cachedIn(viewModelScope).collectLatest {
                _categories.value = it
            }
        }
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
                    _loading.value = false
                    val loadStateError = state.refresh as LoadState.Error
                    if (loadStateError.error is EmptyListException)
                        _listState.value = ListState.ShowEmptyList
                    else
                        _listState.value = ListState.ShowError(loadStateError.error)
                }
            }
        }

    }

    fun deleteItems(items: List<CategoriesData>) {
        viewModelScope.launch {
            _loading.value = true
            productsRepository.deleteCategories(items.map { it.id })
            _eventChannel.send(Event.Refresh)
        }
    }

    class Factory(private val repository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoriesEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoriesEditorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}