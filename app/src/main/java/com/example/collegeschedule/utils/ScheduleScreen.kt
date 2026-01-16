package com.example.collegeschedule.utils


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.collegeschedule.data.dto.GroupDto
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import com.example.collegeschedule.data.network.RetrofitInstance
import com.example.collegeschedule.data.repository.ScheduleRepository
import com.example.collegeschedule.ui.schedule.ScheduleList
import kotlinx.coroutines.launch

@Composable
fun ScheduleScreen() {
    val repository = remember { ScheduleRepository(RetrofitInstance.api) }
    val coroutineScope = rememberCoroutineScope()

    var groups by remember { mutableStateOf<List<GroupDto>>(emptyList()) }
    var selectedGroup by remember { mutableStateOf<GroupDto?>(null) }
    var schedule by remember { mutableStateOf<List<ScheduleByDateDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                groups = repository.loadAllGroups()
                selectedGroup = groups.find { it.groupName == "ИС-12" } ?:
                        groups.firstOrNull()
            } catch (e: Exception) {
                error = "Ошибка загрузки групп: ${e.message}"
            }
        }
    }

    LaunchedEffect(selectedGroup) {
        selectedGroup?.let { group ->
            loading = true
            error = null
            try {
                schedule = repository.loadSchedule(group.groupName)
            } catch (e: Exception) {
                error = "Ошибка загрузки расписания: ${e.message}"
                schedule = emptyList()
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (groups.isNotEmpty()) {
            GroupDropdown(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelected = { group ->
                    selectedGroup = group
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedGroup != null) {
            Text(
                text = "Группа: ${selectedGroup!!.groupName}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            loading -> {
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
                                    coroutineScope.launch {
                                        loading = true
                                        try {
                                            schedule = repository.loadSchedule(it.groupName)
                                            error = null
                                        } catch (e: Exception) {
                                            error = e.message
                                        } finally {
                                            loading = false
                                        }
                                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDropdown(
    groups: List<GroupDto>,
    selectedGroup: GroupDto?,
    onGroupSelected: (GroupDto) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf(selectedGroup?.groupName ?: "") }
    var isExpanded by remember { mutableStateOf(false) }

    val filteredGroups = remember(searchText, groups) {
        if (searchText.isBlank()) groups
        else groups.filter { it.groupName.contains(searchText, ignoreCase = true) }
    }

    Column(modifier = modifier) {
        Text(
            text = "Выберите группу:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    isExpanded = true
                },
                label = { Text("Введите название группы") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { isExpanded = true }),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                if (filteredGroups.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Группа не найдена") },
                        onClick = { isExpanded = false }
                    )
                } else {
                    filteredGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.groupName) },
                            onClick = {
                                onGroupSelected(group)
                                searchText = group.groupName
                                isExpanded = false
                            }
                        )
                    }
                }
            }


        }
    }
}


