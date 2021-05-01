package com.yurii.foody.authorization.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.yurii.foody.api.User
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.utils.AuthDataStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class LoadingViewModel(private val repository: AuthorizationRepository) : ViewModel() {
    sealed class Event {
        data class NavigateToChooseRoleScreen(val role: UserRoleEnum, val isRoleConfirmed: Boolean) : Event()
        data class ServerError(val code: Int) : Event()
        data class NetworkError(val message: String?) : Event()
        data class UnknownError(val message: String?) : Event()
        object NavigateToAuthenticationScreen : Event()
        object NavigateToUserIsNotConfirmedScreen : Event()
    }

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getLogInAuthenticatedDataFlow().catch {
                eventChannel.send(Event.NavigateToAuthenticationScreen)
            }.collect {
                checkAuthorization(it)
            }
        }
    }

    private suspend fun checkAuthorization(authData: AuthDataStorage.Data) {
        repository.setToken(authData.token)
        when (val response = repository.getUser(authData.userId.toLong())) {
            is NetworkResponse.Success -> checkEmailConfirmation(response.body, authData)
            is NetworkResponse.ServerError -> eventChannel.send(
                if (response.code == HTTP_UNAUTHORIZED) Event.NavigateToAuthenticationScreen
                else Event.ServerError(response.code)
            )
            is NetworkResponse.NetworkError -> eventChannel.send(Event.NetworkError(response.error.message))
            is NetworkResponse.UnknownError -> eventChannel.send(Event.UnknownError(response.error.message))
        }
    }

    private suspend fun checkEmailConfirmation(user: User, authData: AuthDataStorage.Data) {
        if (user.isEmailConfirmed)
            checkUserRoleConfirmation(authData)
        else
            eventChannel.send(Event.NavigateToUserIsNotConfirmedScreen)
    }

    private suspend fun checkUserRoleConfirmation(authData: AuthDataStorage.Data) {
        when (val response = repository.getUsersRoles(userId = authData.userId)) {
            is NetworkResponse.Success -> response.body.results.first().run {
                eventChannel.send(Event.NavigateToChooseRoleScreen(role, isConfirmed))
            }
            is NetworkResponse.ServerError -> eventChannel.send(Event.ServerError(response.code))
            is NetworkResponse.NetworkError -> eventChannel.send(Event.NetworkError(response.error.message))
            is NetworkResponse.UnknownError -> eventChannel.send(Event.UnknownError(response.error.message))
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