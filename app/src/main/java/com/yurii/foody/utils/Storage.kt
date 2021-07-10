package com.yurii.foody.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yurii.foody.api.User
import com.yurii.foody.api.UserRoleEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

interface AuthDataStorageInterface {
    val authData: Flow<AuthDataStorage.Data?>
    val selectedRole: Flow<UserRoleEnum?>
    val currentUser: Flow<User?>
    val userRole: Flow<UserRoleEnum?>
    var isRoleConfirmed: Flow<Boolean>
    suspend fun setUserRoleStatus(isConfirmed: Boolean)
    suspend fun saveUserRole(role: UserRoleEnum?)
    suspend fun saveUser(user: User?)
    suspend fun saveSelectedUserRole(role: UserRoleEnum?)
    suspend fun saveAuthData(data: AuthDataStorage.Data?)
    suspend fun cleanAllAuthData()
}

class AuthDataStorage private constructor(private val dataStore: DataStore<Preferences>) : AuthDataStorageInterface {
    data class Data(val token: String, val email: String, val userId: Long)

    override val authData: Flow<Data?> = dataStore.data.map { preferences ->
        Data(
            token = preferences[KEY_AUTH_TOKEN] ?: return@map null,
            email = preferences[KEY_AUTH_EMAIL] ?: return@map null,
            userId = preferences[KEY_AUTH_USER_ID] ?: return@map null
        )
    }

    override val selectedRole: Flow<UserRoleEnum?> = dataStore.data.map { preferences ->
        val role = preferences[KEY_SELECTED_USER_ROLE] ?: return@map null
        UserRoleEnum.toEnum(role)
    }
    override val currentUser: Flow<User?> = dataStore.data.map { preferences ->
        User(
            id = preferences[KEY_USER_ID] ?: return@map null,
            firstName = preferences[KEY_USER_FIRST_NAME] ?: return@map null,
            lastName = preferences[KEY_USER_LAST_NAME] ?: return@map null,
            email = preferences[KEY_USER_EMAIL] ?: return@map null,
            phoneNumber = preferences[KEY_USER_PHONE_NUMBER] ?: return@map null,
            isEmailConfirmed = preferences[KEY_USER_IS_CONFIRMED] ?: return@map null

        )
    }

    override val userRole: Flow<UserRoleEnum?> = dataStore.data.map { preferences ->
        val role = preferences[KEY_USER_ROLE] ?: return@map null
        UserRoleEnum.toEnum(role)
    }

    override var isRoleConfirmed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ROLE_CONFIRMED] ?: false
    }

    override suspend fun setUserRoleStatus(isConfirmed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ROLE_CONFIRMED] = isConfirmed
        }
    }

    override suspend fun saveUserRole(role: UserRoleEnum?) {
        dataStore.edit { preferences ->
            role?.run { preferences[KEY_USER_ROLE] = this.role } ?: preferences.remove(KEY_USER_ROLE)
        }
    }

    override suspend fun saveUser(user: User?) {
        dataStore.edit { preferences ->
            user?.run {
                preferences[KEY_USER_ID] = user.id
                preferences[KEY_USER_FIRST_NAME] = user.firstName
                preferences[KEY_USER_LAST_NAME] = user.lastName
                preferences[KEY_USER_PHONE_NUMBER] = user.phoneNumber
                preferences[KEY_USER_IS_CONFIRMED] = user.isEmailConfirmed
                preferences[KEY_USER_EMAIL] = user.email
            } ?: kotlin.run {
                preferences.remove(KEY_USER_ID)
                preferences.remove(KEY_USER_FIRST_NAME)
                preferences.remove(KEY_USER_LAST_NAME)
                preferences.remove(KEY_USER_PHONE_NUMBER)
                preferences.remove(KEY_USER_IS_CONFIRMED)
                preferences.remove(KEY_USER_EMAIL)
            }
        }
    }

    override suspend fun saveSelectedUserRole(role: UserRoleEnum?) {
        dataStore.edit { preferences ->
            role?.run { preferences[KEY_SELECTED_USER_ROLE] = this.role } ?: preferences.remove(KEY_SELECTED_USER_ROLE)
        }
    }

    override suspend fun saveAuthData(data: Data?) {
        dataStore.edit { preferences ->
            data?.run {
                preferences[KEY_AUTH_TOKEN] = token
                preferences[KEY_AUTH_EMAIL] = email
                preferences[KEY_AUTH_USER_ID] = userId
            } ?: kotlin.run {
                preferences.remove(KEY_AUTH_TOKEN)
                preferences.remove(KEY_AUTH_EMAIL)
                preferences.remove(KEY_AUTH_USER_ID)
            }
        }
    }

    override suspend fun cleanAllAuthData() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("key_auth_token")
        private val KEY_AUTH_EMAIL = stringPreferencesKey("key_auth_email")
        private val KEY_AUTH_USER_ID = longPreferencesKey("key_auth_user_id")

        private val KEY_USER_ID = longPreferencesKey("key_user_id")
        private val KEY_USER_FIRST_NAME = stringPreferencesKey("key_user_first_name")
        private val KEY_USER_LAST_NAME = stringPreferencesKey("key_user_last_name")
        private val KEY_USER_PHONE_NUMBER = stringPreferencesKey("key_user_phone_number")
        private val KEY_USER_IS_CONFIRMED = booleanPreferencesKey("key_user_is_confirmed")
        private val KEY_USER_EMAIL = stringPreferencesKey("key_user_email")

        private val KEY_USER_ROLE_CONFIRMED = booleanPreferencesKey("key_user_role_confirmed")
        private val KEY_USER_ROLE = stringPreferencesKey("key_user_role")
        private val KEY_SELECTED_USER_ROLE = stringPreferencesKey("key_selected_user_role")

        private var INSTANCE: AuthDataStorage? = null

        fun create(context: Context): AuthDataStorage {
            if (INSTANCE == null)
                synchronized(AuthDataStorage::class.java) { INSTANCE = AuthDataStorage(context.authDataStore) }
            return INSTANCE!!
        }
    }
}