package com.yurii.foody.screens.client.main

import androidx.lifecycle.*
import com.yurii.foody.api.User
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.utils.AuthorizationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ClientMainScreenViewModel(private val repository: AuthorizationRepository) : ViewModel() {

    sealed class Event {
        object NavigateToLogInScreen : Event()
        object ShowDialogToBecomeCook : Event()
        object ShowDialogYouBecameCook : Event()
    }

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

    val role: Flow<UserRoleEnum?> = repository.getUserRoleFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getSavedUser()?.run {
                _user.value = this
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logOut()
            eventChannel.send(Event.NavigateToLogInScreen)
        }
    }

    fun requestToBecomeCook() {
        viewModelScope.launch {
            eventChannel.send(Event.ShowDialogToBecomeCook)
        }
    }

    fun becomeCook() {
        viewModelScope.launch {
            repository.becomeCook()
            eventChannel.send(Event.ShowDialogYouBecameCook)
            repository.setUserRole(UserRoleEnum.EXECUTOR)
            repository.setUserRoleStatus(false)
        }
    }

    class Factory(private val repository: AuthorizationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClientMainScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClientMainScreenViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}