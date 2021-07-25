package com.yurii.foody.authorization.login

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.databinding.FragmentLogInBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class LogInFragment : Fragment(R.layout.fragment_log_in) {
    private val viewModel: LogInViewModel by viewModels { Injector.provideLogInViewModel(requireContext()) }
    private val binding: FragmentLogInBinding by viewBinding()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog: ErrorDialog by lazy { ErrorDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        observeEventFlow()
        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner) { hideKeyboard() }
    }


    private fun observeEventFlow() = viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
        when (it) {
            is LogInViewModel.Event.NavigateToChooseRoleScreen -> navigateToChooseRoleScreen()
            is LogInViewModel.Event.ServerError -> errorDialog.show(getString(R.string.label_server_error, it.errorCode))
            is LogInViewModel.Event.NetworkError -> errorDialog.show(getString(R.string.label_network_error, it.message))
            is LogInViewModel.Event.UnknownError -> errorDialog.show(getString(R.string.label_unknown_error, it.message))
            is LogInViewModel.Event.Close -> closeFragment()
            is LogInViewModel.Event.NavigateToSingUpScreen -> navigateToSingUpScreen()
            LogInViewModel.Event.NavigateToUserIsNotConfirmed -> navigateToUserIsNotConfirmed()
        }
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
}