package com.yurii.foody.screens.admin.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentProductEditorBinding
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class ProductsEditorFragment : Fragment() {

    companion object {
        const val REFRESH_PRODUCTS = "refresh_products"
    }

    private val viewModel: ProductsEditorViewModel by viewModels { Injector.provideProductsEditorViewModel() }
    private lateinit var binding: FragmentProductEditorBinding
    private val listAdapter: ProductAdapter by lazy { ProductAdapter(viewModel.selectableMode, lifecycleScope) }
    private lateinit var searchView: SearchView
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_editor, container, false)

        listAdapter.onClickItem = { productData ->
            navigateToEditor(productData)
        }

        binding.listFragment.setAdapter(listAdapter)

        binding.listFragment.setOnRefreshListener { viewModel.refreshList() }
        binding.listFragment.setOnRetryListener { listAdapter.retry() }
        binding.listFragment.observeListState(viewModel.listState)

        binding.toolbar.setNavigationOnClickListener {
            if (viewModel.selectableMode.value)
                viewModel.selectableMode.value = false
            else
                findNavController().navigateUp()
        }

        binding.add.setOnClickListener {
            findNavController().navigate(ProductsEditorFragmentDirections.actionProductsEditorFragmentToProductEditorFragment())
        }

        observeEvents()
        initOptionMenu()
        observeSelectableMode()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val refresh = findNavController().currentBackStackEntry?.savedStateHandle?.get<Boolean>(REFRESH_PRODUCTS)
        if (refresh == true)
            listAdapter.refresh()
    }

    private fun navigateToEditor(productData: ProductData) {
        findNavController().navigate(
            ProductsEditorFragmentDirections.actionProductsEditorFragmentToProductEditorFragment(productIdToEdit = productData.id)
        )
    }

    private fun askUserToAcceptDeleting(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_delete_items)
            .setMessage(R.string.label_delete_items_message)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun observeSelectableMode() {
        viewModel.selectableMode.observeOnLifecycle(viewLifecycleOwner) { isSelectableMode ->
            binding.toolbar.menu.forEach { menuItem -> menuItem.isVisible = !isSelectableMode }
            binding.toolbar.menu.findItem(R.id.delete).isVisible = isSelectableMode
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), if (isSelectableMode) R.color.gray else R.color.yellow))
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(
                requireContext(),
                if (isSelectableMode) R.drawable.ic_baseline_close_white_24 else R.drawable.ic_arrow_white_24
            )
            binding.toolbar.title = getString(if (isSelectableMode) R.string.label_select else R.string.label_products)
            binding.listFragment.isRefreshEnable = !isSelectableMode
            binding.add.isVisible = !isSelectableMode
        }
    }


    private fun initOptionMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_product_list_editor)
        searchView = binding.toolbar.menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                searchView.clearFocus()
                viewModel.search(text)
                return false
            }

            override fun onQueryTextChange(p0: String?) = false
        })

        searchView.setOnCloseListener {
            viewModel.search(null)
            false
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.filter_available -> {
                    it.isChecked = !it.isChecked
                    viewModel.filterAvailable(it.isChecked)
                    true
                }
                R.id.filter_active -> {
                    it.isChecked = !it.isChecked
                    viewModel.filterActive(it.isChecked)
                    true
                }
                R.id.delete -> {
                    askUserToAcceptDeleting {
                        deleteSelectedProducts()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun deleteSelectedProducts() {
        if (listAdapter.getSelectedItems().isNotEmpty())
            viewModel.deleteItems(listAdapter.getSelectedItems())
        viewModel.selectableMode.value = false
    }

    private fun observeEvents() {
        viewModel.products.observeOnLifecycle(viewLifecycleOwner) {
            listAdapter.submitData(it)
        }

        listAdapter.loadStateFlow.observeOnLifecycle(viewLifecycleOwner) { loadState ->
            viewModel.onLoadStateChange(loadState)
        }

        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                ProductsEditorViewModel.Event.Refresh -> listAdapter.refresh()
                is ProductsEditorViewModel.Event.ShowItemsRemovedSnackBar -> Snackbar.make(
                    binding.root,
                    R.string.hint_items_are_removed,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.loading.observeOnLifecycle(viewLifecycleOwner) {
            if (it) loadingDialog.show() else loadingDialog.close()
        }
    }
}