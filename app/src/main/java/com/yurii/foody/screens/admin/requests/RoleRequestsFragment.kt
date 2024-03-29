package com.yurii.foody.screens.admin.requests

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentRoleRequestsBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.observeOnLifecycle

class RoleRequestsFragment : Fragment(R.layout.fragment_role_requests) {
    private val viewModel: RoleRequestsViewModel by viewModels { Injector.provideRoleRequestsViewModel(requireContext()) }
    private val binding: FragmentRoleRequestsBinding by viewBinding()
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val listAdapter: UserRoleAdapter by lazy {
        UserRoleAdapter {
            askUserToConfirmTheAction {
                viewModel.acceptRoleRequest(it)
            }
        }
    }

    private fun askUserToConfirmTheAction(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_role_request)
            .setMessage(R.string.label_comfirm_role_request)
            .setPositiveButton(R.string.label_yes) { _, _ -> onConfirm.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.requests.setAdapter(listAdapter)

        binding.requests.setOnRefreshListener { viewModel.refreshList() }
        binding.requests.setOnRetryListener { listAdapter.retry() }

        binding.toolbar.setNavigationOnClickListener { closeFragment() }

        binding.requests.observeListState(viewModel.listState)

        observeLoadState()
        observeRequests()
        observeEvents()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                RoleRequestsViewModel.Event.RefreshList -> listAdapter.refresh()
                is RoleRequestsViewModel.Event.ShowError -> errorDialog.show(event.exception.message ?: getString(R.string.label_no_message))
            }
        }
    }

    private fun observeLoadState() {
        listAdapter.loadStateFlow.observeOnLifecycle(viewLifecycleOwner) { loadState ->
            viewModel.onLoadStateChange(loadState)
        }
    }

    private fun observeRequests() {
        viewModel.userRolesRequests.observeOnLifecycle(viewLifecycleOwner) {
            listAdapter.submitData(it)
        }
    }
}