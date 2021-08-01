package com.yurii.foody.utils

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingSource
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.yurii.foody.R
import com.yurii.foody.api.AuthResponseData
import com.yurii.foody.api.UserRoleEnum
import java.text.SimpleDateFormat
import java.util.*

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

fun ImageView.loadImage(urlOrUri: String) {
    this.load(urlOrUri) {
        error(R.drawable.image_error_placeholder)
    }
}

fun Fragment.closeFragment() {
    findNavController().navigateUp()
}

fun EditText.setOnQueryTextListener(callback: (text: String) -> Unit) {
    this.setOnKeyListener { _, i, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
            callback.invoke(this.text.toString())
            return@setOnKeyListener true
        } else
            return@setOnKeyListener false
    }
}

fun convertToAverageTime(seconds: Int): String {
    val minutes = seconds / 60
    val minTime = (minutes - minutes * 0.1).toInt()
    val maxTime = (minutes + minutes * 0.1).toInt()
    return "$minTime-$maxTime"
}

fun toSimpleDateTime(timestamp: Long): String = SimpleDateFormat("MM.dd.yyyy hh:mm", Locale.getDefault()).format(Date(timestamp))

fun isOrderDelayed(timestamp: Long, cookingTime: Int) = System.currentTimeMillis() > (timestamp + (cookingTime * 1000))

fun toTimestampInSeconds(timestamp: String): Long {
    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return dateTimeFormat.parse(timestamp)?.time ?: 0
}

val Number.toPx get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()