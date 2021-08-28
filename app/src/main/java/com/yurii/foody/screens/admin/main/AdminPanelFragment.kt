package com.yurii.foody.screens.admin.main

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
import com.yurii.foody.databinding.FragmentNavigationAdminPanelBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.OnBackPressed
import com.yurii.foody.utils.observeOnLifecycle

class AdminPanelFragment : Fragment(R.layout.fragment_navigation_admin_panel), OnBackPressed {
    private val binding: FragmentNavigationAdminPanelBinding by viewBinding()
    private val viewModel: AdminPanelViewModel by viewModels { Injector.provideAdminPanelViewModel(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.content.viewModel = viewModel
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        observeEvents()

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_personal_information -> viewModel.changePersonalInformation()
                R.id.item_help -> findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToHelpFragment())
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
    }

    private fun askUserToAcceptLoggingOut(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_log_out)
            .setMessage(R.string.message_logout_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                AdminPanelViewModel.Event.NavigateToCategoriesEditor -> navigateToCategoriesEditor()
                AdminPanelViewModel.Event.NavigateToProductsEditor -> navigateToProductEditor()
                AdminPanelViewModel.Event.NavigateToRequests -> navigateToRequests()
                AdminPanelViewModel.Event.NavigateToChangeRole -> navigateToChooseRoleScreen()
                AdminPanelViewModel.Event.NavigateToLogInScreen -> navigateToLogInScreen()
                AdminPanelViewModel.Event.NavigateToPersonalInformation -> navigateToPersonalInformationScreen()
            }
        }
    }

    private fun navigateToPersonalInformationScreen() {
        findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToPersonalInformationFragment())
    }

    private fun navigateToLogInScreen() {
        findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToAuthenticationFragment())
    }

    private fun navigateToChooseRoleScreen() {
        findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToChooseRoleFragment(selectNewRole = true))
    }

    private fun navigateToProductEditor() {
        findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToProductsEditorFragment())
    }

    private fun navigateToRequests() {
        findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToRoleRequestsFragment())
    }

    private fun navigateToCategoriesEditor() {
        findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToCategoriesEditorFragment())
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