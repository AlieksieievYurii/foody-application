package com.yurii.foody.authorization.login

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.api.*
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import com.yurii.foody.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    val emailValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val passwordValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch { handleResponseError(exception) }
    }

    private val viewModelJob = Job()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    fun logIn() {
        if (isDataValidated())
            performLogIn()
    }

    private fun isDataValidated(): Boolean {
        if (emailField.value.isNullOrBlank()) {
            emailValidation.value = FieldValidation.EmptyField
            return false
        } else if (emailField.value.notMatches(EMAIL_REGEX)) {
            emailValidation.value = FieldValidation.WrongEmailFormat
            return false
        }

        if (passwordField.value.isNullOrBlank()) {
            passwordValidation.value = FieldValidation.EmptyField
            return false
        }

        return true
    }

    private fun performLogIn() {
        netWorkScope.launch {
            _isLoading.value = true
            val authData = repository.logIn(AuthData(emailField.value, passwordField.value))
            handleUser(authData.userId)
        }

    }

    private suspend fun handleUser(userId: Long) {
        val user = repository.getUser(userId)
        repository.saveUser(user)
        if (user.isEmailConfirmed)
            handleUserRole()
        else {
            eventChannel.send(Event.NavigateToUserIsNotConfirmed)
            _isLoading.value = false
        }
    }

    private suspend fun handleUserRole() {
        val currentUserRole: UserRole? = try {
            repository.getCurrentUserRole()
        } catch (exception: ResponseException) {
            handleResponseError(exception, isAuthenticated = true)
            null
        }

        if (currentUserRole != null) {
            repository.setUserRole(currentUserRole.role)
            repository.setUserRoleStatus(currentUserRole.isConfirmed)
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
                    emailValidation.postValue(FieldValidation.WrongCredentials)
                else
                    eventChannel.send(Event.ServerError(error.code))
            }
            is ResponseException.UnknownError -> eventChannel.send(Event.UnknownError(error.responseMessage))
        }
    }

    fun onClickSingUp() = viewModelScope.launch { eventChannel.send(Event.NavigateToSingUpScreen) }

    fun onClose() = viewModelScope.launch { eventChannel.send(Event.Close) }

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