package com.example.collegeschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.collegeschedule.data.api.ScheduleApi
import com.example.collegeschedule.data.dto.GroupDto
import com.example.collegeschedule.ui.theme.CollegeScheduleTheme
import com.example.collegeschedule.ui.viewmodel.ScheduleViewModel
import com.example.collegeschedule.utils.ScheduleScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollegeScheduleTheme {
                CollegeScheduleApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeScheduleApp() {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    val scheduleViewModel: ScheduleViewModel = viewModel()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                        if (it == AppDestinations.HOME) {
                            scheduleViewModel.loadPreferences()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> {
                    ScheduleScreen(scheduleViewModel)
                }

                AppDestinations.FAVORITES -> {
                    FavoritesScreen(
                        viewModel = scheduleViewModel,
                        onGroupClick = { groupName ->
                            currentDestination = AppDestinations.HOME
                            scheduleViewModel.selectGroupByName(groupName)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppDestinations.PROFILE -> {
                    Text(
                        "Профиль студента",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: ScheduleViewModel,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoriteGroups.observeAsState(emptySet())
    val mainGroup by viewModel.mainGroup.observeAsState()
    val groups by viewModel.groups.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadAllGroups()
        viewModel.loadPreferences()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Избранные группы",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет избранных групп",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val favoriteGroupsList = groups
                .filter { it.groupName in favorites }
                .sortedWith(
                    compareByDescending<GroupDto> { it.groupName == mainGroup }
                        .thenBy { it.groupName }
                )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteGroupsList) { group ->
                    FavoriteGroupItem(
                        groupName = group.groupName,
                        isMainGroup = group.groupName == mainGroup,
                        onClick = {
                            onGroupClick(group.groupName)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteGroupItem(
    groupName: String,
    isMainGroup: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMainGroup)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isMainGroup) FontWeight.Bold else FontWeight.Normal
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMainGroup) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Основная группа",
                        tint = Color.Yellow,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Избранное",
                    tint = Color.Red
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    HOME("Главная", Icons.Default.Home),
    FAVORITES("Избранное", Icons.Default.Favorite),
    PROFILE("Профиль", Icons.Default.AccountBox),
}
