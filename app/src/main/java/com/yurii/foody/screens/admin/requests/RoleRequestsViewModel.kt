package com.yurii.foody.screens.admin.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoleRequestsViewModel(private val userRoleRepository: UserRoleRepository) : ViewModel() {

    private val _userRolesRequests: MutableStateFlow<PagingData<UserRoleRequest>> = MutableStateFlow(PagingData.empty())
    val userRolesRequests: StateFlow<PagingData<UserRoleRequest>> = _userRolesRequests

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