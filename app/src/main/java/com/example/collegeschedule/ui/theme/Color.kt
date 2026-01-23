package com.example.collegeschedule.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.collegeschedule.data.dto.LessonDto

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Composable
fun getBuildingInfo(lesson: LessonDto): Triple<Color, String, String> {
    val buildingInfo = lesson.groupParts.values.firstOrNull { it != null }
    val buildingName = buildingInfo?.building ?: "Не указан"
    val buildingAddress = buildingInfo?.address ?: ""

    val buildingColor = when {
        buildingName.contains("Главный", ignoreCase = true) -> Color(0xFF2196F3)
        buildingName.contains("Учебный", ignoreCase = true) -> Color(0xFF4FC3F7)
        buildingName.contains("Лабораторный", ignoreCase = true) -> Color(0xFF0288D1)
        buildingName.contains("Спортивный", ignoreCase = true) -> Color(0xFF00BCD4)
        buildingName.contains("Библиотечный", ignoreCase = true) -> Color(0xFF0097A7)
        buildingName.contains("Институт ИТ", ignoreCase = true) -> Color(0xFF1976D2)
        buildingName.contains("Физический", ignoreCase = true) -> Color(0xFF03A9F4)
        buildingName.contains("Корпус инженерии", ignoreCase = true) -> Color(0xFF0288D1)
        buildingName.contains("Корпус Экономики", ignoreCase = true) -> Color(0xFF81D4FA)
        buildingName.contains("Корпус Психологии", ignoreCase = true) -> Color(0xFFB3E5FC)
        else -> MaterialTheme.colorScheme.primary
    }

    return Triple(buildingColor, buildingName, buildingAddress)
}