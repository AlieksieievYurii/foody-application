package com.yurii.foody.authorization.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurii.foody.authorization.AuthorizationRepository

class SignUpViewModel(private val repository: AuthorizationRepository) : ViewModel() {

    class Factory(private val repository: AuthorizationRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SignUpViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}