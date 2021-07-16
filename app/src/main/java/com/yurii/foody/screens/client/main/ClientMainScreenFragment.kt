package com.yurii.foody.screens.client.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentNavigationClientPanelBinding

class ClientMainScreenFragment : Fragment() {
    private lateinit var binding: FragmentNavigationClientPanelBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navigation_client_panel, container, false)
        return binding.root
    }
}