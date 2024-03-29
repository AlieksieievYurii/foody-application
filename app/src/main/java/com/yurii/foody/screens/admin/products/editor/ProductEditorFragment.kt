package com.yurii.foody.screens.admin.products.editor

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentEditCreateProductBinding
import com.yurii.foody.screens.admin.products.ProductsEditorFragment
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.closeFragment
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class ProductEditorFragment : Fragment(R.layout.fragment_edit_create_product) {
    private val args: ProductEditorFragmentArgs by navArgs()
    private val viewModel: ProductEditorViewModel by viewModels {
        Injector.provideProductEditorViewModel(requireActivity().application, args.productIdToEdit)
    }
    private val uploadImageDialog: UploadPhotoDialog by lazy { UploadPhotoDialog(requireContext(), requireActivity().activityResultRegistry) }
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private val binding: FragmentEditCreateProductBinding by viewBinding()
    private val imagesListAdapter = ImagesListAdapter(this::onAddNewAdditionalImage, this::onDeleteAdditionalImage)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.additionalImages.adapter = imagesListAdapter
        binding.toolbar.title = getString(if (viewModel.isEditMode) R.string.label_edit_product else R.string.label_create_product)
        binding.action.text = getString(if (viewModel.isEditMode) R.string.label_save else R.string.label_create)
        loadingDialog.observeState(viewModel.isLoading, viewLifecycleOwner) { hideKeyboard() }
        binding.defaultImage.setOnClickListener {
            uploadImageDialog.show { viewModel.addMainPhoto(ProductPhoto.create(it)) }
        }

        binding.toolbar.setNavigationOnClickListener { closeFragment() }

        observeAdditionalImages()
        observeMainPhoto()
        observeCategories()
        observeEvents()
    }

    private fun observeMainPhoto() {
        viewModel.mainPhoto.observeOnLifecycle(viewLifecycleOwner) { image ->
            image?.run {
                binding.defaultImage.load(this.urlOrUri)
            }
        }
    }

    private fun observeAdditionalImages() {
        viewModel.additionalImagesFlow.observeOnLifecycle(viewLifecycleOwner) {
            imagesListAdapter.submitList(it)
        }
    }

    private fun observeEvents() {
        viewModel.eventFlow.observeOnLifecycle(viewLifecycleOwner) { event ->
            when (event) {
                is ProductEditorViewModel.Event.ShowError -> errorDialog.show(event.exception.message ?: getString(R.string.label_no_message))
                ProductEditorViewModel.Event.CloseEditor -> {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(ProductsEditorFragment.REFRESH_PRODUCTS, true)
                    closeFragment()
                }
            }
        }
    }

    private fun onAddNewAdditionalImage() {
        uploadImageDialog.show {
            viewModel.addAdditionalImage(ProductPhoto.create(it))
        }
    }

    private fun askUserToConfirmDeletionImage(callback: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.label_delete_image)
            .setMessage(R.string.message_delete_image_confirmation)
            .setPositiveButton(R.string.label_yes) { _, _ -> callback.invoke() }
            .setNegativeButton(R.string.label_no) { _, _ -> }
            .show()
    }

    private fun onDeleteAdditionalImage(image: ProductPhoto) {
        askUserToConfirmDeletionImage {
            viewModel.removeAdditionalImage(image)
        }
    }

    private fun observeCategories() {
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mutableListOf<CategoryItem>())
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.category.adapter = arrayAdapter

        viewModel.categories.observeOnLifecycle(viewLifecycleOwner) { categories ->
            arrayAdapter.clear()
            arrayAdapter.add(CategoryItem.NoCategory)
            arrayAdapter.addAll(categories.map { it.toCategoryItem() })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uploadImageDialog.dismiss()
    }
}