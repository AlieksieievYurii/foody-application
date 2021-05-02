package com.yurii.foody.authorization.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.databinding.FragmentLogInBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class LogInFragment : Fragment() {
    private val viewModel: LogInViewModel by viewModels { Injector.provideLogInViewModel(requireContext()) }

    private lateinit var binding: FragmentLogInBinding
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog: ErrorDialog by lazy { ErrorDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_log_in, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        observeLoading()
        observeEmailValidation()
        observePasswordValidation()
        observeEventFlow()

        return binding.root
    }

    private fun observeEventFlow() = viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
        when (it) {
            is LogInViewModel.Event.NavigateToChooseRoleScreen -> navigateToChooseRoleScreen()
            is LogInViewModel.Event.ServerError -> errorDialog.show(getString(R.string.label_server_error, it.errorCode))
            is LogInViewModel.Event.NetworkError -> errorDialog.show(getString(R.string.label_network_error, it.message))
            is LogInViewModel.Event.UnknownError -> errorDialog.show(getString(R.string.label_unknown_error, it.message))
            is LogInViewModel.Event.Close -> findNavController().navigateUp()
            is LogInViewModel.Event.NavigateToSingUpScreen -> navigateToSingUpScreen()
            LogInViewModel.Event.NavigateToUserIsNotConfirmed -> navigateToUserIsNotConfirmed()
        }
    }

    private fun observeLoading() = viewModel.isLoading.observe(viewLifecycleOwner) {
        if (it) {
            loadingDialog.show()
            hideKeyboard()
        } else
            loadingDialog.close()
    }

    private fun navigateToChooseRoleScreen() {
        findNavController().navigate(LogInFragmentDirections.actionLogInFragmentToChooseRoleFragment())
    }

    private fun navigateToSingUpScreen() {
        findNavController().navigate(LogInFragmentDirections.actionLogInFragmentToSignUpFragment())
    }

    private fun navigateToUserIsNotConfirmed() {
        findNavController().navigate(
            LogInFragmentDirections.actionLogInFragmentToConfirmationFragment(mode = ConfirmationFragment.Mode.EMAIL_IS_NOT_CONFIRMED)
        )
    }

    private fun observeEmailValidation() = viewModel.emailValidation.observe(viewLifecycleOwner) {
        when (it) {
            is FieldValidation.EmptyField -> setEditTextError(binding.errorEmailField, R.string.label_must_not_empty)
            is FieldValidation.WrongCredentials -> setEditTextError(binding.errorEmailField, R.string.label_wrong_credentials)
            is FieldValidation.None -> hideError(binding.errorEmailField)
        }
    }

    private fun observePasswordValidation() = viewModel.passwordValidation.observe(viewLifecycleOwner) {
        if (it is FieldValidation.EmptyField)
            setEditTextError(binding.errorPasswordField, R.string.label_must_not_empty)
        else
            hideError(binding.errorPasswordField)
    }

    private fun setEditTextError(textView: TextView, errorMessageResource: Int) {
        textView.apply {
            visibility = View.VISIBLE
            setText(errorMessageResource)
        }
    }

    private fun hideError(textView: TextView) {
        if (textView.isVisible)
            textView.visibility = View.GONE
    }
}