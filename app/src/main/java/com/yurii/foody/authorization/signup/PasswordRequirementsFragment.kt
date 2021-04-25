package com.yurii.foody.authorization.signup

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentPasswordRequirementsBinding

class PasswordRequirementsFragment(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding: FragmentPasswordRequirementsBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.fragment_password_requirements, this, true
    )

    fun checkPassword(password: String): Boolean {
        val length = validateEightCharacters(password)
        val oneLetter = validateOneLetter(password)
        val oneNumber = validateOneNumber(password)
        val oneSymbol = validateOneSymbol(password)
        return length && oneLetter && oneNumber && oneSymbol
    }

    private fun validateOneLetter(password: String): Boolean {
        val isOkay = Regex(".*[a-zA-Z].*").find(password) != null
        setState(binding.oneLetter, isOkay)
        return isOkay
    }

    private fun validateOneNumber(password: String): Boolean {
        val isOkay = Regex(".*[\\d].*").find(password) != null
        setState(binding.oneNumber, isOkay)
        return isOkay
    }

    private fun validateOneSymbol(password: String): Boolean {
        val isOkay = Regex(".*[!@#\$%^&/*(),.?\":{}|<>].*").find(password) != null
        setState(binding.oneSymbol, isOkay)
        return isOkay
    }

    private fun validateEightCharacters(password: String): Boolean {
        val isLengthOkay = password.length >= 8
        setState(binding.eightCharacters, isLengthOkay)
        return isLengthOkay
    }


    private fun setState(imageView: ImageView, isOkay: Boolean) {
        imageView.setImageResource(if (isOkay) R.drawable.ic_check_circle_outline_24_yellow else R.drawable.ic_check_circle_outline_24_gray)
    }

}