package com.example.collegeschedule.utils


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Icon
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.collegeschedule.data.dto.GroupDto
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import com.example.collegeschedule.data.network.RetrofitInstance
import com.example.collegeschedule.data.repository.ScheduleRepository
import com.example.collegeschedule.ui.components.GroupDropdown
import com.example.collegeschedule.ui.schedule.ScheduleList
import com.example.collegeschedule.ui.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // Состояния из ViewModel
    val groups by viewModel.groups.observeAsState(emptyList())
    val selectedGroup by viewModel.selectedGroup.observeAsState()
    val schedule by viewModel.schedule.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    val favorites by viewModel.favoriteGroups.observeAsState(emptySet())
    val mainGroup by viewModel.mainGroup.observeAsState()

    // Загрузка данных при первом открытии
    LaunchedEffect(Unit) {
        viewModel.loadAllGroups()
        viewModel.loadPreferences()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Панель выбора группы (используем ваш компонент)
        if (groups.isNotEmpty()) {
            GroupDropdown(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelected = { group ->
                    viewModel.selectGroup(group)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Заголовок с названием группы и кнопками избранного/основной
        selectedGroup?.let { group ->
            GroupHeader(
                groupName = group.groupName,
                isFavorite = favorites.contains(group.groupName),
                isMainGroup = mainGroup == group.groupName,
                onFavoriteClick = {
                    coroutineScope.launch {
                        viewModel.toggleFavoriteGroup(group.groupName)
                    }
                },
                onMainGroupClick = {
                    coroutineScope.launch {
                        if (mainGroup == group.groupName) {
                            viewModel.clearMainGroup()
                        } else {
                            viewModel.setMainGroup(group.groupName)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Отображение состояния загрузки/ошибки/данных
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: $error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedGroup?.let {
                                    viewModel.loadScheduleForGroup(it.groupName)
                                }
                            }
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }
            selectedGroup == null && groups.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка групп...")
                }
            }
            else -> {
                ScheduleList(schedule)
            }
        }
    }
}

@Composable
fun GroupHeader(
    groupName: String,
    isFavorite: Boolean,
    isMainGroup: Boolean,
    onFavoriteClick: () -> Unit,
    onMainGroupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Название группы
        Text(
            text = "Группа: $groupName",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )

        // Кнопки избранного и основной группы
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            // Кнопка избранного
            IconButton(
                onClick = onFavoriteClick,
                enabled = !isMainGroup || isFavorite // Нельзя удалить из избранного если это основная группа
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }

            // Кнопка основной группы
            IconButton(
                onClick = onMainGroupClick
            ) {
                Icon(
                    imageVector = if (isMainGroup) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = if (isMainGroup) "Убрать основную группу" else "Сделать основной",
                    tint = if (isMainGroup) Color.Yellow else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


