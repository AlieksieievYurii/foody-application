package com.yurii.foody.screens.admin.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoleRequestsViewModel : ViewModel() {

    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoleRequestsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RoleRequestsViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}