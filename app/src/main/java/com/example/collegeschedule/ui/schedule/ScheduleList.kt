package com.example.collegeschedule.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.room.util.copy
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import com.example.collegeschedule.ui.components.GroupDropdown
import com.example.collegeschedule.data.dto.GroupDto
import com.example.collegeschedule.data.dto.LessonDto
import com.example.collegeschedule.data.dto.LessonGroupPart
import com.example.collegeschedule.ui.theme.getBuildingInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleList(data: List<ScheduleByDateDto>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(data) { day ->
            // День недели и дата
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    day.weekday,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    formatDate(day.lessonDate),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (day.lessons.isEmpty()) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        "Занятий нет",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Список занятий
                day.lessons.forEach { lesson ->
                    val buildingInfo = getBuildingInfo(lesson)
                    val buildingColor = buildingInfo.first
                    val buildingName = buildingInfo.second
                    val buildingAddress = buildingInfo.third

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        "Пара ${lesson.lessonNumber}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    lesson.time,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            lesson.groupParts.forEach { (part, info) ->
                                if (info != null) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = buildingColor.copy(alpha = 0.05f),
                                        border = BorderStroke(1.dp, buildingColor.copy(alpha = 0.1f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .fillMaxWidth()
                                        ) {
                                            if (part != LessonGroupPart.FULL) {
                                                val subgroupText = when (part) {
                                                    LessonGroupPart.SUB1 -> "Подгруппа 1"
                                                    LessonGroupPart.SUB2 -> "Подгруппа 2"
                                                    else -> part.name
                                                }
                                                Text(
                                                    subgroupText,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    modifier = Modifier.padding(bottom = 2.dp)
                                                )
                                            }

                                            Text(
                                                info.subject,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )

                                            Text(
                                                info.teacher,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )

                                            // Адресс
                                            Column(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(bottom = 2.dp)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = buildingColor,
                                                        modifier = Modifier.size(8.dp)
                                                    ) {}

                                                    Text(
                                                        "$buildingName, $buildingAddress",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = buildingColor,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                }

                                                Text(
                                                    "Аудитория: ${info.classroom}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    modifier = Modifier.padding(start = 14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val date = LocalDateTime.parse(dateString, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString
    }
}