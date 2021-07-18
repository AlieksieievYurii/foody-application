package com.yurii.foody.utils

import android.app.Application
import android.content.Context
import com.yurii.foody.api.Service
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.authorization.confirmation.ConfirmationViewModel
import com.yurii.foody.authorization.loading.LoadingViewModel
import com.yurii.foody.authorization.login.LogInViewModel
import com.yurii.foody.authorization.role.ChooseRoleViewModel
import com.yurii.foody.authorization.signup.SignUpViewModel
import com.yurii.foody.screens.admin.categories.CategoriesEditorViewModel
import com.yurii.foody.screens.admin.categories.editor.CategoryEditorViewModel
import com.yurii.foody.screens.admin.main.AdminPanelViewModel
import com.yurii.foody.screens.admin.products.ProductsEditorViewModel
import com.yurii.foody.screens.admin.products.editor.ProductEditorViewModel
import com.yurii.foody.screens.admin.requests.RoleRequestsViewModel
import com.yurii.foody.screens.client.main.ClientMainScreenViewModel
import com.yurii.foody.screens.client.products.ProductsViewModel
import com.yurii.foody.screens.client.products.detail.ProductDetailViewModel
import com.yurii.foody.screens.personal.PersonalInformationViewModel
import com.yurii.foody.screens.personal.UserRepository

object Injector {

    private fun provideAuthorizationRepository(context: Context) = AuthorizationRepository.create(
        authDataStorage = AuthDataStorage.create(context),
        api = Service
    )

    private fun provideProductRepository() = ProductsRepository.create(api = Service)

    fun provideChooseRoleViewModel(context: Context, selectNewRole: Boolean) =
        ChooseRoleViewModel.Factory(repository = provideAuthorizationRepository(context), selectNewRole = selectNewRole)

    fun provideLogInViewModel(context: Context) = LogInViewModel.Factory(repository = provideAuthorizationRepository(context))

    fun provideLoadingViewModel(context: Context) = LoadingViewModel.Factory(repository = provideAuthorizationRepository(context))

    fun provideConfirmationViewModel(context: Context, mode: ConfirmationFragment.Mode) =
        ConfirmationViewModel.Factory(provideAuthorizationRepository(context), mode)

    fun provideSignUpViewModel(context: Context) = SignUpViewModel.Factory(repository = provideAuthorizationRepository(context))

    fun provideAdminPanelViewModel(context: Context) = AdminPanelViewModel.Factory(repository = provideAuthorizationRepository(context))

    fun provideProductsEditorViewModel() = ProductsEditorViewModel.Factory(repository = provideProductRepository())

    fun provideProductEditorViewModel(application: Application, productIdToEdit: Long) = ProductEditorViewModel.Factory(
        application = application,
        productsRepository = provideProductRepository(),
        productIdToEdit = if (productIdToEdit == -1L) null else productIdToEdit
    )

    fun provideCategoriesEditorViewModel() = CategoriesEditorViewModel.Factory(repository = provideProductRepository())
    fun provideCategoryEditorViewModel(application: Application, categoryIdToEdit: Long) = CategoryEditorViewModel.Factory(
        application = application,
        productsRepository = provideProductRepository(),
        categoryIdToEdit = if (categoryIdToEdit == -1L) null else categoryIdToEdit
    )

    fun provideRoleRequestsViewModel(context: Context) =
        RoleRequestsViewModel.Factory(authorizationRepository = provideAuthorizationRepository(context))

    fun providePersonalInformationViewModel(context: Context) = PersonalInformationViewModel.Factory(
        UserRepository(
            service = Service, authDataStorage = AuthDataStorage.create(context)
        )
    )

    fun provideClientMainScreenViewModel(context: Context) = ClientMainScreenViewModel.Factory(repository = provideAuthorizationRepository(context))
    fun provideProductsViewModel() = ProductsViewModel.Factory(repository = provideProductRepository())
    fun provideProductDetailViewModel(productId: Long) =
        ProductDetailViewModel.Factory(repository = provideProductRepository(), productId = productId)
}