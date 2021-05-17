package com.yurii.foody.utils

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.paging.PagingSource
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.yurii.foody.api.AuthResponseData
import com.yurii.foody.api.UserRoleEnum

fun Fragment.statusBar(hide: Boolean) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        activity?.window?.setDecorFitsSystemWindows(!hide)
    } else {
        @Suppress("DEPRECATION")
        if (hide)
            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        else
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}

fun LottieAnimationView.setListener(callback: () -> Unit) {
    this.addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator?) {
            //Nothing
        }

        override fun onAnimationEnd(p0: Animator?) {
            //Nothing
        }

        override fun onAnimationCancel(p0: Animator?) {
            //Nothing
        }

        override fun onAnimationRepeat(p0: Animator?) {
            callback.invoke()
        }
    })
}

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

fun UserRoleEnum.isInsideScope(userRoleEnum: UserRoleEnum): Boolean {
    if (this == UserRoleEnum.ADMINISTRATOR && userRoleEnum in listOf(UserRoleEnum.CLIENT, UserRoleEnum.EXECUTOR))
        return false

    if (this == UserRoleEnum.EXECUTOR && userRoleEnum == UserRoleEnum.CLIENT)
        return false

    return true
}

fun String.notMatches(regex: String) = Regex(regex).find(this) == null

class EmptyListException : Exception()