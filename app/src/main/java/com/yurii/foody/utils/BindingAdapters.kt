package com.yurii.foody.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
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
        FieldValidation.EmailIsAlreadyUsed -> R.string.label_email_is_already_used
        FieldValidation.WrongEmailFormat -> R.string.label_wrong_email
        FieldValidation.WrongPhoneFormat -> R.string.label_wrong_phone_format
        FieldValidation.DoesNotFitRequirements -> TODO()
    })
}

@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String) {
    imageView.load(url) {
        error(R.drawable.image_error_placeholder)
    }
}