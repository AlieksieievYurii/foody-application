package com.yurii.foody.authorization.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.foody.api.ResponseException
import com.yurii.foody.api.User
import com.yurii.foody.utils.AuthorizationRepository
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.isInsideScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class LoadingViewModel(private val repository: AuthorizationRepository) : ViewModel() {
    sealed class Event {
        data class ServerError(val code: Int) : Event()
        data class NetworkError(val message: String?) : Event()
        data class UnknownError(val message: String?) : Event()
        object NavigateToChooseRoleScreen : Event()
        object NavigateToAuthenticationScreen : Event()
        object NavigateToUserIsNotConfirmedScreen : Event()
    }

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        viewModelScope.launch { handleResponseError(exception) }
    }

    private val viewModelJob = SupervisorJob()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)

    init {
        netWorkScope.launch {
            repository.getAuthenticationData()?.run {
                checkAuthorization(this)
            } ?: eventChannel.send(Event.NavigateToAuthenticationScreen)
        }
    }

    private suspend fun checkAuthorization(authData: AuthDataStorage.Data) {
        repository.setToken(authData.token)
        netWorkScope.launch {
            val user = repository.getUser(authData.userId)
            repository.saveUser(user)
            checkEmailConfirmation(user)
        }
    }

    private suspend fun handleResponseError(error: Throwable) {
        when (error) {
            is ResponseException.NetworkError -> eventChannel.send(Event.NetworkError(error.responseMessage))
            is ResponseException.ServerError -> handleServerError(error.code)
            is ResponseException.UnknownError -> eventChannel.send(Event.UnknownError(error.responseMessage))
        }
    }

    private suspend fun handleServerError(errorCode: Int) {
        if (errorCode == HTTP_UNAUTHORIZED) {
            repository.logOut()
            eventChannel.send(Event.NavigateToAuthenticationScreen)
        } else
            eventChannel.send(Event.ServerError(errorCode))
    }

    private suspend fun checkEmailConfirmation(user: User) {
        if (user.isEmailConfirmed)
            fetchUserRole()
        else
            eventChannel.send(Event.NavigateToUserIsNotConfirmedScreen)
    }

    private suspend fun fetchUserRole() {
        val currentUserRole = repository.getCurrentUserRole()
        repository.setUserRoleStatus(currentUserRole.isConfirmed)
        repository.setUserRole(currentUserRole.role)
        repository.getSelectedUserRole()?.run {
            if (!isInsideScope(currentUserRole.role))
                repository.setSelectedUserRole(null)
        }
        eventChannel.send(Event.NavigateToChooseRoleScreen)
    }

    class Factory(private val repository: AuthorizationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoadingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoadingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}