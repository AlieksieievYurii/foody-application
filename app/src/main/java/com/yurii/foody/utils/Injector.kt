package com.yurii.foody.utils

import android.content.Context
import com.yurii.foody.api.Service
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.login.LogInViewModel

object Injector {

    private fun provideAuthorizationRepository(context: Context) = AuthorizationRepository(
        authDataStorage = AuthDataStorage.create(context),
        apiTokenAuth = Service.authService
    )

    fun provideLogInViewModel(context: Context): LogInViewModel.Factory =
        LogInViewModel.Factory(repository = provideAuthorizationRepository(context))
}