package com.yurii.foody.screens.admin.products.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yurii.foody.R
import com.yurii.foody.databinding.FragmentEditCreateProductBinding
import com.yurii.foody.ui.ErrorDialog
import com.yurii.foody.ui.LoadingDialog
import com.yurii.foody.ui.UploadPhotoDialog
import com.yurii.foody.utils.Injector
import com.yurii.foody.utils.hideKeyboard
import com.yurii.foody.utils.observeOnLifecycle

class ProductEditorFragment : Fragment() {
    private val args: ProductEditorFragmentArgs by navArgs()
    private val viewModel: ProductEditorViewModel by viewModels {
        Injector.provideProductEditorViewModel(
            requireActivity().application,
            args.productIdToEdit
        )
    }
    private val uploadImageDialog: UploadPhotoDialog by lazy { UploadPhotoDialog(requireContext(), requireActivity().activityResultRegistry) }
    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(requireContext()) }
    private val errorDialog by lazy { ErrorDialog(requireContext()) }
    private lateinit var binding: FragmentEditCreateProductBinding
    private val imagesListAdapter = ImagesListAdapter(this::onAddNewAdditionalImage, this::onDeleteAdditionalImage)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_create_product, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.additionalImages.adapter = imagesListAdapter

        binding.action.text = getString(if (viewModel.isEditMode) R.string.label_save else R.string.label_create)

        binding.defaultImage.setOnClickListener {
            uploadImageDialog.show { viewModel.addMainPhoto(ProductPhoto.create(it)) }
        }

        binding.toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        observeAdditionalImages()
        observeMainPhoto()
        observeCategories()
        observeLoadingState()
        observeEvents()

        return binding.root
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
                is ProductEditorViewModel.Event.ShowError -> errorDialog.show(event.exception.message ?: "No error message")
                ProductEditorViewModel.Event.CloseEditor -> closeFragment()
            }
        }
    }

    private fun closeFragment() {
        findNavController().navigateUp()
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observeOnLifecycle(viewLifecycleOwner) { isLoading ->
            hideKeyboard()
            if (isLoading)
                loadingDialog.show()
            else
                loadingDialog.close()
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