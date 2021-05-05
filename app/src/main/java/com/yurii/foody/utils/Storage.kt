package com.yurii.foody.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yurii.foody.api.UserRoleEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

interface AuthDataStorageInterface {
    val authData: Flow<AuthDataStorage.Data?>
    val selectedRole: Flow<UserRoleEnum?>
    val userRole: Flow<UserRoleEnum?>
    var isRoleConfirmed: Flow<Boolean>
    suspend fun setUserRoleStatus(isConfirmed: Boolean)
    suspend fun saveUserRole(role: UserRoleEnum?)
    suspend fun saveSelectedUserRole(role: UserRoleEnum?)
    suspend fun saveAuthData(data: AuthDataStorage.Data?)
    suspend fun cleanAllAuthData()
}

class AuthDataStorage private constructor(private val dataStore: DataStore<Preferences>) : AuthDataStorageInterface {
    data class Data(val token: String, val email: String, val userId: Int)

    override val authData: Flow<Data?> = dataStore.data.map { preferences ->
        Data(
            token = preferences[KEY_TOKEN] ?: return@map null,
            email = preferences[KEY_EMAIL] ?: return@map null,
            userId = preferences[KEY_USER_ID] ?: return@map null
        )
    }

    override val selectedRole: Flow<UserRoleEnum?> = dataStore.data.map { preferences ->
        val role = preferences[KEY_SELECTED_USER_ROLE] ?: return@map null
        UserRoleEnum.toEnum(role)
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

    override suspend fun saveSelectedUserRole(role: UserRoleEnum?) {
        dataStore.edit { preferences ->
            role?.run { preferences[KEY_SELECTED_USER_ROLE] = this.role } ?: preferences.remove(KEY_SELECTED_USER_ROLE)
        }
    }

    override suspend fun saveAuthData(data: Data?) {
        dataStore.edit { preferences ->
            data?.run {
                preferences[KEY_TOKEN] = token
                preferences[KEY_EMAIL] = email
                preferences[KEY_USER_ID] = userId
            } ?: kotlin.run {
                preferences.remove(KEY_TOKEN)
                preferences.remove(KEY_EMAIL)
                preferences.remove(KEY_USER_ID)
            }
        }
    }

    override suspend fun cleanAllAuthData() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("key_token")
        private val KEY_EMAIL = stringPreferencesKey("key_email")
        private val KEY_USER_ID = intPreferencesKey("key_user_id")
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