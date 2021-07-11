package com.yurii.foody.screens.admin.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentCategoriesEditBinding
import com.yurii.foody.utils.Injector

class CategoriesEditorFragment : Fragment() {
    private lateinit var binding: FragmentCategoriesEditBinding
    private val viewModel: CategoriesEditorViewModel by viewModels { Injector.provideCategoriesEditorViewModel() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_categories_edit, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.test()

        return binding.root
    }
}