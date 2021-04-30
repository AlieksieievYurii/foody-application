package com.yurii.foody.authorization.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentAuthenticationBinding

class AuthenticationFragment : Fragment() {
    private lateinit var binding: FragmentAuthenticationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_authentication, container, false)
        binding.signUp.setOnClickListener {
            findNavController().navigate(R.id.action_authenticationFragment_to_signUpFragment)
        }

        binding.logIn.setOnClickListener {
            findNavController().navigate(R.id.action_authenticationFragment_to_logInFragment)
        }
        return binding.root
    }
}