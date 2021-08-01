package com.yurii.foody.screens.cook.orders

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
    private val listAdapter: OrdersAdapter by lazy { OrdersAdapter(viewLifecycleOwner, viewModel::openOrder) }

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
                is CookOrdersViewModel.Event.NavigateToOrderDetail -> navigateToOrderExecution(event.orderId)
            }
        }
    }

    private fun navigateToOrderExecution(orderId: Long) {
        findNavController().navigate(
            CookOrdersScreenFragmentDirections.actionCookOrdersScreenFragmentToOrderExecutionFragment(
                orderId = orderId,
                orderExecutionId = -1
            )
        )
    }

}