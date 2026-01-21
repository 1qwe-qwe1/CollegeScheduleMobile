package com.example.collegeschedule.data.dataStore

import android.content.Context
import androidx.compose.remote.creation.first
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Создаем DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        // Ключи для хранения данных
        private val FAVORITE_GROUPS_KEY = stringSetPreferencesKey("favorite_groups")
        private val MAIN_GROUP_KEY = stringPreferencesKey("main_group")
        private val IS_MAIN_GROUP_SET_KEY = booleanPreferencesKey("is_main_group_set")
    }

    // Избранные группы
    val favoriteGroups: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
        }

    // Основная группа
    val mainGroup: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[MAIN_GROUP_KEY]
        }

    val isMainGroupSet: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_MAIN_GROUP_SET_KEY] ?: false
        }

    // Добавить/удалить группу из избранного
    suspend fun toggleFavoriteGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
            val newFavorites = if (currentFavorites.contains(groupName)) {
                currentFavorites - groupName
            } else {
                currentFavorites + groupName
            }
            preferences[FAVORITE_GROUPS_KEY] = newFavorites
        }
    }

    // Проверить, является ли группа избранной
    suspend fun isGroupFavorite(groupName: String): Boolean {
        val preferences = context.dataStore.data.map { it[FAVORITE_GROUPS_KEY] }.first()
        return preferences?.contains(groupName) ?: false
    }

    // Установить основную группу
    suspend fun setMainGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            preferences[MAIN_GROUP_KEY] = groupName
            preferences[IS_MAIN_GROUP_SET_KEY] = true
        }
    }

    // Очистить основную группу
    suspend fun clearMainGroup() {
        context.dataStore.edit { preferences ->
            preferences.remove(MAIN_GROUP_KEY)
            preferences[IS_MAIN_GROUP_SET_KEY] = false
        }
    }

    // Получить все настройки сразу
    suspend fun getPreferences(): PreferencesData {
        val favorites = context.dataStore.data.map { it[FAVORITE_GROUPS_KEY] }.first() ?: emptySet()
        val mainGroup = context.dataStore.data.map { it[MAIN_GROUP_KEY] }.first()
        val isMainSet = context.dataStore.data.map { it[IS_MAIN_GROUP_SET_KEY] }.first() ?: false

        return PreferencesData(
            favoriteGroups = favorites,
            mainGroup = mainGroup,
            isMainGroupSet = isMainSet
        )
    }
}

data class PreferencesData(
    val favoriteGroups: Set<String>,
    val mainGroup: String?,
    val isMainGroupSet: Boolean
)