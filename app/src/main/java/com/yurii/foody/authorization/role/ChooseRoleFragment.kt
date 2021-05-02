package com.yurii.foody.authorization.role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
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

class ChooseRoleFragment : Fragment() {
    private lateinit var binding: FragmentChooseRoleBinding
    private val args: ChooseRoleFragmentArgs by navArgs()
    private val viewModel: ChooseRoleViewModel by viewModels {
        Injector.provideChooseRoleViewModel(requireContext(), args.selectNewRole)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_choose_role, container, false)
        binding.viewModel = viewModel
        observeEvents()
        return binding.root
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                is ChooseRoleViewModel.Event.NavigateToMainAdministratorScreen -> navigateToMainAdministratorScreen()
                is ChooseRoleViewModel.Event.NavigateToMainClientScreen -> navigateToMainClientScreen()
                is ChooseRoleViewModel.Event.NavigateToMainExecutorScreen -> navigateToMainExecutorScreen()
                is ChooseRoleViewModel.Event.ShowRoleOptions -> showRoleOptions(it.userRole)
                is ChooseRoleViewModel.Event.NavigateToAuthenticationScreen -> navigateToAuthenticationScreen()
                is ChooseRoleViewModel.Event.NavigateToUserRoleIsNotConfirmed -> navigateToUserRoleIsNotConfirmedScreen()
            }
        }
    }

    private fun navigateToMainClientScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToFragmentTest(UserRoleEnum.CLIENT))
    }

    private fun navigateToMainExecutorScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToFragmentTest(UserRoleEnum.EXECUTOR))
    }

    private fun navigateToMainAdministratorScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToFragmentTest(UserRoleEnum.ADMINISTRATOR))
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