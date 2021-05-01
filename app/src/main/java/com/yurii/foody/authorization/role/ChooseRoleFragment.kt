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
import com.yurii.foody.databinding.FragmentChooseRoleBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle
import timber.log.Timber

class ChooseRoleFragment : Fragment() {
    private lateinit var binding: FragmentChooseRoleBinding
    private val args: ChooseRoleFragmentArgs by navArgs()
    private val viewModel: ChooseRoleViewModel by viewModels {
        Injector.provideChooseRoleViewModel(requireContext(), args.role, args.selectNewRole, args.isRoleConfirmed)
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
                is ChooseRoleViewModel.Event.NavigateToUserRoleIsNotConfirmed -> navigateToUserRoleIsNotConfirmedScreen(it.roleEnum)
            }
        }
    }

    private fun navigateToMainClientScreen() {
        findNavController().navigate(R.id.action_chooseRoleFragment_to_fragmentTest)
    }

    private fun navigateToMainExecutorScreen() {
        Timber.i("Navigate to Cook screen")
    }

    private fun navigateToMainAdministratorScreen() {
        Timber.i("Navigate to Admin screen")
    }

    private fun navigateToAuthenticationScreen() {
        findNavController().navigate(ChooseRoleFragmentDirections.actionChooseRoleFragmentToAuthenticationFragment())
    }

    private fun navigateToUserRoleIsNotConfirmedScreen(roleEnum: UserRoleEnum) {
        findNavController().navigate(
            ChooseRoleFragmentDirections.actionChooseRoleFragmentToConfirmationFragment(
                role = roleEnum,
                userIsNotConfirmed = false
            )
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