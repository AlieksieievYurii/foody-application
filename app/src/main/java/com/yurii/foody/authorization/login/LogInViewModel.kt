package com.yurii.foody.authorization.login

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.haroldadmin.cnradapter.NetworkResponse
import com.yurii.foody.api.AuthData
import com.yurii.foody.api.AuthResponseData
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.toAuthDataStorage
import com.yurii.foody.utils.value
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection.HTTP_BAD_REQUEST

sealed class FieldValidation {
    object None : FieldValidation()
    object EmptyField : FieldValidation()
    object WrongCredentials : FieldValidation()
}

class LogInViewModel(private val repository: AuthorizationRepository) : ViewModel() {
    sealed class Event {
        data class Authenticated(val data: AuthDataStorage.Data) : Event()
        data class ServerError(val errorCode: Int) : Event()
        data class NetworkError(val message: String?) : Event()
        data class UnknownError(val message: String?) : Event()
    }

    val emailField = ObservableField(String.Empty)
    val passwordField = ObservableField(String.Empty)

    private val _emailValidation = MutableLiveData<FieldValidation>()
    val emailValidation: LiveData<FieldValidation> = _emailValidation

    private val _passwordValidation = MutableLiveData<FieldValidation>()
    val passwordValidation: LiveData<FieldValidation> = _passwordValidation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()


    fun logIn() {
        if (isDataValidated())
            performLogIn()
    }

    private fun isDataValidated(): Boolean {
        if (emailField.value.isEmpty()) {
            _emailValidation.value = FieldValidation.EmptyField
            return false
        }

        if (passwordField.value.isEmpty()) {
            _passwordValidation.value = FieldValidation.EmptyField
            return false
        }

        return true
    }

    private fun performLogIn() {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.logIn(AuthData(emailField.value, passwordField.value))
            handleResponse(response)
            _isLoading.value = false
        }

    }

    private fun handleResponse(response: NetworkResponse<AuthResponseData, Unit>) {
        viewModelScope.launch {
            when (response) {
                is NetworkResponse.Success -> eventChannel.send(Event.Authenticated(response.body.toAuthDataStorage()))
                is NetworkResponse.ServerError -> {
                    if (response.code == HTTP_BAD_REQUEST)
                        _emailValidation.value = FieldValidation.WrongCredentials
                    else
                        eventChannel.send(Event.ServerError(response.code))
                }
                is NetworkResponse.NetworkError -> eventChannel.send(Event.NetworkError(response.error.message))
                is NetworkResponse.UnknownError -> eventChannel.send(Event.UnknownError(response.error.message))
            }
        }

    }

    fun resetEmailValidation() {
        if (_emailValidation.value != FieldValidation.None)
            _emailValidation.value = FieldValidation.None
    }

    fun resetPasswordValidation() {
        if (_passwordValidation.value != FieldValidation.None)
            _passwordValidation.value = FieldValidation.None
    }

    class Factory(private val repository: AuthorizationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogInViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LogInViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}