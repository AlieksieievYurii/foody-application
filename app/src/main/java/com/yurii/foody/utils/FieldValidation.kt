package com.yurii.foody.utils

sealed class FieldValidation {
    object NoErrors : FieldValidation()
    object EmptyField : FieldValidation()
    object WrongCredentials : FieldValidation()
    object EmailIsAlreadyUsed : FieldValidation()
    object WrongEmailFormat: FieldValidation()
    object WrongPhoneFormat: FieldValidation()
}