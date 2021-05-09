package com.yurii.foody.screens.admin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNavigationAdminPanelBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.OnBackPressed
import com.yurii.foody.utils.observeOnLifecycle
import timber.log.Timber

class AdminPanelFragment : Fragment(), OnBackPressed {
    private lateinit var binding: FragmentNavigationAdminPanelBinding
    private val viewModel: AdminPanelViewModel by viewModels { Injector.provideAdminPanelViewModel(requireContext()) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navigation_admin_panel, container, false)
        binding.content.viewModel = viewModel
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        observeEvents()
        return binding.root
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                is AdminPanelViewModel.Event.SetHeaderInformation -> setHeaderText("${it.name} ${it.surName}")
                AdminPanelViewModel.Event.NavigateToCategoriesEditor -> navigateToCategoriesEditor()
                AdminPanelViewModel.Event.NavigateToProductsEditor -> navigateToProductEditor()
                AdminPanelViewModel.Event.NavigateToRequests -> navigateToRequests()
            }
        }
    }

    private fun navigateToProductEditor() {
        Timber.i("Product editor")
    }

    private fun navigateToRequests() {
        Timber.i("Requests editor")
    }

    private fun navigateToCategoriesEditor() {
        Timber.i("Categories editor")
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