package com.yurii.foody.screens.admin.categories.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentEditCreateCategoryBinding
import com.yurii.foody.screens.admin.categories.CategoriesEditorFragment
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.observeOnLifecycle

class CategoryEditorFragment : Fragment() {
    private val args: CategoryEditorFragmentArgs by navArgs()
    private lateinit var binding: FragmentEditCreateCategoryBinding
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val uploadImageDialog: UploadPhotoDialog by lazy { UploadPhotoDialog(requireContext(), requireActivity().activityResultRegistry) }
    private val viewModel: CategoryEditorViewModel by viewModels {
        Injector.provideCategoryEditorViewModel(
            requireActivity().application,
            args.categoryIdToEdit,
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_create_category, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.action.text = getString(if (viewModel.isEditMode) R.string.label_save else R.string.label_create)

        binding.image.setOnClickListener {
            uploadImageDialog.show { viewModel.setPhoto(CategoryPhoto.create(it)) }
        }

        binding.toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        observePhoto()
        observeLoading()
        observeEvents()
        return binding.root
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                CategoryEditorViewModel.Event.CloseEditor -> {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(CategoriesEditorFragment.REFRESH_CATEGORIES, true)
                    closeFragment()
                }
                is CategoryEditorViewModel.Event.ShowError -> TODO()
            }
        }
    }

    private fun closeFragment() {
        findNavController().navigateUp()
    }

    private fun observeLoading() {
        viewModel.isLoading.observeOnLifecycle(viewLifecycleOwner) { isLoading ->
            if (isLoading)
                loadingDialog.show()
            else
                loadingDialog.close()
        }
    }

    private fun observePhoto() {
        viewModel.categoryPhoto.observeOnLifecycle(viewLifecycleOwner) {
            it?.run {
                binding.image.load(it.urlOrUri)
            }
        }
    }
}