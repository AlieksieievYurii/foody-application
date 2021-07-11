package com.yurii.foody.screens.admin.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentCategoriesEditBinding

class CategoriesEditorFragment : Fragment() {
    private lateinit var binding: FragmentCategoriesEditBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_categories_edit, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}