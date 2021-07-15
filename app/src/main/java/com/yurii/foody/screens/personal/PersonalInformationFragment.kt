package com.yurii.foody.screens.personal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentPersonalInformationBinding
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.hideKeyboard

class PersonalInformationFragment : Fragment() {
    private lateinit var binding: FragmentPersonalInformationBinding
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val viewModel: PersonalInformationViewModel by viewModels { Injector.providePersonalInformationViewModel(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_information, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner) {
            hideKeyboard()
        }

        return binding.root
    }
}