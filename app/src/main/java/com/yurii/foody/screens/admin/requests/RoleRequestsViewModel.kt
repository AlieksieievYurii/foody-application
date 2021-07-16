package com.yurii.foody.screens.admin.requests

import androidx.lifecycle.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yurii.foody.utils.AuthorizationRepository
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.EmptyListException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class RoleRequestsViewModel(private val authorizationRepository: AuthorizationRepository) : ViewModel() {
    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object RefreshList : Event()
    }

    private val _userRolesRequests: MutableStateFlow<PagingData<UserRoleRequest>> = MutableStateFlow(PagingData.empty())
    val userRolesRequests: StateFlow<PagingData<UserRoleRequest>> = _userRolesRequests

    private val _listState: MutableLiveData<ListFragment.State> = MutableLiveData(ListFragment.State.Loading)
    val listState: LiveData<ListFragment.State> = _listState

    private var isRefreshing = false

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = _eventChannel.receiveAsFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch {
            _eventChannel.send(Event.ShowError(exception))
        }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    init {
        loadUserRolesRequests()
    }

    private fun loadUserRolesRequests() {
        netWorkScope.launch {
            authorizationRepository.getUnconfirmedUserRolesPager().cachedIn(viewModelScope).collectLatest {
                _userRolesRequests.value = it
            }
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

    fun acceptRoleRequest(roleRequest: UserRoleRequest) {
        netWorkScope.launch {
            authorizationRepository.confirmUserRole(roleRequest)
            refreshList()
        }
    }

    fun refreshList() {
        viewModelScope.launch {
            isRefreshing = true
            _eventChannel.send(Event.RefreshList)
        }

    }

    class Factory(private val authorizationRepository: AuthorizationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoleRequestsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RoleRequestsViewModel(authorizationRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}