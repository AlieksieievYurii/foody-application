package com.yurii.foody.screens.cook.orders

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentCookOrdersBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.observeOnLifecycle

class CookOrdersScreenFragment : Fragment(R.layout.fragment_cook_orders) {
    private val binding: FragmentCookOrdersBinding by viewBinding()
    private val viewModel: CookOrdersViewModel by viewModels { Injector.provideCookOrdersViewModel(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val listAdapter: OrdersAdapter by lazy {
        OrdersAdapter(viewLifecycleOwner) { order -> askUserToConfirm { viewModel.takeOrder(order) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.orders.apply {
            setAdapter(listAdapter)
            setOnRefreshListener { viewModel.refreshList() }
            observeListState(viewModel.listState)
        }

        listAdapter.apply {
            observeOrders(viewModel.orders)
            observeStateFlow(viewModel::onLoadStateChange)
        }

        binding.toolbar.setNavigationOnClickListener { closeFragment() }

        observeEvents()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                CookOrdersViewModel.Event.RefreshList -> listAdapter.refresh()
                is CookOrdersViewModel.Event.ShowError -> errorDialog.show(event.exception.message ?: getString(R.string.label_no_message))
                is CookOrdersViewModel.Event.NavigateToOrderExecution -> navigateToOrderExecution(event.orderExecutionId)
            }
        }
    }

    private fun navigateToOrderExecution(orderExecutionId: Long) {
        findNavController().navigate(CookOrdersScreenFragmentDirections.actionCookOrdersScreenFragmentToOrderExecutionFragment(orderExecutionId))
    }

    private fun askUserToConfirm(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_taking_order)
            .setMessage(R.string.message_take_order_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }
}