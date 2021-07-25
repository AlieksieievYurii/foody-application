package com.yurii.foody.screens.personal

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentPersonalInformationBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class PersonalInformationFragment : Fragment(R.layout.fragment_personal_information) {
    private val binding: FragmentPersonalInformationBinding by viewBinding()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val viewModel: PersonalInformationViewModel by viewModels { Injector.providePersonalInformationViewModel(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner) { hideKeyboard() }

        binding.toolbar.setNavigationOnClickListener { closeFragment() }

        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                PersonalInformationViewModel.Event.CloseEditor -> closeFragment()
                is PersonalInformationViewModel.Event.ShowError -> errorDialog.show(event.exception.message ?: getString(R.string.label_no_message))
            }
        }
    }
}