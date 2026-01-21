package com.example.collegeschedule.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.collegeschedule.data.dataStore.AppPreferences
import com.example.collegeschedule.data.dto.GroupDto
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import com.example.collegeschedule.data.repository.ScheduleRepository
import com.example.collegeschedule.data.network.RetrofitInstance
import com.example.collegeschedule.utils.getWeekDateRange
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(RetrofitInstance.api)
    private val preferences = AppPreferences(application.applicationContext)

    // LiveData для групп с начальным значением пустого списка
    private val _groups = MutableLiveData<List<GroupDto>>(emptyList())
    val groups: LiveData<List<GroupDto>> = _groups

    // LiveData для выбранной группы
    private val _selectedGroup = MutableLiveData<GroupDto?>(null)
    val selectedGroup: LiveData<GroupDto?> = _selectedGroup

    // LiveData для расписания с начальным значением пустого списка
    private val _schedule = MutableLiveData<List<ScheduleByDateDto>>(emptyList())
    val schedule: LiveData<List<ScheduleByDateDto>> = _schedule

    // LiveData для состояния загрузки с начальным значением false
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData для ошибок
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // LiveData для избранных групп с начальным значением пустого множества
    private val _favoriteGroups = MutableLiveData<Set<String>>(emptySet())
    val favoriteGroups: LiveData<Set<String>> = _favoriteGroups

    // LiveData для основной группы
    private val _mainGroup = MutableLiveData<String?>(null)
    val mainGroup: LiveData<String?> = _mainGroup

    // Флаг для отслеживания первой загрузки
    private var isInitialLoad = true

    // Загрузить все группы
    fun loadAllGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val groupsList = repository.loadAllGroups()
                _groups.value = groupsList

                // Только при первой загрузке выбираем группу автоматически
                if (isInitialLoad) {
                    selectInitialGroup(groupsList)
                    isInitialLoad = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки групп: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Логика выбора начальной группы
    private suspend fun selectInitialGroup(groupsList: List<GroupDto>) {
        val prefsData = preferences.getPreferences()
        _favoriteGroups.value = prefsData.favoriteGroups
        _mainGroup.value = prefsData.mainGroup

        val groupToSelect = when {
            // 1. Если есть основная группа - выбираем её
            !prefsData.mainGroup.isNullOrEmpty() -> {
                groupsList.find { it.groupName == prefsData.mainGroup }
            }
            // 2. Если нет основной, но есть избранные - выбираем первую из избранных
            prefsData.favoriteGroups.isNotEmpty() -> {
                groupsList.find { it.groupName in prefsData.favoriteGroups }
            }
            // 3. Иначе выбираем ИС-12 или первую группу в списке
            else -> {
                groupsList.find { it.groupName == "ИС-12" } ?: groupsList.firstOrNull()
            }
        }

        groupToSelect?.let { selectGroup(it) }
    }

    // Выбрать группу
    fun selectGroup(group: GroupDto) {
        _selectedGroup.value = group
        loadScheduleForGroup(group.groupName)
    }

    // Выбрать группу по имени (для навигации из избранного)
    fun selectGroupByName(groupName: String) {
        viewModelScope.launch {
            val group = _groups.value?.find { it.groupName == groupName }
            group?.let { selectGroup(it) }
        }
    }

    // Загрузить расписание для группы
    fun loadScheduleForGroup(groupName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val (start, end) = getWeekDateRange()
                val scheduleList = repository.loadSchedule(groupName, start, end)
                _schedule.value = scheduleList
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки расписания: ${e.message}"
                _schedule.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Загрузить настройки из DataStore
    fun loadPreferences() {
        viewModelScope.launch {
            try {
                val prefsData = preferences.getPreferences()
                _favoriteGroups.value = prefsData.favoriteGroups
                _mainGroup.value = prefsData.mainGroup
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки настроек: ${e.message}"
            }
        }
    }

    // Добавить/удалить группу из избранного
    fun toggleFavoriteGroup(groupName: String) {
        viewModelScope.launch {
            try {
                preferences.toggleFavoriteGroup(groupName)
                // Обновляем состояние
                val currentFavorites = _favoriteGroups.value ?: emptySet()
                val newFavorites = if (currentFavorites.contains(groupName)) {
                    currentFavorites - groupName
                } else {
                    currentFavorites + groupName
                }
                _favoriteGroups.value = newFavorites
            } catch (e: Exception) {
                _error.value = "Ошибка обновления избранного: ${e.message}"
            }
        }
    }

    // Установить основную группу
    fun setMainGroup(groupName: String) {
        viewModelScope.launch {
            try {
                // Автоматически добавляем в избранные при установке основной
                val currentFavorites = _favoriteGroups.value ?: emptySet()
                if (!currentFavorites.contains(groupName)) {
                    preferences.toggleFavoriteGroup(groupName)
                    val newFavorites = currentFavorites + groupName
                    _favoriteGroups.value = newFavorites
                }

                preferences.setMainGroup(groupName)
                _mainGroup.value = groupName
            } catch (e: Exception) {
                _error.value = "Ошибка установки основной группы: ${e.message}"
            }
        }
    }

    // Очистить основную группу
    fun clearMainGroup() {
        viewModelScope.launch {
            try {
                preferences.clearMainGroup()
                _mainGroup.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка очистки основной группы: ${e.message}"
            }
        }
    }
}