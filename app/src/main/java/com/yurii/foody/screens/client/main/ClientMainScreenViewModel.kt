package com.yurii.foody.screens.client.main

import androidx.lifecycle.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yurii.foody.api.User
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.EmptyListException
import com.yurii.foody.utils.ProductsRepository
import com.yurii.foody.utils.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClientMainScreenViewModel(private val userRepository: UserRepository, private val productsRepository: ProductsRepository) : ViewModel() {
    companion object {
        private const val REFRESH_HISTORY_INTERVAL_MS = 5000L
    }

    sealed class Event {
        object NavigateToLogInScreen : Event()
        object ShowDialogToBecomeCook : Event()
        object ShowDialogYouBecameCook : Event()
        object RefreshHistoryAndPendingItemsList : Event()
    }

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user
    private var isRefreshing = false

    val role: Flow<UserRoleEnum?> = userRepository.getUserRoleFlow()

    private val _historyAndPendingItems: MutableStateFlow<PagingData<Item>> = MutableStateFlow(PagingData.empty())
    val historyAndPendingItems: StateFlow<PagingData<Item>> = _historyAndPendingItems

    private val _historyAndPendingItemsListState: MutableLiveData<ListFragment.State> = MutableLiveData(ListFragment.State.Loading)
    val historyAndPendingItemsListState: LiveData<ListFragment.State> = _historyAndPendingItemsListState

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()


    init {
        viewModelScope.launch {
            userRepository.getSavedUser()?.run {
                _user.value = this
            }
        }
        initHistoryAndPendingItemsPager()
        startUpdatingHistoryAndPendingItems()
    }

    private fun initHistoryAndPendingItemsPager() = viewModelScope.launch(Dispatchers.IO) {
        productsRepository.getHistoryAndPendingItemsPager().cachedIn(viewModelScope).collectLatest {
            _historyAndPendingItems.value = it
        }
    }

    private fun startUpdatingHistoryAndPendingItems() = viewModelScope.launch {
        while (true) {
            delay(REFRESH_HISTORY_INTERVAL_MS)
            isRefreshing = true
            eventChannel.send(Event.RefreshHistoryAndPendingItemsList)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            userRepository.logOut()
            eventChannel.send(Event.NavigateToLogInScreen)
        }
    }

    fun requestToBecomeCook() {
        viewModelScope.launch {
            eventChannel.send(Event.ShowDialogToBecomeCook)
        }
    }

    fun becomeCook() {
        viewModelScope.launch {
            userRepository.becomeCook()
            eventChannel.send(Event.ShowDialogYouBecameCook)
            userRepository.setUserRole(UserRoleEnum.EXECUTOR)
            userRepository.setUserRoleStatus(false)
        }
    }

    fun refreshHistoryAndPendingItemsList() {
        viewModelScope.launch {
            isRefreshing = true
            eventChannel.send(Event.RefreshHistoryAndPendingItemsList)
        }
    }

    fun onLoadStateChangeHistoryAndPendingItems(state: CombinedLoadStates) {
        viewModelScope.launch {
            when (state.refresh) {
                is LoadState.NotLoading -> {
                    isRefreshing = false
                    _historyAndPendingItemsListState.value = ListFragment.State.Ready
                }
                LoadState.Loading ->
                    if (!isRefreshing)
                        _historyAndPendingItemsListState.value = ListFragment.State.Loading
                is LoadState.Error -> {
                    val loadStateError = state.refresh as LoadState.Error
                    if (loadStateError.error is EmptyListException)
                        _historyAndPendingItemsListState.value = ListFragment.State.Empty
                    else
                        _historyAndPendingItemsListState.value = ListFragment.State.Error(loadStateError.error)
                }
            }
        }

    }

    fun giveFeedback(historyItem: Item.HistoryItem, rating: Int) {
        viewModelScope.launch {
            productsRepository.giveProductRating(productId = historyItem.product!!.id, rating)
            eventChannel.send(Event.RefreshHistoryAndPendingItemsList)
        }
    }

    class Factory(private val userRepository: UserRepository, private val productsRepository: ProductsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClientMainScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClientMainScreenViewModel(userRepository, productsRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}