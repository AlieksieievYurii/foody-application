package com.yurii.foody.screens.admin.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentRoleRequestsBinding
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class RoleRequestsFragment : Fragment() {
    private val viewModel: RoleRequestsViewModel by viewModels { Injector.provideRoleRequestsViewModel() }
    private lateinit var binding: FragmentRoleRequestsBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_role_requests, container, false)
        binding.requests.setAdapter(listAdapter)

        binding.requests.setOnRefreshListener {
            viewModel.isRefreshing = true
            listAdapter.refresh()
        }
        binding.requests.setOnRetryListener { listAdapter.retry() }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        observeLoadState()
        observeListState()
        observeRequests()
        return binding.root
    }

    private fun observeLoadState() {
        listAdapter.loadStateFlow.observeOnLifecycle(viewLifecycleOwner) { loadState ->
            viewModel.onLoadStateChange(loadState)
        }
    }

    private fun observeListState() {
        viewModel.listState.observe(viewLifecycleOwner) {
            when (it) {
                RoleRequestsViewModel.ListState.ShowLoading -> binding.requests.state = ListFragment.State.Loading
                RoleRequestsViewModel.ListState.ShowResult -> binding.requests.state = ListFragment.State.Ready
                RoleRequestsViewModel.ListState.ShowEmptyList -> binding.requests.state = ListFragment.State.Empty
                is RoleRequestsViewModel.ListState.ShowError -> binding.requests.state = ListFragment.State.Error(it.exception)
            }
        }
    }

    private fun observeRequests() {
        viewModel.userRolesRequests.observeOnLifecycle(viewLifecycleOwner) {
            listAdapter.submitData(it)
        }
    }
}