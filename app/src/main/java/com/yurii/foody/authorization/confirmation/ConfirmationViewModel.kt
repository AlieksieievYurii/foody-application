package com.yurii.foody.authorization.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.AuthorizationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    private val repository: AuthorizationRepository,
    private val userIsNotConfirmed: Boolean,
    private val userRoleEnum: UserRoleEnum
) : ViewModel() {
    sealed class Event {
        object ShowUserIsNotConfirmed : Event()
        data class ShowRoleIsNotConfirmed(val roleEnum: UserRoleEnum) : Event()
        object NavigateToAuthorizationFragment : Event()
        data class NavigateToChoosingRoleFragment(val roleEnum: UserRoleEnum) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (userIsNotConfirmed)
                eventChannel.send(Event.ShowUserIsNotConfirmed)
            else
                eventChannel.send(Event.ShowRoleIsNotConfirmed(userRoleEnum))
        }
    }

    fun onLogOut() {
        viewModelScope.launch {
            repository.clearUserAuth()
            eventChannel.send(Event.NavigateToAuthorizationFragment)
        }
    }

    fun onChangeRole() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToChoosingRoleFragment(userRoleEnum)) }
    }

    class Factory(private val repository: AuthorizationRepository, private val userIsNotConfirmed: Boolean, private val userRoleEnum: UserRoleEnum) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConfirmationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConfirmationViewModel(repository, userIsNotConfirmed, userRoleEnum) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}