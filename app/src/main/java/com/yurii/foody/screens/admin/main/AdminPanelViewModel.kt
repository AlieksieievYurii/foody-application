package com.yurii.foody.screens.admin.main

import androidx.lifecycle.*
import com.yurii.foody.api.User
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AdminPanelViewModel(private val repository: AuthorizationRepositoryInterface) : ViewModel() {
    sealed class Event {
        object NavigateToRequests : Event()
        object NavigateToProductsEditor : Event()
        object NavigateToCategoriesEditor : Event()
        object NavigateToLogInScreen : Event()
        object NavigateToChangeRole : Event()
    }

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User> = _user

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

    fun changeRole() {
        viewModelScope.launch {

            repository.setSelectedUserRole(null)
            eventChannel.send(Event.NavigateToChangeRole)
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