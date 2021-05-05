package com.yurii.foody

import com.yurii.foody.authorization.AuthorizationRepository
import com.yurii.foody.authorization.confirmation.ConfirmationFragment
import com.yurii.foody.authorization.confirmation.ConfirmationViewModel
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
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ConfirmationViewModelUnitTest {
    private val authData = AuthDataStorage.Data(token = "token123", email = "test@email.com", userId = 1)
    private val fakeStorage: AuthDataStorageInterface = FakeAuthStorage(authData, mockedIsRoleConfirmed = false)
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
    fun message_emailIsNotConfirmed() = runBlocking {
        val viewModel = ConfirmationViewModel(repository, ConfirmationFragment.Mode.EMAIL_IS_NOT_CONFIRMED)
        assertThat(viewModel.showMessage.first(), `is`(ConfirmationFragment.Mode.EMAIL_IS_NOT_CONFIRMED))
    }

    @Test
    fun message_roleIsNotConfirmed() = runBlocking {
        val viewModel = ConfirmationViewModel(repository, ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED)
        assertThat(viewModel.showMessage.first(), `is`(ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED))
    }

    @Test
    fun logOut_eventSend() = runBlocking {
        val viewModel = ConfirmationViewModel(repository, ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED)
        viewModel.onLogOut()
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(ConfirmationViewModel.Event.NavigateToAuthorizationFragment))
        }
    }

    @Test
    fun logOut_dataIsClear() = runBlocking {
        val viewModel = ConfirmationViewModel(repository, ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED)
        viewModel.onLogOut()
        assertThat(repository.getAuthenticationData(), `is`(nullValue()))
    }

    @Test
    fun changeRole_eventSend() = runBlocking {
        val viewModel = ConfirmationViewModel(repository, ConfirmationFragment.Mode.ROLE_IS_NOT_CONFIRMED)
        viewModel.onChangeRole()
        withTimeout(1000) {
            assertThat(viewModel.eventFlow.first(), `is`(ConfirmationViewModel.Event.NavigateToChoosingRoleScreen))
        }
    }
}