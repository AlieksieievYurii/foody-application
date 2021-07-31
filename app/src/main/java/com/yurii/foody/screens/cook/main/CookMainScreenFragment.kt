package com.yurii.foody.screens.cook.main

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNavigationCookPanelBinding
import com.yurii.foody.utils.OnBackPressed

class CookMainScreenFragment : Fragment(R.layout.fragment_navigation_cook_panel), OnBackPressed {
    private val binding: FragmentNavigationCookPanelBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_personal_information -> {

                }
                R.id.item_help -> {
                }
                R.id.item_change_role -> {
                }
                R.id.item_log_out -> askUserToAcceptLoggingOut {
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            false
        }
    }

    private fun askUserToAcceptLoggingOut(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_log_out)
            .setMessage(R.string.message_logout_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    override fun onBackPressed(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }
}