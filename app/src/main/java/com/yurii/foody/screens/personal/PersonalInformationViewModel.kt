package com.yurii.foody.screens.personal

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import com.yurii.foody.utils.value
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class PersonalInformationViewModel(private val userRepository: UserRepository) : ViewModel() {

    sealed class Event {
        data class ShowError(val exception: Throwable) : Event()
        object CloseEditor : Event()
    }

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val nameFieldValidation: LiveData<FieldValidation> = _nameFieldValidation

    private val _surnameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val surnameFieldValidation: LiveData<FieldValidation> = _surnameFieldValidation


    private val _phoneFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val phoneFieldValidation: LiveData<FieldValidation> = _phoneFieldValidation

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    val nameField = ObservableField(String.Empty)
    val surnameField = ObservableField(String.Empty)
    val emailField = ObservableField(String.Empty)
    val phoneField = ObservableField(String.Empty)

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _isLoading.value = false
        viewModelScope.launch {
            eventChannel.send(Event.ShowError(exception))
        }
    }

    private val viewModelJob = Job()
    private val netWorkScope = CoroutineScope(viewModelJob + Dispatchers.IO + coroutineExceptionHandler)


    init {
        netWorkScope.launch {
            _isLoading.value = true
            val currentUser = userRepository.getCurrentUser()
            nameField.set(currentUser.firstName)
            surnameField.set(currentUser.lastName)
            emailField.set(currentUser.email)
            phoneField.set(currentUser.phoneNumber)
            _isLoading.value = false
        }
    }

    fun resetNameValidation() {
        _nameFieldValidation.value = FieldValidation.NoErrors
    }

    fun resetSurnameValidation() {
        _surnameFieldValidation.value = FieldValidation.NoErrors
    }

    fun resetPhoneValidation() {
        _phoneFieldValidation.value = FieldValidation.NoErrors
    }

    fun save() {
        if (isValidated())
            saveChanges()
    }

    private fun saveChanges() {
        netWorkScope.launch {
            _isLoading.value = true
            val currentUser = userRepository.getCurrentUser()
            val updatedUser = currentUser.copy(
                firstName = nameField.value,
                lastName = surnameField.value,
                phoneNumber = phoneField.value
            )

            userRepository.updateUser(updatedUser)
            _isLoading.value = false
            eventChannel.send(Event.CloseEditor)
        }
    }

    private fun isValidated(): Boolean {
        var isValidated = true

        if (nameField.value.isNullOrBlank())
            _nameFieldValidation.value = FieldValidation.EmptyField.also { isValidated = false }

        if (surnameField.value.isNullOrBlank())
            _surnameFieldValidation.value = FieldValidation.EmptyField.also { isValidated = false }

        if (phoneField.value.isNullOrBlank())
            _phoneFieldValidation.value = FieldValidation.EmptyField.also { isValidated = false }

        return isValidated
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PersonalInformationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PersonalInformationViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}