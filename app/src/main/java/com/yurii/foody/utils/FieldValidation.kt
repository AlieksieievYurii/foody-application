package com.yurii.foody.utils

sealed class FieldValidation {
    object NoErrors : FieldValidation()
    object EmptyField : FieldValidation()
    object WrongCredentials : FieldValidation()
    object EmailIsAlreadyUsed : FieldValidation()
    object NoPhoto : FieldValidation()
    object WrongEmailFormat: FieldValidation()
    object WrongPhoneFormat: FieldValidation()
    object DoesNotFitRequirements: FieldValidation()
}