package com.example.kidtracker.system

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kidtracker_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val PARENT_UID_KEY = stringPreferencesKey("parent_uid")
        private val CHILD_UID_KEY = stringPreferencesKey("child_uid")
        private val CHILD_NAME_KEY = stringPreferencesKey("child_name")
        private val CHILD_EMAIL_KEY = stringPreferencesKey("child_email")
        private val CHILD_AGE_KEY = stringPreferencesKey("child_age")
        private val CHILD_PHONE_KEY = stringPreferencesKey("child_phone")
        private val CHILD_COUNTRY_KEY = stringPreferencesKey("child_country")
        private val CHILD_TOKEN_KEY = stringPreferencesKey("child_token") // <-- added
    }

    // Savers
    suspend fun saveParentUID(uid: String) {
        context.dataStore.edit { prefs -> prefs[PARENT_UID_KEY] = uid }
    }
    val parentUID: Flow<String?> = context.dataStore.data.map { it[PARENT_UID_KEY] }

    suspend fun saveChildUID(uid: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_UID_KEY] = uid }
    }
    val childUID: Flow<String?> = context.dataStore.data.map { it[CHILD_UID_KEY] }

    suspend fun saveChildName(name: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_NAME_KEY] = name }
    }
    val childNameFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_NAME_KEY] }

    suspend fun saveChildEmail(email: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_EMAIL_KEY] = email }
    }
    val childEmailFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_EMAIL_KEY] }

    suspend fun saveChildAge(age: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_AGE_KEY] = age }
    }
    val childAgeFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_AGE_KEY] }

    suspend fun saveChildPhone(phone: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_PHONE_KEY] = phone }
    }
    val childPhoneFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_PHONE_KEY] }

    suspend fun saveChildCountry(country: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_COUNTRY_KEY] = country }
    }
    val childCountryFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_COUNTRY_KEY] }

    // <-- NEW: token handling
    suspend fun saveChildToken(token: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_TOKEN_KEY] = token }
    }
    val childTokenFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_TOKEN_KEY] }
    suspend fun getChildToken(): String? = childTokenFlow.firstOrNull()

    // Suspend getters
    suspend fun getParentUID(): String? = parentUID.firstOrNull()
    suspend fun getChildUID(): String? = childUID.firstOrNull()
    suspend fun getChildName(): String = childNameFlow.firstOrNull() ?: "Child"
    suspend fun getChildEmail(): String = childEmailFlow.firstOrNull() ?: "example@gmail.com"
    suspend fun getChildAge(): String = childAgeFlow.firstOrNull() ?: ""
    suspend fun getChildPhone(): String = childPhoneFlow.firstOrNull() ?: ""
    suspend fun getChildCountry(): String = childCountryFlow.firstOrNull() ?: ""

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
