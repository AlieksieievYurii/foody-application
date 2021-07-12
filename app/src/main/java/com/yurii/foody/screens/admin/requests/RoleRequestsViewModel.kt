package com.yurii.foody.screens.admin.requests

import androidx.lifecycle.*
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yurii.foody.utils.EmptyListException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoleRequestsViewModel(private val userRoleRepository: UserRoleRepository) : ViewModel() {
    sealed class ListState {
        object ShowEmptyList : ListState()
        object ShowLoading : ListState()
        object ShowResult : ListState()
        data class ShowError(val exception: Throwable) : ListState()
    }
    private val _userRolesRequests: MutableStateFlow<PagingData<UserRoleRequest>> = MutableStateFlow(PagingData.empty())
    val userRolesRequests: StateFlow<PagingData<UserRoleRequest>> = _userRolesRequests

    private val _listState: MutableStateFlow<ListState> = MutableStateFlow(ListState.ShowLoading)
    val listState: StateFlow<ListState> = _listState

    init {
        loadUserRolesRequests()
    }

    private fun loadUserRolesRequests() {
        viewModelScope.launch {
            userRoleRepository.getUnconfirmedUserRolesPager().cachedIn(viewModelScope).collectLatest {
                _userRolesRequests.value = it
            }
        }
    }

    fun onLoadStateChange(state: CombinedLoadStates) {
        viewModelScope.launch {
            when (state.refresh) {
                is LoadState.NotLoading -> _listState.value = ListState.ShowResult
                LoadState.Loading -> _listState.value = ListState.ShowLoading
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

    fun acceptRoleRequest(it: UserRoleRequest) {

    }

    class Factory(private val userRoleRepository: UserRoleRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoleRequestsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RoleRequestsViewModel(userRoleRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}