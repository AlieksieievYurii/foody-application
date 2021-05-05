package com.yurii.foody.authorization.confirmation

import androidx.lifecycle.*
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ConfirmationViewModel(private val repository: AuthorizationRepositoryInterface, mode: ConfirmationFragment.Mode) : ViewModel() {
    sealed class Event {
        object NavigateToAuthorizationFragment : Event()
        object NavigateToChoosingRoleScreen : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val _showMessage = MutableStateFlow(mode)
    val showMessage: StateFlow<ConfirmationFragment.Mode> = _showMessage


    fun onLogOut() {
        viewModelScope.launch {
            repository.logOut()
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