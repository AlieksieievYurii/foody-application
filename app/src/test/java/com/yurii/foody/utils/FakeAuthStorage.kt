package com.yurii.foody.utils

import com.yurii.foody.api.UserRoleEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthStorage(
    var mockedAuthData: AuthDataStorage.Data? = null,
    var mockedSelectedRole: UserRoleEnum? = null,
    var mockedUserRole: UserRoleEnum? = null,
    var mockedIsRoleConfirmed: Boolean = false
) : AuthDataStorageInterface {

    private val _authData: MutableStateFlow<AuthDataStorage.Data?> = MutableStateFlow(mockedAuthData)
    override val authData: Flow<AuthDataStorage.Data?> = _authData

    private val _selectedRole: MutableStateFlow<UserRoleEnum?> = MutableStateFlow(mockedSelectedRole)
    override val selectedRole: Flow<UserRoleEnum?> = _selectedRole

    private val _userRole: MutableStateFlow<UserRoleEnum?> = MutableStateFlow(mockedUserRole)
    override val userRole: Flow<UserRoleEnum?> = _userRole

    private val _isRoleConfirmed: MutableStateFlow<Boolean> = MutableStateFlow(mockedIsRoleConfirmed)
    override var isRoleConfirmed: Flow<Boolean> = _isRoleConfirmed

    override suspend fun setUserRoleStatus(isConfirmed: Boolean) {
        mockedIsRoleConfirmed = isConfirmed
        _isRoleConfirmed.value = isConfirmed
    }

    override suspend fun saveUserRole(role: UserRoleEnum?) {
        mockedUserRole = role
        _userRole.value = role
    }

    override suspend fun saveSelectedUserRole(role: UserRoleEnum?) {
        mockedSelectedRole = role
        _selectedRole.value = role
    }

    override suspend fun saveAuthData(data: AuthDataStorage.Data?) {
        mockedAuthData = data
        _authData.value = data
    }

    override suspend fun cleanAllAuthData() {
        setUserRoleStatus(false)
        saveUserRole(null)
        saveSelectedUserRole(null)
        saveAuthData(null)
    }
}