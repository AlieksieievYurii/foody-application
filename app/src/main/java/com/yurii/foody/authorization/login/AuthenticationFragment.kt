package com.yurii.foody.authorization.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentAuthenticationBinding

class AuthenticationFragment : Fragment() {
    private lateinit var binding: FragmentAuthenticationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_authentication, container, false)
        binding.signUp.setOnClickListener {
            findNavController().navigate(AuthenticationFragmentDirections.actionAuthenticationFragmentToSignUpFragment())
        }

        binding.logIn.setOnClickListener {
            findNavController().navigate(AuthenticationFragmentDirections.actionAuthenticationFragmentToLogInFragment())
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }
}