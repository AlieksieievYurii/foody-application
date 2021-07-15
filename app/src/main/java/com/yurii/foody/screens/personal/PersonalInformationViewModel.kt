package com.yurii.foody.screens.personal

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.yurii.foody.authorization.signup.SignUpViewModel
import com.yurii.foody.utils.Empty
import com.yurii.foody.utils.FieldValidation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PersonalInformationViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _nameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val nameFieldValidation: LiveData<FieldValidation> = _nameFieldValidation

    private val _surnameFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val surnameFieldValidation: LiveData<FieldValidation> = _surnameFieldValidation


    private val _phoneFieldValidation = MutableLiveData<FieldValidation>(FieldValidation.NoErrors)
    val phoneFieldValidation: LiveData<FieldValidation> = _phoneFieldValidation

    private val eventChannel = Channel<SignUpViewModel.Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    val nameField = ObservableField(String.Empty)
    val surnameField = ObservableField(String.Empty)
    val emailField = ObservableField(String.Empty)
    val phoneField = ObservableField(String.Empty)

    init {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            nameField.set(currentUser.firstName)
            surnameField.set(currentUser.lastName)
            emailField.set(currentUser.email)
            phoneField.set(currentUser.phoneNumber)
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