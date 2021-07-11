package com.yurii.foody.screens.admin.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentRoleRequestsBinding
import com.yurii.foody.utils.Injector

class RoleRequestsFragment : Fragment() {
    private val viewModel: RoleRequestsViewModel by viewModels { Injector.provideRoleRequestsViewModel() }
    private lateinit var binding: FragmentRoleRequestsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_role_requests, container, false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}