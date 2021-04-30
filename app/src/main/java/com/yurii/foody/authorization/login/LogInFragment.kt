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
import com.yurii.foody.R
import com.yurii.foody.api.Service
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.databinding.FragmentLogInBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle
import timber.log.Timber

class LogInFragment : Fragment() {
    private val viewModel: LogInViewModel by viewModels {
        LogInViewModel.Factory(
            AuthorizationRepository(
                authDataStorage = AuthDataStorage.create(requireContext()),
                apiTokenAuth = Service.authService
            )
        )
    }

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
            is LogInViewModel.Event.Authenticated -> {Timber.i("OK")}
            is LogInViewModel.Event.ServerError -> errorDialog.show("Server error\nHttp Code: ${it.errorCode}")
            is LogInViewModel.Event.NetworkError -> errorDialog.show("NetWork error\n${it.message}")
            is LogInViewModel.Event.UnknownError -> errorDialog.show("Unknown Error\n${it.message}}")
        }
    }

    private fun observeLoading() = viewModel.isLoading.observe(viewLifecycleOwner) {
        if (it) {
            loadingDialog.show()
            hideKeyboard()
        } else
            loadingDialog.close()
    }

    private fun observeEmailValidation() = viewModel.emailValidation.observe(viewLifecycleOwner) {
        when (it) {
            is FieldValidation.EmptyField -> setEditTextError(binding.errorEmail, R.string.label_must_not_empty)
            is FieldValidation.WrongCredentials -> setEditTextError(binding.errorEmail, R.string.label_wrong_credentials)
            is FieldValidation.None -> hideError(binding.errorEmail)
        }
    }

    private fun observePasswordValidation() = viewModel.passwordValidation.observe(viewLifecycleOwner) {
        if (it is FieldValidation.EmptyField)
            setEditTextError(binding.errorPassword, R.string.label_must_not_empty)
        else
            hideError(binding.errorPassword)
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