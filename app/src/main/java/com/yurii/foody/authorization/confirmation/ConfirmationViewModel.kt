package com.yurii.foody.authorization.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.foody.authorization.AuthorizationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ConfirmationViewModel(private val repository: AuthorizationRepository, private val mode: ConfirmationFragment.Mode) : ViewModel() {
    sealed class Event {
        object ShowUserIsNotConfirmed : Event()
        object ShowRoleIsNotConfirmed : Event()
        object NavigateToAuthorizationFragment : Event()
        object NavigateToChoosingRoleScreen : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            eventChannel.send(
                when (mode) {
                    ConfirmationFragment.Mode.EMAIL_IS_NOT_CONFIRMED -> Event.ShowUserIsNotConfirmed
                    ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED -> Event.ShowRoleIsNotConfirmed
                }
            )
        }
    }

    fun onLogOut() {
        viewModelScope.launch {
            repository.clearUserAuth()
            eventChannel.send(Event.NavigateToAuthorizationFragment)
        }
    }

    fun onChangeRole() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToChoosingRoleScreen) }
    }

    class Factory(private val repository: AuthorizationRepository, private val mode: ConfirmationFragment.Mode) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConfirmationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConfirmationViewModel(repository, mode) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}