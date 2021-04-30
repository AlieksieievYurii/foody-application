package com.yurii.foody.utils

import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber

interface CallBack {
    fun onChange()
}

@BindingAdapter("textWatcher")
fun editTextListener(editText: TextInputEditText, callback: CallBack) =
    editText.setOnTextChangeListener { callback.onChange() }