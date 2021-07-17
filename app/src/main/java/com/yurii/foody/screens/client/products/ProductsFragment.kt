package com.yurii.foody.screens.client.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentClientProductsBinding
import com.yurii.foody.utils.Injector

class ProductsFragment : Fragment() {
    private lateinit var binding: FragmentClientProductsBinding
    private val viewModel: ProductsViewModel by viewModels { Injector.provideProductsViewModel() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_client_products, container, false)
        return binding.root
    }
}