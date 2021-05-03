package com.yurii.foody.utils

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.yurii.foody.R

interface CallBack {
    fun onChange()
}

@BindingAdapter("textWatcher")
fun editTextListener(editText: TextInputEditText, callback: CallBack) =
    editText.setOnTextChangeListener { callback.onChange() }

@BindingAdapter("errorField")
fun errorField(textView: TextView, fieldValidation: FieldValidation) {
    textView.isVisible = fieldValidation != FieldValidation.NoErrors
    textView.setText(when(fieldValidation) {
        FieldValidation.EmptyField -> R.string.label_must_not_empty
        FieldValidation.WrongCredentials -> R.string.label_wrong_credentials
        FieldValidation.NoErrors -> return
    })
}