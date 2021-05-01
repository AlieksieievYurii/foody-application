package com.yurii.foody.authorization.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.databinding.FragmentLoadingBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle
import com.yurii.foody.utils.setListener
import com.yurii.foody.utils.statusBar

class LoadingFragment : Fragment() {
    private val viewModel: LoadingViewModel by viewModels { Injector.provideLoadingViewModel(requireContext()) }
    private lateinit var binding: FragmentLoadingBinding
    private val errorDialog: ErrorDialog by lazy { ErrorDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loading, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.animation.setListener { observeEvents() }
    }

    private fun observeEvents() = viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
        when (it) {
            is LoadingViewModel.Event.NavigateToAuthenticationScreen -> navigateToAuthenticationScreen()
            is LoadingViewModel.Event.NavigateToChooseRoleScreen -> navigateToChooseRoleScreen(it.role)
            is LoadingViewModel.Event.NetworkError -> errorDialog.show("Network error ${it.message}")
            is LoadingViewModel.Event.ServerError -> errorDialog.show("Server error ${it.code}")
            is LoadingViewModel.Event.UnknownError -> errorDialog.show("Unknown error ${it.message}")
            is LoadingViewModel.Event.NavigateToUserIsNotConfirmedScreen -> navigateToConfirmationScreen(ConfirmationFragment.Mode.CONFIRMATION_EMAIL)
            is LoadingViewModel.Event.NavigateToUserRoleIsNotConfirmedScreen -> navigateToConfirmationScreen(ConfirmationFragment.Mode.CONFIRMATION_EXECUTOR_REQUEST)
        }
    }

    private fun navigateToAuthenticationScreen() {
        findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToAuthenticationFragment())
    }

    private fun navigateToChooseRoleScreen(role: UserRoleEnum) {
        findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToChooseRoleFragment(role))
    }

    private fun navigateToConfirmationScreen(mode: ConfirmationFragment.Mode) {
        findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToConfirmationFragment(mode))
    }

    override fun onResume() {
        super.onResume()
        statusBar(hide = true)
    }

    override fun onStop() {
        super.onStop()
        statusBar(hide = false)
    }
}