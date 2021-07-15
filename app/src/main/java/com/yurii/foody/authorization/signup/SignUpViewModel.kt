package com.yurii.foody.authorization.signup

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.beust.klaxon.JsonObject
import com.yurii.foody.api.ResponseException
import com.yurii.foody.api.RegistrationForm
import com.yurii.foody.api.UserRegistration
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.AuthorizationRepositoryInterface
import com.yurii.foody.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection.HTTP_BAD_REQUEST

class SignUpViewModel(private val repository: AuthorizationRepositoryInterface) : ViewModel() {
    sealed class Event {
        object NavigateToLogInScreen : Event()
        object CloseScreen : Event()
        object ShowInfoAboutCook : Event()
        data class ShowRegistrationHasDoneDialog(val email: String, val userRoleEnum: UserRoleEnum)
        data class ShowErrorDialog(val message: String) : Event()
    }

    var isPasswordSuitable: Boolean = false

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showRegistrationHasDodeDialog: MutableStateFlow<Event.ShowRegistrationHasDoneDialog?> = MutableStateFlow(null)
    val showRegistrationHasDodeDialog: StateFlow<Event.ShowRegistrationHasDoneDialog?> = _showRegistrationHasDodeDialog

    val nameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val surnameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val emailFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val phoneFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val passwordValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    val nameField = ObservableField(String.Empty)
    val surnameField = ObservableField(String.Empty)
    val emailField = ObservableField(String.Empty)
    val phoneField = ObservableField(String.Empty)
    val passwordField = ObservableField(String.Empty)
    val isCook = ObservableField(false)

    fun singUp() {
        if (isDataValid())
            performRegistration()
    }

    private fun performRegistration() {
        val userRegistration = UserRegistration(
            email = emailField.value,
            firstName = nameField.value,
            lastName = surnameField.value,
            phoneNumber = phoneField.value,
            password = passwordField.value
        )
        val registrationForm = RegistrationForm(userRegistration, role = if (isCook.value) UserRoleEnum.EXECUTOR else UserRoleEnum.CLIENT)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.register(registrationForm).onStart {
                    _isLoading.value = true
                }.catch { exception -> handleErrorResponse(exception) }.collect {
                    _isLoading.value = false
                    _showRegistrationHasDodeDialog.value = Event.ShowRegistrationHasDoneDialog(it.user.email, it.role)
                }
            }
        }
    }

    fun onGotIt() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToLogInScreen) }
    }

    private suspend fun handleErrorResponse(exception: Throwable) {
        _isLoading.value = false
        when (exception) {
            is ResponseException.NetworkError -> eventChannel.send(Event.ShowErrorDialog(exception.responseMessage))
            is ResponseException.ServerError -> {
                if (exception.code == HTTP_BAD_REQUEST) {
                    exception.getErrorResponse()?.run {
                        if ((get("user") as? JsonObject)?.contains("email") == true)
                            emailFieldValidation.postValue(FieldValidation.EmailIsAlreadyUsed)
                        else
                            eventChannel.send(Event.ShowErrorDialog(exception.responseMessage))
                    }
                } else
                    eventChannel.send(Event.ShowErrorDialog(exception.responseMessage))
            }
            is ResponseException.UnknownError -> eventChannel.send(Event.ShowErrorDialog(exception.responseMessage))
        }
    }

    fun showInfoAboutCook() {
        viewModelScope.launch { eventChannel.send(Event.ShowInfoAboutCook) }
    }

    fun close() {
        viewModelScope.launch { eventChannel.send(Event.CloseScreen) }
    }

    fun logIn() {
        viewModelScope.launch { eventChannel.send(Event.NavigateToLogInScreen) }
    }

    private fun isDataValid(): Boolean {
        var isValid = true

        if (nameField.value.isBlank()) {
            isValid = false
            nameFieldValidation.value = FieldValidation.EmptyField
        }

        if (surnameField.value.isBlank()) {
            isValid = false
            surnameFieldValidation.value = FieldValidation.EmptyField
        }

        if (emailField.value.isBlank()) {
            isValid = false
            emailFieldValidation.value = FieldValidation.EmptyField
        } else if (emailField.value.notMatches(EMAIL_REGEX)) {
            isValid = false
            emailFieldValidation.value = FieldValidation.WrongEmailFormat
        }

        if (phoneField.value.isBlank()) {
            isValid = false
            phoneFieldValidation.value = FieldValidation.EmptyField
        } else if (phoneField.value.notMatches(PHONE_REGEX)) {
            isValid = false
            phoneFieldValidation.value = FieldValidation.WrongPhoneFormat
        }

        if (!isPasswordSuitable) {
            isValid = false
            passwordValidation.value = FieldValidation.DoesNotFitRequirements
        }

        return isValid
    }


    class Factory(private val repository: AuthorizationRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SignUpViewModel(repository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}