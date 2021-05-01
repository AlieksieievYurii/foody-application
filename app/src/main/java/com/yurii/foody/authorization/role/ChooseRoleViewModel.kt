package com.yurii.foody.authorization.role

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.AuthorizationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ChooseRoleViewModel(
    private val repository: AuthorizationRepository,
    private val userRoleEnum: UserRoleEnum,
    private val selectNewRole: Boolean,
    private val isRoleConfirmed: Boolean
) : ViewModel() {
    sealed class Event {
        object NavigateToMainClientScreen : Event()
        object NavigateToMainExecutorScreen : Event()
        object NavigateToMainAdministratorScreen : Event()
        object NavigateToAuthenticationScreen : Event()
        data class NavigateToUserRoleIsNotConfirmed(val roleEnum: UserRoleEnum) : Event()
        data class ShowRoleOptions(val userRole: UserRoleEnum) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (selectNewRole)
                showRoleOptions()
            else {
                repository.getSelectedUserRoleFlow().catch {
                    if (userRoleEnum == UserRoleEnum.CLIENT)
                        navigate(UserRoleEnum.CLIENT)
                    else
                        showRoleOptions()
                }.collect { lastSelectedRole ->
                    if (lastSelectedRole == UserRoleEnum.CLIENT)
                        navigate(lastSelectedRole)
                    else if (lastSelectedRole == UserRoleEnum.EXECUTOR && userRoleEnum in listOf(UserRoleEnum.EXECUTOR, UserRoleEnum.ADMINISTRATOR))
                        navigateIfConfirmed(lastSelectedRole)
                    else if (lastSelectedRole == UserRoleEnum.ADMINISTRATOR && userRoleEnum == UserRoleEnum.ADMINISTRATOR)
                        navigateIfConfirmed(lastSelectedRole)
                    else
                        showRoleOptions()
                }
            }
        }
    }

    private suspend fun navigateIfConfirmed(roleEnum: UserRoleEnum) {
        if (isRoleConfirmed)
            navigate(roleEnum)
        else
            eventChannel.send(Event.NavigateToUserRoleIsNotConfirmed(roleEnum))
    }

    fun onLogOut() {
        viewModelScope.launch {
            repository.clearUserAuth()
            eventChannel.send(Event.NavigateToAuthenticationScreen)
        }
    }

    fun onRoleSelected(userRoleEnum: UserRoleEnum) {
        viewModelScope.launch {
            repository.saveUserRole(userRoleEnum)
            navigate(userRoleEnum)
        }
    }

    private suspend fun showRoleOptions() = eventChannel.send(Event.ShowRoleOptions(userRoleEnum))

    private suspend fun navigate(userRoleEnum: UserRoleEnum) {
        when (userRoleEnum) {
            UserRoleEnum.CLIENT -> eventChannel.send(Event.NavigateToMainClientScreen)
            UserRoleEnum.EXECUTOR -> eventChannel.send(Event.NavigateToMainExecutorScreen)
            UserRoleEnum.ADMINISTRATOR -> eventChannel.send(Event.NavigateToMainAdministratorScreen)
        }
    }

    class Factory(
        private val repository: AuthorizationRepository,
        private val userRoleEnum: UserRoleEnum,
        private val selectNewRole: Boolean,
        private val isRoleConfirmed: Boolean
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChooseRoleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChooseRoleViewModel(repository, userRoleEnum, selectNewRole, isRoleConfirmed) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}