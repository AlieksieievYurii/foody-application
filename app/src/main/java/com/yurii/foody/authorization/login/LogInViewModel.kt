package com.yurii.foody.authorization.login

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.*
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import com.yurii.foody.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class LogInViewModel(private val repository: AuthorizationRepositoryInterface) : ViewModel() {
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

    private val _emailValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val emailValidation: LiveData<FieldValidation> = _emailValidation

    private val _passwordValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val passwordValidation: LiveData<FieldValidation> = _passwordValidation

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
        } else if (emailField.value.notMatches(EMAIL_REGEX)) {
            _emailValidation.value = FieldValidation.WrongEmailFormat
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
            withContext(Dispatchers.IO) {
                repository.logIn(AuthData(emailField.value, passwordField.value)).onStart { _isLoading.value = true }
                    .catch { exception ->
                        handleResponseError(exception)
                    }.collect {
                        handleUser(it.userId)
                    }
            }
        }
    }

    private suspend fun handleUser(userId: Long) {
        repository.getUser(userId.toLong()).catch { exception ->
            handleResponseError(exception)
        }.collect { user ->
            repository.saveUser(user)
            if (user.isEmailConfirmed)
                handleUserRole(user.id)
            else {
                eventChannel.send(Event.NavigateToUserIsNotConfirmed)
                _isLoading.value = false
            }
        }
    }

    private suspend fun handleUserRole(userId: Long) {
        repository.getUsersRoles(userId).catch { exception ->
            handleResponseError(exception, isAuthenticated = true)
        }.collect { userRolePagination ->
            val role = userRolePagination.results.first()
            repository.setUserRole(role.role)
            repository.setUserRoleStatus(role.isConfirmed)
            _isLoading.value = false
            eventChannel.send(Event.NavigateToChooseRoleScreen)
        }
    }

    private suspend fun handleResponseError(error: Throwable, isAuthenticated: Boolean = false) {
        _isLoading.value = false
        when (error) {
            is ResponseException.NetworkError -> eventChannel.send(Event.NetworkError(error.responseMessage))
            is ResponseException.ServerError -> {
                if (isAuthenticated && error.code == HTTP_UNAUTHORIZED || error.code == HTTP_BAD_REQUEST)
                    _emailValidation.postValue(FieldValidation.WrongCredentials)
                else
                    eventChannel.send(Event.ServerError(error.code))
            }
            is ResponseException.UnknownError -> eventChannel.send(Event.UnknownError(error.responseMessage))
        }
    }

    fun onClickSingUp() = viewModelScope.launch { eventChannel.send(Event.NavigateToSingUpScreen) }

    fun onClose() = viewModelScope.launch { eventChannel.send(Event.Close) }

    fun resetEmailValidation() {
        if (_emailValidation.value != FieldValidation.NoErrors)
            _emailValidation.value = FieldValidation.NoErrors
    }

    fun resetPasswordValidation() {
        if (_passwordValidation.value != FieldValidation.NoErrors)
            _passwordValidation.value = FieldValidation.NoErrors
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