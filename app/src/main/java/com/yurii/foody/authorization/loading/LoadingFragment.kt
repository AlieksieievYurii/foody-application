package com.yurii.foody.authorization.loading

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.databinding.FragmentLoadingBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle
import com.yurii.foody.utils.setListener
import com.yurii.foody.utils.statusBar

class LoadingFragment : Fragment(R.layout.fragment_loading) {
    private val viewModel: LoadingViewModel by viewModels { Injector.provideLoadingViewModel(requireContext()) }
    private val binding: FragmentLoadingBinding by viewBinding()
    private val errorDialog: ErrorDialog by lazy { ErrorDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.animation.setListener { observeEvents() }
    }

    private fun observeEvents() = viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
        when (it) {
            is LoadingViewModel.Event.NavigateToAuthenticationScreen -> navigateToAuthenticationScreen()
            is LoadingViewModel.Event.NavigateToChooseRoleScreen -> navigateToChooseRoleScreen()
            is LoadingViewModel.Event.NetworkError -> errorDialog.show(getString(R.string.label_network_error, it.message))
            is LoadingViewModel.Event.ServerError -> errorDialog.show(getString(R.string.label_server_error, it.code))
            is LoadingViewModel.Event.UnknownError -> errorDialog.show(getString(R.string.label_unknown_error, it.message))
            is LoadingViewModel.Event.NavigateToUserIsNotConfirmedScreen -> navigateToUserIsNotConfirmedScreen()
        }
    }

    private fun navigateToAuthenticationScreen() {
        val extras = FragmentNavigatorExtras(binding.title to "title_transition")
        findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToAuthenticationFragment(), extras)
    }

    private fun navigateToChooseRoleScreen() {
        findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToChooseRoleFragment())
    }

    private fun navigateToUserIsNotConfirmedScreen() {
        findNavController().navigate(LoadingFragmentDirections.actionLoadingFragmentToConfirmationFragment(ConfirmationFragment.Mode.EMAIL_IS_NOT_CONFIRMED))
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