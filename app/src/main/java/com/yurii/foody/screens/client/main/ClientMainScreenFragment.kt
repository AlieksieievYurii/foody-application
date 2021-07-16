package com.yurii.foody.screens.client.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNavigationClientPanelBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.OnBackPressed
import com.yurii.foody.utils.observeOnLifecycle

class ClientMainScreenFragment : Fragment(), OnBackPressed {
    private lateinit var binding: FragmentNavigationClientPanelBinding
    private val viewModel: ClientMainScreenViewModel by viewModels { Injector.provideClientMainScreenViewModel(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navigation_client_panel, container, false)
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_personal_information -> navigateToPersonalInformation()
                R.id.item_help -> {
                }
                R.id.item_change_role -> navigateToChangeRole()
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
        return binding.root
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                ClientMainScreenViewModel.Event.NavigateToLogInScreen -> navigateToLogInScreen()
            }
        }
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