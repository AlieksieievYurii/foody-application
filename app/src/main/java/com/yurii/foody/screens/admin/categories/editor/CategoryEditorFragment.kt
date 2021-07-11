package com.yurii.foody.screens.admin.categories.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentEditCreateCategoryBinding
import com.yurii.foody.utils.Injector

class CategoryEditorFragment : Fragment() {
    private lateinit var binding: FragmentEditCreateCategoryBinding
    private val viewModel: CategoryEditorViewModel by viewModels { Injector.provideCategoryEditorViewModel(requireActivity().application, -1) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_create_category, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}