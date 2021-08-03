package com.yurii.foody.utils

import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.MutableLiveData
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.yurii.foody.R

interface CallBack {
    fun onChange()
}

@BindingAdapter("textWatcher")
fun editTextListener(editText: TextInputEditText, callback: CallBack) =
    editText.setOnTextChangeListener { callback.onChange() }

@BindingAdapter("resetValidation")
fun resetErrorField(editText: TextInputEditText, errorField: MutableLiveData<FieldValidation>) {
    editText.setOnTextChangeListener {
        if (errorField.value != FieldValidation.NoErrors)
            errorField.value = FieldValidation.NoErrors
    }
}

@BindingAdapter("errorField")
fun errorField(textView: TextView, fieldValidation: FieldValidation) {
    textView.isVisible = fieldValidation != FieldValidation.NoErrors
    textView.setText(
        when (fieldValidation) {
            FieldValidation.EmptyField -> R.string.label_must_not_empty
            FieldValidation.WrongCredentials -> R.string.label_wrong_credentials
            FieldValidation.NoErrors -> return
            FieldValidation.EmailIsAlreadyUsed -> R.string.label_email_is_already_used
            FieldValidation.WrongEmailFormat -> R.string.label_wrong_email
            FieldValidation.WrongPhoneFormat -> R.string.label_wrong_phone_format
            FieldValidation.DoesNotFitRequirements -> TODO()
            FieldValidation.NoPhoto -> R.string.error_no_photo
        }
    )
}

@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String) {
    imageView.load(url) {
        error(R.drawable.image_error_placeholder)
    }
}

@BindingAdapter("valueAttrChanged")
fun setListener(spinner: Spinner, listener: InverseBindingListener) {
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            listener.onChange()
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            //Nothing
        }
    }
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("value")
fun setValue(spinner: Spinner, obj: Any) {
    val position = (spinner.adapter as ArrayAdapter<Any>).getPosition(obj)
    spinner.setSelection(position)
}

@InverseBindingAdapter(attribute = "value")
fun getValue(spinner: Spinner): Any {
    return spinner.selectedItem
}

@BindingAdapter("isVisible")
fun isVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}