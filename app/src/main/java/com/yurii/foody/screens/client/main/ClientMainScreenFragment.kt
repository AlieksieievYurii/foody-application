package com.yurii.foody.screens.client.main

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
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.databinding.FragmentNavigationClientPanelBinding
import com.yurii.foody.ui.InformationDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.OnBackPressed
import com.yurii.foody.utils.observeOnLifecycle

class ClientMainScreenFragment : Fragment(R.layout.fragment_navigation_client_panel), OnBackPressed {
    private val binding: FragmentNavigationClientPanelBinding by viewBinding()
    private val registrationHasDoneDialog by lazy { InformationDialog(requireContext()) }
    private val viewModel: ClientMainScreenViewModel by viewModels { Injector.provideClientMainScreenViewModel(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        viewModel.role.observeOnLifecycle(viewLifecycleOwner) { role ->
            binding.navView.menu.apply {
                findItem(R.id.item_become_cook).isVisible = role == UserRoleEnum.CLIENT
                findItem(R.id.item_change_role).isVisible = role != UserRoleEnum.CLIENT
            }
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_personal_information -> navigateToPersonalInformation()
                R.id.item_help -> {
                }
                R.id.item_change_role -> navigateToChangeRole()
                R.id.item_become_cook -> viewModel.requestToBecomeCook()
                R.id.item_log_out -> askUserToAcceptLoggingOut { viewModel.logOut() }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            false
        }

        viewModel.user.observe(viewLifecycleOwner) {
            setHeaderText("${it.firstName} ${it.lastName}")
        }
        observeEvents()

        binding.content.products.setOnClickListener {
            findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToProductsFragment())
        }
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                ClientMainScreenViewModel.Event.NavigateToLogInScreen -> navigateToLogInScreen()
                ClientMainScreenViewModel.Event.ShowDialogToBecomeCook -> showDialogToBecomeCook()
                ClientMainScreenViewModel.Event.ShowDialogYouBecameCook -> registrationHasDoneDialog.show(getString(R.string.message_you_became_cook))
            }
        }
    }

    private fun showDialogToBecomeCook() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_become_cook)
            .setMessage(R.string.message_become_cook)
            .setPositiveButton(R.string.label_yes) { _, _ -> viewModel.becomeCook() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun navigateToLogInScreen() {
        findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToAuthenticationFragment())
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

    private fun navigateToPersonalInformation() {
        findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToPersonalInformationFragment())
    }

    private fun navigateToChangeRole() {
        findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToChooseRoleFragment(selectNewRole = true))
    }

    override fun onBackPressed(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }
}