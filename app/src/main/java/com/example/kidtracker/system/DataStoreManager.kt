package com.example.kidtracker.system

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "kidtracker_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val PARENT_UID_KEY = stringPreferencesKey("parent_uid")
        val CHILD_UID_KEY = stringPreferencesKey("child_uid")
        val CHILD_NAME_KEY = stringPreferencesKey("child_name")
        val CHILD_EMAIL_KEY = stringPreferencesKey("child_email")
        val CHILD_AGE_KEY = stringPreferencesKey("child_age")
        val CHILD_PHONE_KEY = stringPreferencesKey("child_phone")
        val CHILD_COUNTRY_KEY = stringPreferencesKey("child_country")
    }

    // ================= Parent UID =================
    suspend fun saveParentUID(uid: String) {
        context.dataStore.edit { prefs -> prefs[PARENT_UID_KEY] = uid }
    }
    val parentUID: Flow<String?> = context.dataStore.data.map { it[PARENT_UID_KEY] }

    // ================= Child UID =================
    suspend fun saveChildUID(uid: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_UID_KEY] = uid }
    }
    val childUID: Flow<String?> = context.dataStore.data.map { it[CHILD_UID_KEY] }

    // ================= Child Name =================
    suspend fun saveChildName(name: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_NAME_KEY] = name }
    }
    val childNameFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_NAME_KEY] }

    // ================= Child Email =================
    suspend fun saveChildEmail(email: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_EMAIL_KEY] = email }
    }
    val childEmailFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_EMAIL_KEY] }

    // ================= Child Age =================
    suspend fun saveChildAge(age: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_AGE_KEY] = age }
    }
    val childAgeFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_AGE_KEY] }

    // ================= Child Phone =================
    suspend fun saveChildPhone(phone: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_PHONE_KEY] = phone }
    }
    val childPhoneFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_PHONE_KEY] }

    // ================= Child Country =================
    suspend fun saveChildCountry(country: String) {
        context.dataStore.edit { prefs -> prefs[CHILD_COUNTRY_KEY] = country }
    }
    val childCountryFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_COUNTRY_KEY] }

    // ================= Suspend Getters =================
    suspend fun getParentUID(): String? = parentUID.firstOrNull()
    suspend fun getChildUID(): String? = childUID.firstOrNull()
    suspend fun getChildName(): String = childNameFlow.firstOrNull() ?: "Child"
    suspend fun getChildEmail(): String = childEmailFlow.firstOrNull() ?: "example@gmail.com"
    suspend fun getChildAge(): String = childAgeFlow.firstOrNull() ?: ""
    suspend fun getChildPhone(): String = childPhoneFlow.firstOrNull() ?: ""
    suspend fun getChildCountry(): String = childCountryFlow.firstOrNull() ?: ""

    // ================= Clear All =================
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
