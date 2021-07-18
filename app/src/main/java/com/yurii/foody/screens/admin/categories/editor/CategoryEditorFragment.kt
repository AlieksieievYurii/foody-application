package com.yurii.foody.screens.admin.categories.editor

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentEditCreateCategoryBinding
import com.yurii.foody.screens.admin.categories.CategoriesEditorFragment
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class CategoryEditorFragment : Fragment(R.layout.fragment_edit_create_category) {
    private val args: CategoryEditorFragmentArgs by navArgs()
    private val binding: FragmentEditCreateCategoryBinding by viewBinding()
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val uploadImageDialog: UploadPhotoDialog by lazy { UploadPhotoDialog(requireContext(), requireActivity().activityResultRegistry) }
    private val viewModel: CategoryEditorViewModel by viewModels {
        Injector.provideCategoryEditorViewModel(requireActivity().application, args.categoryIdToEdit)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.title = getString(if (viewModel.isEditMode) R.string.label_edit_category else R.string.label_create_category)
        binding.action.text = getString(if (viewModel.isEditMode) R.string.label_save else R.string.label_create)

        binding.image.setOnClickListener {
            uploadImageDialog.show { viewModel.setPhoto(CategoryPhoto.create(it)) }
        }

        binding.toolbar.setNavigationOnClickListener { closeFragment() }

        observePhoto()
        observeLoading()
        observeEvents()
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) {
            when (it) {
                CategoryEditorViewModel.Event.CloseEditor -> {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(CategoriesEditorFragment.REFRESH_CATEGORIES, true)
                    closeFragment()
                }
                is CategoryEditorViewModel.Event.ShowError -> errorDialog.show(it.exception.message ?: getString(R.string.label_no_message))
            }
        }
    }

    private fun observeLoading() {
        viewModel.isLoading.observeOnLifecycle(viewLifecycleOwner) { isLoading ->
            hideKeyboard()
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

    override fun onDestroy() {
        super.onDestroy()
        uploadImageDialog.dismiss()
    }
}