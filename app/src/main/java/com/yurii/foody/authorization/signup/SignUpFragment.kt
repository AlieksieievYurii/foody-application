package com.yurii.foody.authorization.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.databinding.FragmentSignupBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.InformationDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class SignUpFragment : Fragment() {
    private val viewModel: SignUpViewModel by viewModels { Injector.provideSignUpViewModel(requireContext()) }
    private lateinit var binding: FragmentSignupBinding
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val registrationHasDoneDialog by lazy { InformationDialog(requireContext(), isCancelable = false) { viewModel.onGotIt() } }

    private val cookInfoDialog by lazy { InformationDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        observePasswordRequirements()
        observeEvents()

        return binding.root
    }

    private fun observePasswordRequirements() {
        binding.password.setOnFocusChangeListener { _, isFocused -> binding.passwordRequirements.isVisible = isFocused }

        binding.password.addTextChangedListener {
            viewModel.isPasswordSuitable = binding.passwordRequirements.checkPassword(it!!.toString())
            viewModel.passwordField.set(it.toString())
        }
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                SignUpViewModel.Event.CloseScreen -> findNavController().navigateUp()
                SignUpViewModel.Event.NavigateToLogInScreen -> navigateToLogInScreen()
                SignUpViewModel.Event.ShowInfoAboutCook -> cookInfoDialog.show(getString(R.string.description_cook))
                is SignUpViewModel.Event.ShowErrorDialog -> errorDialog.show(it.message)
            }
        }

        viewModel.showRegistrationHasDodeDialog.observeOnLifecycle(viewLifecycleOwner) {
            it?.run {
                registrationHasDoneDialog.show(
                    if (it.userRoleEnum == UserRoleEnum.CLIENT) getString(R.string.hint_registration_done_client, it.email)
                    else getString(R.string.hint_registration_done_cook, it.email)
                )
            }
        }

        viewModel.isLoading.observeOnLifecycle(viewLifecycleOwner) { isLoading ->
            if (isLoading)
                loadingDialog.show()
            else
                loadingDialog.close()
        }
    }

    private fun navigateToLogInScreen() {
        findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToLogInFragment())
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }
}