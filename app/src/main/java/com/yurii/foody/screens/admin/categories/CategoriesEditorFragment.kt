package com.yurii.foody.screens.admin.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentCategoriesEditBinding
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class CategoriesEditorFragment : Fragment() {
    companion object {
        const val REFRESH_CATEGORIES = "refresh_categories"
    }

    private lateinit var binding: FragmentCategoriesEditBinding
    private val viewModel: CategoriesEditorViewModel by viewModels { Injector.provideCategoriesEditorViewModel() }
    private val listAdapter: CategoriesAdapter by lazy { CategoriesAdapter(viewModel.selectableMode, lifecycleScope) }
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_categories_edit, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        listAdapter.onClickItem = {
            //Navigate
        }

        binding.listFragment.setAdapter(listAdapter)

        binding.listFragment.setOnRefreshListener { listAdapter.refresh() }
        binding.listFragment.setOnRetryListener { listAdapter.retry() }

        binding.toolbar.setNavigationOnClickListener {
            if (viewModel.selectableMode.value)
                viewModel.selectableMode.value = false
            else
                findNavController().navigateUp()
        }

        binding.add.setOnClickListener {
            findNavController().navigate(CategoriesEditorFragmentDirections.actionCategoriesEditorFragmentToCategoryEditorFragment())
        }

        initOptionMenu()
        observeCategoriesData()
        observeLoadState()
        observeListState()
        observeSelectableMode()
        observeLoading()
        observeEvents()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val refresh = findNavController().currentBackStackEntry?.savedStateHandle?.get<Boolean>(REFRESH_CATEGORIES)
        if (refresh == true)
            listAdapter.refresh()
    }

    private fun observeCategoriesData() {
        viewModel.categories.observeOnLifecycle(viewLifecycleOwner) {
            listAdapter.submitData(it)
        }
    }

    private fun observeLoadState() {
        listAdapter.loadStateFlow.observeOnLifecycle(viewLifecycleOwner) { loadState ->
            viewModel.onLoadStateChange(loadState)
        }
    }

    private fun observeLoading() {
        viewModel.loading.observeOnLifecycle(viewLifecycleOwner) {
            if (it) loadingDialog.show() else loadingDialog.close()
        }
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                CategoriesEditorViewModel.Event.Refresh -> listAdapter.refresh()
                is CategoriesEditorViewModel.Event.ShowItemsRemovedSnackBar -> Snackbar.make(
                    binding.root,
                    R.string.hint_items_are_removed,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeListState() {
        viewModel.listState.observe(viewLifecycleOwner) {
            when (it) {
                CategoriesEditorViewModel.ListState.ShowLoading -> {
                    if (binding.listFragment.state != ListFragment.State.Ready)
                        binding.listFragment.state = ListFragment.State.Loading
                }
                CategoriesEditorViewModel.ListState.ShowResult -> binding.listFragment.state = ListFragment.State.Ready
                CategoriesEditorViewModel.ListState.ShowEmptyList -> binding.listFragment.state = ListFragment.State.Empty
                is CategoriesEditorViewModel.ListState.ShowError -> binding.listFragment.state = ListFragment.State.Error(it.exception)
            }
        }
    }

    private fun observeSelectableMode() {
        viewModel.selectableMode.observeOnLifecycle(viewLifecycleOwner) { isSelectableMode ->
            binding.toolbar.menu.findItem(R.id.delete).isVisible = isSelectableMode
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), if (isSelectableMode) R.color.gray else R.color.yellow))
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(
                requireContext(),
                if (isSelectableMode) R.drawable.ic_baseline_close_white_24 else R.drawable.ic_arrow_white_24
            )
            binding.toolbar.title = getString(if (isSelectableMode) R.string.label_select else R.string.label_categories)
            binding.listFragment.isRefreshEnable = !isSelectableMode
            binding.add.isVisible = !isSelectableMode
        }
    }

    private fun initOptionMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_categories_list_editor)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.delete) {
                askUserToAcceptDeleting {
                    deleteSelectedProducts()
                }
                true
            } else false
        }
    }

    private fun deleteSelectedProducts() {
        if (listAdapter.getSelectedItems().isNotEmpty())
            viewModel.deleteItems(listAdapter.getSelectedItems())
        viewModel.selectableMode.value = false
    }

    private fun askUserToAcceptDeleting(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_delete_items)
            .setMessage(R.string.label_delete_items_message)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }
}