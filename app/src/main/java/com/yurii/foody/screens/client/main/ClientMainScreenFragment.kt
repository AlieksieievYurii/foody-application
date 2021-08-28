package com.yurii.foody.screens.client.main

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.databinding.FragmentNavigationClientPanelBinding
import com.yurii.foody.ui.InformationDialog
import com.yurii.foody.ui.RatingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.OnBackPressed
import com.yurii.foody.utils.observeOnLifecycle

class ClientMainScreenFragment : Fragment(R.layout.fragment_navigation_client_panel), OnBackPressed {
    private val binding: FragmentNavigationClientPanelBinding by viewBinding()
    private val registrationHasDoneDialog by lazy { InformationDialog(requireContext()) }
    private lateinit var historyBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val ratingDialog by lazy { RatingDialog(requireContext()) }
    private val viewModel: ClientMainScreenViewModel by viewModels { Injector.provideClientMainScreenViewModel(requireContext()) }
    private lateinit var historyAndPendingItemsAdapter: HistoryAndPendingItemsAdapter

    private fun navigateToProductDetail(item: Item.HistoryItem) {
        item.product?.run {
            findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToProductDetailFragment(id))
        }
    }

    private fun navigateToOrderDetail(item: Item.PendingItem) {
        findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToOrderDetail(item.id))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.content.openMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        historyAndPendingItemsAdapter = HistoryAndPendingItemsAdapter(viewLifecycleOwner, onClick = { item ->
            when (item) {
                is Item.HistoryItem -> navigateToProductDetail(item)
                is Item.PendingItem -> navigateToOrderDetail(item)
            }
        }, onGiveFeedback = this::onGiveFeedback)
        viewModel.role.observeOnLifecycle(viewLifecycleOwner) { role ->
            binding.navView.menu.apply {
                findItem(R.id.item_become_cook).isVisible = role == UserRoleEnum.CLIENT
                findItem(R.id.item_change_role).isVisible = role != UserRoleEnum.CLIENT
            }
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_personal_information -> navigateToPersonalInformation()
                R.id.item_help -> findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToHelpFragment())
                R.id.item_change_role -> navigateToChangeRole()
                R.id.item_become_cook -> viewModel.requestToBecomeCook()
                R.id.item_log_out -> askUserToAcceptLoggingOut { viewModel.logOut() }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            false
        }

        viewModel.user.observe(viewLifecycleOwner) { setHeaderText("${it.firstName} ${it.lastName}") }

        observeEvents()
        initBottomSheetHistoryView()

        binding.content.products.setOnClickListener {
            findNavController().navigate(ClientMainScreenFragmentDirections.actionClientMainScreenFragmentToProductsFragment())
        }
    }

    private fun onGiveFeedback(historyItem: Item.HistoryItem) {
        ratingDialog.show { rating -> viewModel.giveFeedback(historyItem, rating) }
    }

    private fun initBottomSheetHistoryView() {
        historyBottomSheetBehavior = BottomSheetBehavior.from(binding.content.history.history)
        historyBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.content.history.apply {
                        swipeHeader.isVisible = false
                        appBarLayout.isVisible = true
                    }
                    historyBottomSheetBehavior.isDraggable = false
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.content.history.apply {
                        swipeHeader.isVisible = true
                        appBarLayout.isVisible = false
                    }
                    historyBottomSheetBehavior.isDraggable = true
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.content.history.swipeHeader.alpha = 1 - slideOffset
            }
        })

        binding.content.history.open.setOnClickListener {
            historyBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.content.history.close.setOnClickListener {
            historyBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.content.history.historyList.apply {
            setAdapter(historyAndPendingItemsAdapter)
            observeListState(viewModel.historyAndPendingItemsListState)
            setOnRefreshListener(viewModel::refreshHistoryAndPendingItemsList)
            setOnRetryListener(historyAndPendingItemsAdapter::retry)
        }

        historyAndPendingItemsAdapter.apply {
            observeData(viewModel.historyAndPendingItems)
            bindListState(viewModel::onLoadStateChangeHistoryAndPendingItems)
        }
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                ClientMainScreenViewModel.Event.NavigateToLogInScreen -> navigateToLogInScreen()
                ClientMainScreenViewModel.Event.ShowDialogToBecomeCook -> showDialogToBecomeCook()
                ClientMainScreenViewModel.Event.ShowDialogYouBecameCook -> registrationHasDoneDialog.show(getString(R.string.message_you_became_cook))
                ClientMainScreenViewModel.Event.RefreshHistoryAndPendingItemsList -> historyAndPendingItemsAdapter.refresh()
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

        if (historyBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            historyBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }
        return false
    }
}