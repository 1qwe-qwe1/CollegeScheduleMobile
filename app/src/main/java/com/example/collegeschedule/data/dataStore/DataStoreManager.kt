package com.example.collegeschedule.data.dataStore

import android.content.Context
import androidx.compose.remote.creation.first
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(private val context: Context) {

    companion object {
        private val FAVORITE_GROUPS_KEY = stringSetPreferencesKey("favorite_groups")
        private val MAIN_GROUP_KEY = stringPreferencesKey("main_group")
    }

    val favoriteGroups: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
        }

    val mainGroup: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[MAIN_GROUP_KEY]
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

    // Установить основную группу (автоматически добавляет в избранные)
    suspend fun setMainGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            // Автоматически добавляем в избранные
            val currentFavorites = preferences[FAVORITE_GROUPS_KEY] ?: emptySet()
            if (!currentFavorites.contains(groupName)) {
                preferences[FAVORITE_GROUPS_KEY] = currentFavorites + groupName
            }

            // Устанавливаем как основную
            preferences[MAIN_GROUP_KEY] = groupName
        }
    }

    // Очистить основную группу
    suspend fun clearMainGroup() {
        context.dataStore.edit { preferences ->
            preferences.remove(MAIN_GROUP_KEY)
        }
    }

    // Получить все настройки сразу
    suspend fun getPreferences(): PreferencesData {
        val favorites = context.dataStore.data.map { it[FAVORITE_GROUPS_KEY] }.first() ?: emptySet()
        val mainGroup = context.dataStore.data.map { it[MAIN_GROUP_KEY] }.first()

        return PreferencesData(
            favoriteGroups = favorites,
            mainGroup = mainGroup
        )
    }
}

data class PreferencesData(
    val favoriteGroups: Set<String>,
    val mainGroup: String?
)