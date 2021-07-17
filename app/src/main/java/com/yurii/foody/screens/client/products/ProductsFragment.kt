package com.yurii.foody.screens.client.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentClientProductsBinding

class ProductsFragment : Fragment() {
    private lateinit var binding: FragmentClientProductsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_client_products, container, false)
        return binding.root
    }
}