package com.yurii.foody.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yurii.foody.api.UserRoleEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

class AuthDataStorage private constructor(private val dataStore: DataStore<Preferences>) {
    data class Data(val token: String, val email: String, val userId: Int)

    val authData: Flow<Data> = dataStore.data.map { preferences ->
        Data(
            token = preferences[KEY_TOKEN]!!,
            email = preferences[KEY_EMAIL]!!,
            userId = preferences[KEY_USER_ID]!!
        )
    }

    val selectedRole: Flow<UserRoleEnum> = dataStore.data.map { preferences ->
        UserRoleEnum.toEnum(preferences[KEY_USER_ROLE]!!)
    }

    suspend fun clearUserAuth() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun saveUserRole(role: UserRoleEnum) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ROLE] = role.role
        }
    }

    suspend fun save(data: Data) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = data.token
            preferences[KEY_EMAIL] = data.email
            preferences[KEY_USER_ID] = data.userId
        }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("key_token")
        private val KEY_EMAIL = stringPreferencesKey("key_email")
        private val KEY_USER_ID = intPreferencesKey("key_user_id")
        private val KEY_USER_ROLE = stringPreferencesKey("key_user_role")

        private var INSTANCE: AuthDataStorage? = null

        fun create(context: Context): AuthDataStorage {
            if (INSTANCE == null)
                synchronized(AuthDataStorage::class.java) { INSTANCE = AuthDataStorage(context.authDataStore) }
            return INSTANCE!!
        }
    }
}