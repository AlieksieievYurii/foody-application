package com.yurii.foody.screens.admin.categories

import androidx.lifecycle.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class CategoriesEditorViewModel(private val productsRepository: ProductsRepository) : ViewModel() {
    sealed class Event {
        object Refresh : Event()
        object ShowItemsRemovedSnackBar : Event()
    }

    val selectableMode = MutableStateFlow(false)

    private val _categories: MutableStateFlow<PagingData<CategoriesData>> = MutableStateFlow(PagingData.empty())
    val categories: StateFlow<PagingData<CategoriesData>> = _categories

    private val _listState: MutableLiveData<ListFragment.State> = MutableLiveData(ListFragment.State.Loading)
    val listState: LiveData<ListFragment.State> = _listState

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

    private var isRefreshing = false

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

    fun refreshList() {
        viewModelScope.launch {
            isRefreshing = true
            _eventChannel.send(Event.Refresh)
        }
    }

    fun deleteItems(items: List<CategoriesData>) {
        viewModelScope.launch {
            _loading.value = true
            productsRepository.deleteCategories(items.map { it.id })
            refreshList()
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