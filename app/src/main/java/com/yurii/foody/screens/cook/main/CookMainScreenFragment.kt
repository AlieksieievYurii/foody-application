package com.yurii.foody.screens.cook.main

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNavigationCookPanelBinding
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.OnBackPressed
import com.yurii.foody.utils.observeOnLifecycle

class CookMainScreenFragment : Fragment(R.layout.fragment_navigation_cook_panel), OnBackPressed {
    private val binding: FragmentNavigationCookPanelBinding by viewBinding()
    private val viewModel: CookMainScreenViewModel by viewModels { Injector.provideCookMainScreenViewModel(requireContext()) }
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.content.viewModel = viewModel
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner)
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_personal_information -> viewModel.changePersonalInformation()
                R.id.item_help -> {
                }
                R.id.item_change_role -> viewModel.changeRole()
                R.id.item_log_out -> askUserToAcceptLoggingOut {
                    viewModel.logOut()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            false
        }

        viewModel.user.observe(viewLifecycleOwner) {
            setHeaderText("${it.firstName} ${it.lastName}")
        }

        observeEvents()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                CookMainScreenViewModel.Event.NavigateToChangeRole -> navigateToChooseRoleScreen()
                CookMainScreenViewModel.Event.NavigateToLogInScreen -> navigateToLogInScreen()
                CookMainScreenViewModel.Event.NavigateToPersonalInformation -> navigateToPersonalInformationScreen()
                CookMainScreenViewModel.Event.NavigateToOrders -> navigateToOrders()
                is CookMainScreenViewModel.Event.NavigateToOrderExecution -> navigateToOrderExecution(it.orderExecutionId)
            }
        }
    }

    private fun navigateToOrderExecution(orderExecutionId: Long) {
        findNavController().navigate(
            CookMainScreenFragmentDirections.actionCookMainScreenFragmentToOrderExecutionFragment(
                orderId = -1,
                orderExecutionId = orderExecutionId
            )
        )
    }

    private fun navigateToOrders() {
        findNavController().navigate(CookMainScreenFragmentDirections.actionCookMainScreenFragmentToCookOrdersScreenFragment())
    }

    private fun navigateToPersonalInformationScreen() {
        findNavController().navigate(CookMainScreenFragmentDirections.actionCookMainScreenFragmentToPersonalInformationFragment())
    }

    private fun navigateToLogInScreen() {
        findNavController().navigate(CookMainScreenFragmentDirections.actionCookMainScreenFragmentToAuthenticationFragment())
    }

    private fun navigateToChooseRoleScreen() {
        findNavController().navigate(CookMainScreenFragmentDirections.actionCookMainScreenFragmentToChooseRoleFragment(selectNewRole = true))
    }

    private fun askUserToAcceptLoggingOut(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_log_out)
            .setMessage(R.string.message_logout_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun setHeaderText(text: String) {
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.name).text = text
    }

    override fun onBackPressed(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }
}