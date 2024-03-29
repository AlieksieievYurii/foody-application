package com.yurii.foody.authorization.signup

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.yurii.foody.R
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.databinding.FragmentSignupBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.InformationDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.*

class SignUpFragment : Fragment(R.layout.fragment_signup) {
    private val viewModel: SignUpViewModel by viewModels { Injector.provideSignUpViewModel(requireContext()) }
    private val binding: FragmentSignupBinding by viewBinding()
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val registrationHasDoneDialog by lazy { InformationDialog(requireContext(), isCancelable = false) { viewModel.onGotIt() } }
    private val cookInfoDialog by lazy { InformationDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner) {hideKeyboard()}
        observePasswordRequirements()
        observeEvents()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    private fun observePasswordRequirements() {
        binding.password.setOnFocusChangeListener { _, isFocused -> binding.passwordRequirements.isVisible = isFocused }

        binding.password.addTextChangedListener {
            viewModel.isPasswordSuitable = binding.passwordRequirements.checkPassword(it!!.toString())
            viewModel.passwordField.set(it.toString())
        }

        viewModel.passwordValidation.observe(viewLifecycleOwner) {
            if (it == FieldValidation.DoesNotFitRequirements) {
                binding.passwordRequirements.highlight()
                binding.passwordRequirements.isVisible = true
            }
        }
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                SignUpViewModel.Event.CloseScreen -> closeFragment()
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
    }

    private fun navigateToLogInScreen() {
        findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToLogInFragment())
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }
}