package com.yurii.foody.utils

sealed class FieldValidation {
    object NoErrors : FieldValidation()
    object EmptyField : FieldValidation()
    object WrongCredentials : FieldValidation()
}