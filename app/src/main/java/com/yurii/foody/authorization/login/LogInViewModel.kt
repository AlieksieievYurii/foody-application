package com.yurii.foody.authorization.login

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.*
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.value
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
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
        object NavigateToChooseRoleScreen : Event()
        data class ServerError(val errorCode: Int) : Event()
        data class NetworkError(val message: String?) : Event()
        data class UnknownError(val message: String?) : Event()

        object NavigateToSingUpScreen : Event()
        object Close : Event()
        object NavigateToUserIsNotConfirmed : Event()
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
            repository.logIn(AuthData(emailField.value, passwordField.value)).onStart { _isLoading.value = true }
                .catch { exception ->
                    handleResponseError(exception)
                }.collect {
                    handleUser(it.userId)
                }
        }
    }

    private suspend fun handleUser(userId: Int) {
        repository.getUser(userId.toLong()).catch { exception ->
            handleResponseError(exception)
        }.collect { user ->
            if (user.isEmailConfirmed)
                handleUserRole(user.id)
            else {
                eventChannel.send(Event.NavigateToUserIsNotConfirmed)
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleUserRole(userId: Int) {
        repository.getUsersRoles(userId).catch { exception ->
            handleResponseError(exception)
        }.collect { userRolePagination ->
            val role = userRolePagination.results.first()
            repository.saveUserRole(role.role)
            repository.setUserRoleStatus(role.isConfirmed)
            _isLoading.value = false
            eventChannel.send(Event.NavigateToChooseRoleScreen)
        }
    }

    private suspend fun handleResponseError(error: Throwable) {
        _isLoading.value = false
        when (error) {
            is ResponseException.NetworkError -> eventChannel.send(Event.NetworkError(error.responseMessage))
            is ResponseException.ServerError -> {
                if (error.code == HTTP_BAD_REQUEST)
                    _emailValidation.value = FieldValidation.WrongCredentials
                else
                    eventChannel.send(Event.ServerError(error.code))
            }
            is ResponseException.UnknownError -> eventChannel.send(Event.UnknownError(error.responseMessage))
        }
    }

    fun onClickSingUp() = viewModelScope.launch { eventChannel.send(Event.NavigateToSingUpScreen) }

    fun onClose() = viewModelScope.launch { eventChannel.send(Event.Close) }

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
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}