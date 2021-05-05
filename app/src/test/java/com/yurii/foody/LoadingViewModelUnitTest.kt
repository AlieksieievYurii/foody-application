package com.yurii.foody

import com.yurii.foody.api.User
import com.yurii.foody.api.UserRole
import com.yurii.foody.api.UserRoleEnum
import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.loading.LoadingViewModel
import com.yurii.foody.utils.AuthDataStorage
import com.yurii.foody.utils.AuthDataStorageInterface
import com.yurii.foody.utils.FakeApiService
import com.yurii.foody.utils.FakeAuthStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoadingViewModelUnitTest {
    private val authData = AuthDataStorage.Data(token = "token123", email = "test@email.com", userId = 1)
    private val fakeStorage: AuthDataStorageInterface = FakeAuthStorage()
    private val fakeApi = FakeApiService()
    private val repository = AuthorizationRepository(fakeStorage, fakeApi)

    private val dispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun noAuthenticatedYet_sendEventToNavigateToAuthorizationScree() = runBlockingTest {
        val viewModel = LoadingViewModel(repository)
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(LoadingViewModel.Event.NavigateToAuthenticationScreen))
        }
    }

    @Test
    fun authenticated_wrongCredentials_sendEventNavigateToAuthorizationScreen() = runBlocking {
        // Data preparation
        fakeStorage.saveAuthData(authData)
        fakeApi.doesGetUserThrowServerErrorUnauthorized = true
        /////////////////////////////////////

        val viewModel = LoadingViewModel(repository)
        assertTrue(fakeApi.isAuthenticatedServiceCreated)
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(LoadingViewModel.Event.NavigateToAuthenticationScreen))
        }
    }

    @Test
    fun authenticated_emailIsNotConfigured_sendEventNavigateToConfirmationScreen() = runBlocking {
        // Data preparation
        fakeStorage.saveAuthData(authData)
        fakeApi.users.add(User(authData.userId, authData.email, "Name", "Surname", "1111", false))
        /////////////////////////////////////

        val viewModel = LoadingViewModel(repository)
        assertTrue(fakeApi.isAuthenticatedServiceCreated)
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(LoadingViewModel.Event.NavigateToUserIsNotConfirmedScreen))
        }
    }

    @Test
    fun authenticated_sendEventNavigateToChooseRoleScreen() = runBlocking {
        // Data preparation
        fakeStorage.saveAuthData(authData)
        fakeApi.users.add(User(authData.userId, authData.email, "Name", "Surname", "1111", true))
        fakeApi.userRoles.add(UserRole(1, false, UserRoleEnum.CLIENT))
        /////////////////////////////////////

        val viewModel = LoadingViewModel(repository)
        assertTrue(fakeApi.isAuthenticatedServiceCreated)
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(LoadingViewModel.Event.NavigateToChooseRoleScreen))
        }
        assertThat(fakeStorage.userRole.first(), `is`(UserRoleEnum.CLIENT))
        assertThat(fakeStorage.isRoleConfirmed.first(), `is`(false))
    }

    @Test
    fun authenticated_roleIsDecreased_sendEventNavigateToChooseRoleScreen() = runBlocking {
        // Data preparation
        fakeStorage.saveAuthData(authData)
        fakeStorage.saveUserRole(UserRoleEnum.ADMINISTRATOR)
        fakeApi.users.add(User(authData.userId, authData.email, "Name", "Surname", "1111", true))
        fakeApi.userRoles.add(UserRole(1, true, UserRoleEnum.CLIENT))
        /////////////////////////////////////

        val viewModel = LoadingViewModel(repository)
        assertTrue(fakeApi.isAuthenticatedServiceCreated)
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(LoadingViewModel.Event.NavigateToChooseRoleScreen))
        }
        assertThat(fakeStorage.userRole.first(), `is`(UserRoleEnum.CLIENT))
        assertThat(fakeStorage.isRoleConfirmed.first(), `is`(true))
    }
}