package com.yurii.foody.screens.admin.main

import androidx.lifecycle.*
import com.yurii.foody.api.User
import com.yurii.foody.utils.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AdminPanelViewModel(private val repository: UserRepository) : ViewModel() {
    sealed class Event {
        object NavigateToRequests : Event()
        object NavigateToProductsEditor : Event()
        object NavigateToCategoriesEditor : Event()
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

    fun onClickRequests() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToRequests) }
    }

    fun onClickProductEditor() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToProductsEditor) }
    }

    fun onClickCategoriesEditor() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToCategoriesEditor) }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminPanelViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminPanelViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}