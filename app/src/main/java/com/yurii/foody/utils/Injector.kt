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
import com.yurii.foody.screens.cook.main.CookMainScreenViewModel
import com.yurii.foody.screens.cook.orders.CookOrdersViewModel
import com.yurii.foody.screens.personal.PersonalInformationViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injector {

    private fun provideAuthorizedApiService(context: Context): Service {
        val token = runBlocking {
            AuthDataStorage.create(context).authData.first()?.token
                ?: throw IllegalStateException("There is not token")
        }
        return Service(token)
    }

    private fun provideUnAuthorizedApiService() = Service()

    private fun provideUserRepository(context: Context) = UserRepository.create(
        authDataStorage = AuthDataStorage.create(context),
        api = provideAuthorizedApiService(context)
    )

    private fun provideUnAuthRepo(context: Context) = AuthorizationRepository(
        AuthDataStorage.create(context),
        provideUnAuthorizedApiService()
    )

    private fun provideProductRepository(context: Context) =
        ProductsRepository.create(api = provideAuthorizedApiService(context))

    fun provideChooseRoleViewModel(context: Context, selectNewRole: Boolean) =
        ChooseRoleViewModel.Factory(repository = provideUnAuthRepo(context), selectNewRole = selectNewRole)

    fun provideLogInViewModel(context: Context) =
        LogInViewModel.Factory(repository = provideUnAuthRepo(context))

    fun provideLoadingViewModel(context: Context) =
        LoadingViewModel.Factory(repository = provideUnAuthRepo(context))

    fun provideConfirmationViewModel(context: Context, mode: ConfirmationFragment.Mode) =
        ConfirmationViewModel.Factory(provideUnAuthRepo(context), mode)

    fun provideSignUpViewModel(context: Context) =
        SignUpViewModel.Factory(repository = provideUnAuthRepo(context))

    fun provideAdminPanelViewModel(context: Context) =
        AdminPanelViewModel.Factory(repository = provideUserRepository(context))

    fun provideProductsEditorViewModel(context: Context) =
        ProductsEditorViewModel.Factory(repository = provideProductRepository(context))

    fun provideProductEditorViewModel(application: Application, productIdToEdit: Long) = ProductEditorViewModel.Factory(
        application = application,
        productsRepository = provideProductRepository(application.applicationContext),
        productIdToEdit = if (productIdToEdit == -1L) null else productIdToEdit
    )

    fun provideCategoriesEditorViewModel(context: Context) =
        CategoriesEditorViewModel.Factory(repository = provideProductRepository(context))

    fun provideCategoryEditorViewModel(application: Application, categoryIdToEdit: Long) =
        CategoryEditorViewModel.Factory(
            application = application,
            productsRepository = provideProductRepository(application.applicationContext),
            categoryIdToEdit = if (categoryIdToEdit == -1L) null else categoryIdToEdit
        )

    fun provideRoleRequestsViewModel(context: Context) =
        RoleRequestsViewModel.Factory(userRepository = provideUserRepository(context))

    fun providePersonalInformationViewModel(context: Context) =
        PersonalInformationViewModel.Factory(provideUserRepository(context))

    fun provideClientMainScreenViewModel(context: Context) =
        ClientMainScreenViewModel.Factory(repository = provideUserRepository(context))

    fun provideProductsViewModel(context: Context) =
        ProductsViewModel.Factory(repository = provideProductRepository(context))

    fun provideProductDetailViewModel(context: Context, productId: Long) =
        ProductDetailViewModel.Factory(repository = provideProductRepository(context), productId = productId)

    fun provideCookMainScreenViewModel(context: Context) = CookMainScreenViewModel.Factory(
        repository = provideUserRepository(context)
    )

    fun provideCookOrdersViewModel(context: Context) =
        CookOrdersViewModel.Factory(productsRepository = provideProductRepository(context))
}