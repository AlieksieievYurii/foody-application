package com.yurii.foody.screens.cook.execution

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.api.OrderExecutionStatus
import com.yurii.foody.databinding.FragmentOrderExecutionBinding
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.observeOnLifecycle

class OrderExecutionFragment : Fragment(R.layout.fragment_order_execution) {
    private val args: OrderExecutionFragmentArgs by navArgs()
    private val binding: FragmentOrderExecutionBinding by viewBinding()
    private val viewModel: OrderExecutionViewModel by viewModels {
        Injector.provideOrderExecutionViewModel(requireContext(), args.orderId, args.orderExecutionId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.orderStatus.apply {
            lifecycleOwner = viewLifecycleOwner
            setListener(viewModel::onOrderStatusChanged)
            observeLoading(viewModel.changingOrderStatus)
        }
        binding.takeOrder.setOnClickListener { askUserToConfirm(viewModel::takeOrder) }
        observeEvents()
    }

    private fun askUserToAcceptChangingStatus(currentStatus: OrderExecutionStatus, nextOrderStatus: OrderExecutionStatus) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_change_order_status)
            .setMessage(getString(R.string.message_change_order_status, currentStatus.status, nextOrderStatus.status))
            .setPositiveButton(R.string.label_yes) { _, _ -> viewModel.performChangingOrderStatus(nextOrderStatus) }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun askUserToConfirm(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_taking_order)
            .setMessage(R.string.message_take_order_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                is OrderExecutionViewModel.Event.AskUserToChangeOrderExecutionStatus -> askUserToAcceptChangingStatus(
                    it.currentStatus,
                    it.nextOrderStatus
                )
                OrderExecutionViewModel.Event.CloseScreen -> closeFragment()
                is OrderExecutionViewModel.Event.ShowError -> TODO()
            }
        }
    }
}