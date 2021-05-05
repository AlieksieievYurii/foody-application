package com.yurii.foody.authorization.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.foody.api.ResponseException
import com.yurii.foody.api.User
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.isInsideScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class LoadingViewModel(private val repository: AuthorizationRepositoryInterface) : ViewModel() {
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

    init {
        viewModelScope.launch {
            repository.getAuthenticationData()?.run {
                checkAuthorization(this)
            } ?: eventChannel.send(Event.NavigateToAuthenticationScreen)
        }
    }

    private suspend fun checkAuthorization(authData: AuthDataStorage.Data) {
        repository.setToken(authData.token)
        withContext(Dispatchers.IO) {
            repository.getUser(authData.userId.toLong()).catch { exception -> handleResponseError(exception) }.collect { user ->
                checkEmailConfirmation(user)
            }
        }
    }

    private suspend fun handleResponseError(error: Throwable) {
        when (error) {
            is ResponseException.NetworkError -> handleNetworkError(error.responseMessage)
            is ResponseException.ServerError -> handleServerError(error.code)
            is ResponseException.UnknownError -> handleUnknownError(error.responseMessage)
        }
    }

    private suspend fun handleServerError(errorCode: Int) {
        if (errorCode == HTTP_UNAUTHORIZED) {
            repository.logOut()
            eventChannel.send(Event.NavigateToAuthenticationScreen)
        } else
            eventChannel.send(Event.ServerError(errorCode))
    }

    private suspend fun handleNetworkError(message: String?) {
        eventChannel.send(Event.NetworkError(message))
    }

    private suspend fun handleUnknownError(message: String?) {
        eventChannel.send(Event.UnknownError(message))
    }

    private suspend fun checkEmailConfirmation(user: User) {
        if (user.isEmailConfirmed)
            fetchUserRole(user)
        else
            eventChannel.send(Event.NavigateToUserIsNotConfirmedScreen)
    }

    private suspend fun fetchUserRole(user: User) {
        repository.getUsersRoles(user.id).catch { exception -> handleResponseError(exception) }.collect {
            val roleResponse = it.results.first()
            repository.setUserRoleStatus(roleResponse.isConfirmed)
            repository.setUserRole(roleResponse.role)
            repository.getSelectedUserRole()?.run {
                if (!isInsideScope(roleResponse.role))
                    repository.setSelectedUserRole(null)
            }
            eventChannel.send(Event.NavigateToChooseRoleScreen)
        }
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