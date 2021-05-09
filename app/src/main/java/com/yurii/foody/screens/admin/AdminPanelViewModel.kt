package com.yurii.foody.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AdminPanelViewModel(private val repository: AuthorizationRepositoryInterface) : ViewModel() {
    sealed class Event {
        data class SetHeaderInformation(val name: String, val surName: String) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getSavedUser()?.run {
                eventChannel.send(Event.SetHeaderInformation(name = this.firstName, surName = this.lastName))
            }
        }
    }

    class Factory(private val repository: AuthorizationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminPanelViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminPanelViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}