package com.yurii.foody.utils

import android.content.Context
import com.yurii.foody.api.Service
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.authorization.confirmation.ConfirmationViewModel
import com.yurii.foody.authorization.loading.LoadingViewModel
import com.yurii.foody.authorization.login.LogInViewModel
import com.yurii.foody.authorization.role.ChooseRoleViewModel

object Injector {

    private fun provideAuthorizationRepository(context: Context) = AuthorizationRepository.create(
        authDataStorage = AuthDataStorage.create(context),
        api = Service
    )

    fun provideChooseRoleViewModel(context: Context, selectNewRole: Boolean) =
        ChooseRoleViewModel.Factory(repository = provideAuthorizationRepository(context), selectNewRole = selectNewRole)

    fun provideLogInViewModel(context: Context) = LogInViewModel.Factory(repository = provideAuthorizationRepository(context))

    fun provideLoadingViewModel(context: Context) = LoadingViewModel.Factory(repository = provideAuthorizationRepository(context))

    fun provideConfirmationViewModel(context: Context, mode: ConfirmationFragment.Mode) =
        ConfirmationViewModel.Factory(provideAuthorizationRepository(context), mode)
}