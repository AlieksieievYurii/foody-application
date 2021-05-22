package com.yurii.foody.screens.admin.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentProductEditorBinding
import com.yurii.foody.ui.ListFragment
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class ProductsEditorFragment : Fragment() {
    private val viewModel: ProductsEditorViewModel by viewModels { Injector.provideProductEditorViewModel(requireContext()) }
    private lateinit var binding: FragmentProductEditorBinding
    private val listAdapter: ProductAdapter = ProductAdapter()
    private lateinit var searchView: SearchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_editor, container, false)

        binding.listFragment.setAdapter(listAdapter)

        binding.listFragment.setOnRefreshListener { listAdapter.refresh() }
        binding.listFragment.setOnRetryListener { listAdapter.retry() }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        observeEvents()
        initOptionMenu()

        return binding.root
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
                else -> false
            }
        }
    }

    private fun observeEvents() {
        viewModel.listState.observe(viewLifecycleOwner) {
            when (it) {
                ProductsEditorViewModel.ListState.ShowLoading -> {
                    if (binding.listFragment.state != ListFragment.State.Ready)
                        binding.listFragment.state = ListFragment.State.Loading
                }
                ProductsEditorViewModel.ListState.ShowResult -> binding.listFragment.state = ListFragment.State.Ready
                ProductsEditorViewModel.ListState.ShowEmptyList -> binding.listFragment.state = ListFragment.State.Empty
                is ProductsEditorViewModel.ListState.ShowError -> binding.listFragment.state = ListFragment.State.Error(it.exception)
            }
        }

        viewModel.products.observeOnLifecycle(viewLifecycleOwner) {
            listAdapter.submitData(it)
        }

        listAdapter.loadStateFlow.observeOnLifecycle(viewLifecycleOwner) { loadState ->
            viewModel.onLoadStateChange(loadState)
        }
    }
}