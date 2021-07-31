package com.yurii.foody.authorization.role

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.yurii.foody.R
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.databinding.FragmentChooseRoleBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class ChooseRoleFragment : Fragment(R.layout.fragment_choose_role) {
    private val binding: FragmentChooseRoleBinding by viewBinding()
    private val args: ChooseRoleFragmentArgs by navArgs()
    private val viewModel: ChooseRoleViewModel by viewModels { Injector.provideChooseRoleViewModel(requireContext(), args.selectNewRole) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        observeEvents()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                is ChooseRoleViewModel.Event.NavigateToMainAdministratorScreen -> navigateToMainAdministratorScreen()
                is ChooseRoleViewModel.Event.NavigateToMainClientScreen -> navigateToMainClientScreen()
                is ChooseRoleViewModel.Event.NavigateToMainExecutorScreen -> navigateToMainExecutorScreen()
                is ChooseRoleViewModel.Event.NavigateToAuthenticationScreen -> navigateToAuthenticationScreen()
                is ChooseRoleViewModel.Event.NavigateToUserRoleIsNotConfirmed -> navigateToUserRoleIsNotConfirmedScreen()
            }
        }

        viewModel.showRoleOptions.observe(viewLifecycleOwner) { showRoleOptions(it) }
    }

    private fun navigateToMainClientScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToClientMainScreenFragment())
    }

    private fun navigateToMainExecutorScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToCookMainScreenFragment())
    }

    private fun navigateToMainAdministratorScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToAdminPanelFragment())
    }

    private fun navigateToAuthenticationScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToAuthenticationFragment())
    }

    private fun navigateToUserRoleIsNotConfirmedScreen() {
        findNavController().navigate(
            ChooseRoleFragmentDirections.actionChooseRoleFragmentToConfirmationFragment(ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED)
        )
    }

    private fun showRoleOptions(userRoleEnum: UserRoleEnum) {
        when (userRoleEnum) {
            UserRoleEnum.CLIENT -> {
                binding.client.isVisible = true
            }
            UserRoleEnum.EXECUTOR -> {
                binding.client.isVisible = true
                binding.cook.isVisible = true
            }
            UserRoleEnum.ADMINISTRATOR -> {
                binding.client.isVisible = true
                binding.cook.isVisible = true
                binding.administrator.isVisible = true
            }
        }
    }
}