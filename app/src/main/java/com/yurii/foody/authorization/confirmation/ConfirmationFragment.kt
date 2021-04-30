package com.yurii.foody.authorization.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentConfirmationBinding

class ConfirmationFragment : Fragment() {
    enum class Mode {
        CONFIRMATION_EMAIL, CONFIRMATION_EXECUTOR_REQUEST
    }

    private val args: ConfirmationFragmentArgs by navArgs()
    private lateinit var binding: FragmentConfirmationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_confirmation, container, false)

        binding.hint.setText(
            when (args.mode) {
                Mode.CONFIRMATION_EMAIL -> R.string.hint_confirmation_email
                Mode.CONFIRMATION_EXECUTOR_REQUEST -> R.string.hint_confirmation_executor_role
            }
        )

        binding.logIn.setOnClickListener {
            findNavController().navigate(R.id.action_confirmationFragment_to_authenticationFragment)
        }

        binding.changeRole.setOnClickListener {

        }

        return binding.root
    }

}