package com.yurii.foody.authorization.role

import androidx.lifecycle.*
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.utils.AuthorizationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChooseRoleViewModel(private val repository: AuthorizationRepository, private val selectNewRole: Boolean) : ViewModel() {
    sealed class Event {
        object NavigateToMainClientScreen : Event()
        object NavigateToMainExecutorScreen : Event()
        object NavigateToMainAdministratorScreen : Event()
        object NavigateToAuthenticationScreen : Event()
        object NavigateToUserRoleIsNotConfirmed : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val _showRoleOptions = MutableLiveData<UserRoleEnum>()
    val showRoleOptions: LiveData<UserRoleEnum> = _showRoleOptions

    init {
        viewModelScope.launch {
            val userRole = repository.getUserRole()!!
            if (selectNewRole)
                showRoleOptions(userRole)
            else {
                val selectedUserRole = repository.getSelectedUserRole()
                if (selectedUserRole == null) {
                    if (userRole == UserRoleEnum.CLIENT)
                        navigate(userRole)
                    else
                        showRoleOptions(userRole)
                } else {
                    if (repository.isUserRoleConfirmed())
                        navigate(selectedUserRole)
                    else
                        eventChannel.send(Event.NavigateToUserRoleIsNotConfirmed)
                }
            }
        }
    }

    fun onLogOut() {
        viewModelScope.launch {
            repository.logOut()
            eventChannel.send(Event.NavigateToAuthenticationScreen)
        }
    }

    fun onRoleSelected(userRoleEnum: UserRoleEnum) {
        viewModelScope.launch {
            repository.setSelectedUserRole(userRoleEnum)
            if (userRoleEnum != UserRoleEnum.CLIENT)
                if (repository.isUserRoleConfirmed())
                    navigate(userRoleEnum)
                else
                    eventChannel.send(Event.NavigateToUserRoleIsNotConfirmed)
            else
                navigate(userRoleEnum)
        }
    }

    private fun showRoleOptions(roleEnum: UserRoleEnum) {
        _showRoleOptions.value = roleEnum
    }

    private suspend fun navigate(userRoleEnum: UserRoleEnum) {
        when (userRoleEnum) {
            UserRoleEnum.CLIENT -> eventChannel.send(Event.NavigateToMainClientScreen)
            UserRoleEnum.EXECUTOR -> eventChannel.send(Event.NavigateToMainExecutorScreen)
            UserRoleEnum.ADMINISTRATOR -> eventChannel.send(Event.NavigateToMainAdministratorScreen)
        }
    }

    class Factory(private val repository: AuthorizationRepository, private val selectNewRole: Boolean) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseRoleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChooseRoleViewModel(repository, selectNewRole) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}