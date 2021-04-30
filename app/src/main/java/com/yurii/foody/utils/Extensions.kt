package com.yurii.foody.utils

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.yurii.foody.api.AuthData
import com.yurii.foody.api.AuthResponseData

fun TextInputEditText.setOnTextChangeListener(callback: (text: String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Nothing
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            callback.invoke(p0.toString())
        }

        override fun afterTextChanged(p0: Editable?) {
            // Nothing
        }
    })
}

val String.Companion.Empty
    get() = ""

val <T> ObservableField<T>.value: T
    get() = this.get()!!

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AuthResponseData.toAuthDataStorage(): AuthDataStorage.Data = AuthDataStorage.Data(
    token = this.token,
    email = this.email,
    userId = this.userId
)