package com.example.kidtracker.system

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kidtracker_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        // --- Child / Parent info ---
        private val PARENT_UID_KEY = stringPreferencesKey("parent_uid")
        private val CHILD_UID_KEY = stringPreferencesKey("child_uid")
        private val CHILD_NAME_KEY = stringPreferencesKey("child_name")
        private val CHILD_EMAIL_KEY = stringPreferencesKey("child_email")
        private val CHILD_AGE_KEY = stringPreferencesKey("child_age")
        private val CHILD_PHONE_KEY = stringPreferencesKey("child_phone")
        private val CHILD_COUNTRY_KEY = stringPreferencesKey("child_country")
        private val CHILD_TOKEN_KEY = stringPreferencesKey("child_token")

        // --- Game High Scores ---
        private val MEMORY_GAME_HIGH_SCORE_KEY = intPreferencesKey("memory_game_high_score")
        private val PATTERN_GAME_HIGH_LEVEL_KEY = intPreferencesKey("pattern_game_high_level")
        private val MOLE_GAME_HIGH_SCORE_KEY = intPreferencesKey("mole_game_high_score")

        // --- Inbox ---
        private val INBOX_MESSAGES_KEY = stringPreferencesKey("inbox_messages")

    }

    // --- Parent / Child Info Savers ---
    suspend fun saveParentUID(uid: String) = context.dataStore.edit { it[PARENT_UID_KEY] = uid }
    val parentUID: Flow<String?> = context.dataStore.data.map { it[PARENT_UID_KEY] }

    suspend fun saveChildUID(uid: String) = context.dataStore.edit { it[CHILD_UID_KEY] = uid }
    val childUID: Flow<String?> = context.dataStore.data.map { it[CHILD_UID_KEY] }

    suspend fun saveChildName(name: String) = context.dataStore.edit { it[CHILD_NAME_KEY] = name }
    val childNameFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_NAME_KEY] }

    suspend fun saveChildEmail(email: String) = context.dataStore.edit { it[CHILD_EMAIL_KEY] = email }
    val childEmailFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_EMAIL_KEY] }

    suspend fun saveChildAge(age: String) = context.dataStore.edit { it[CHILD_AGE_KEY] = age }
    val childAgeFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_AGE_KEY] }

    suspend fun saveChildPhone(phone: String) = context.dataStore.edit { it[CHILD_PHONE_KEY] = phone }
    val childPhoneFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_PHONE_KEY] }

    suspend fun saveChildCountry(country: String) = context.dataStore.edit { it[CHILD_COUNTRY_KEY] = country }
    val childCountryFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_COUNTRY_KEY] }

    suspend fun saveChildToken(token: String) = context.dataStore.edit { it[CHILD_TOKEN_KEY] = token }
    val childTokenFlow: Flow<String?> = context.dataStore.data.map { it[CHILD_TOKEN_KEY] }
    suspend fun getChildToken(): String? = childTokenFlow.firstOrNull()

    // --- Suspend getters ---
    suspend fun getParentUID(): String? = parentUID.firstOrNull()
    suspend fun getChildUID(): String? = childUID.firstOrNull()
    suspend fun getChildName(): String = childNameFlow.firstOrNull() ?: "Child"
    suspend fun getChildEmail(): String = childEmailFlow.firstOrNull() ?: "example@gmail.com"
    suspend fun getChildAge(): String = childAgeFlow.firstOrNull() ?: ""
    suspend fun getChildPhone(): String = childPhoneFlow.firstOrNull() ?: ""
    suspend fun getChildCountry(): String = childCountryFlow.firstOrNull() ?: ""

    // --- Clear all data ---
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // --- Memory Game High Score (lower moves = better) ---
    suspend fun saveMemoryGameHighScore(moves: Int) {
        val currentHigh = getMemoryGameHighScore()
        if (currentHigh == null || moves < currentHigh) {
            context.dataStore.edit { it[MEMORY_GAME_HIGH_SCORE_KEY] = moves }
        }
    }
    val memoryGameHighScoreFlow: Flow<Int?> = context.dataStore.data.map { it[MEMORY_GAME_HIGH_SCORE_KEY] }
    suspend fun getMemoryGameHighScore(): Int? = memoryGameHighScoreFlow.firstOrNull()

    // --- Pattern Game High Level (higher level = better) ---
    suspend fun savePatternGameHighLevel(level: Int) {
        val currentHigh = getPatternGameHighLevel()
        if (currentHigh == null || level > currentHigh) {
            context.dataStore.edit { it[PATTERN_GAME_HIGH_LEVEL_KEY] = level }
        }
    }
    val patternGameHighLevelFlow: Flow<Int?> = context.dataStore.data.map { it[PATTERN_GAME_HIGH_LEVEL_KEY] }
    suspend fun getPatternGameHighLevel(): Int? = patternGameHighLevelFlow.firstOrNull()

    // --- Whack-A-Mole High Score (higher score = better) ---
    suspend fun saveMoleGameHighScore(score: Int) {
        val currentHigh = getMoleGameHighScore()
        if (currentHigh == null || score > currentHigh) {
            context.dataStore.edit { it[MOLE_GAME_HIGH_SCORE_KEY] = score }
        }
    }
    val moleGameHighScoreFlow: Flow<Int?> = context.dataStore.data.map { it[MOLE_GAME_HIGH_SCORE_KEY] }
    suspend fun getMoleGameHighScore(): Int? = moleGameHighScoreFlow.firstOrNull()

    // Inbox Messages stored as JSON string
    suspend fun saveInboxMessages(messages: List<InboxMessage>) {
        val json = InboxMessage.toJson(messages)
        context.dataStore.edit {
            it[INBOX_MESSAGES_KEY] = json
        }
    }
    suspend fun getInboxMessages(): List<InboxMessage> {
        val json = context.dataStore.data.map { it[INBOX_MESSAGES_KEY] }.firstOrNull() ?: ""
        return InboxMessage.fromJson(json)
    }

}
