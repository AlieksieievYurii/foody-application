package com.yurii.foody.screens.client.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ClientMainScreenViewModel : ViewModel() {

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClientMainScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClientMainScreenViewModel() as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}