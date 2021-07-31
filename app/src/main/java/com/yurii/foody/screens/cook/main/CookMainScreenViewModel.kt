package com.yurii.foody.screens.cook.main

import androidx.lifecycle.*
import com.yurii.foody.api.User
import com.yurii.foody.utils.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CookMainScreenViewModel(private val repository: UserRepository) : ViewModel() {
    sealed class Event {
        object NavigateToLogInScreen : Event()
        object NavigateToChangeRole : Event()
        object NavigateToPersonalInformation : Event()
    }

    val user: LiveData<User> = liveData {
        repository.getSavedUser()?.run {
            emit(this)
        }
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()

    fun logOut() {
        viewModelScope.launch {
            repository.logOut()
            eventChannel.send(Event.NavigateToLogInScreen)
        }
    }

    fun changeRole() {
        viewModelScope.launch {
            repository.setSelectedUserRole(null)
            eventChannel.send(Event.NavigateToChangeRole)
        }
    }

    fun changePersonalInformation() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToPersonalInformation)
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CookMainScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CookMainScreenViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}