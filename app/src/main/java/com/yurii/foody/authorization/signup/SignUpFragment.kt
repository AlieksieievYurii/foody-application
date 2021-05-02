package com.yurii.foody.authorization.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentSingupBinding

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSingupBinding
    private var isSuitablePassword: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_singup, container, false)

        binding.password.setOnFocusChangeListener { _, isFocused -> binding.passwordRequirements.isVisible = isFocused }

        binding.password.addTextChangedListener {
            isSuitablePassword = binding.passwordRequirements.checkPassword(it!!.toString())
        }

        return binding.root
    }
}