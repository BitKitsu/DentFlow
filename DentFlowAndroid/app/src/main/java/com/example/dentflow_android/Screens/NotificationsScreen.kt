package com.example.dentflow_android.Screens

import androidx.compose.foundation.BorderStroke // Import dla ramki
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class NotificationItem(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean
)

enum class NotificationType {
    NEW_APPOINTMENT, CANCELLED, REMINDER, SYSTEM
}

@Composable
fun NotificationsScreen() {
    val notifications = listOf(
        NotificationItem(1, "Nowa wizyta", "Pacjent Anna Nowak zapisała się na jutro o 10:30.", "10 min temu", NotificationType.NEW_APPOINTMENT, false),
        NotificationItem(2, "Wizyta odwołana", "Marek Wiśniewski odwołał wizytę (14:00).", "1 godz. temu", NotificationType.CANCELLED, false),
        NotificationItem(3, "Przypomnienie", "Pamiętaj o uzupełnieniu grafiku na przyszły tydzień.", "3 godz. temu", NotificationType.REMINDER, true),
        NotificationItem(4, "Aktualizacja systemu", "Wprowadzono nowe funkcje w panelu rozliczeń.", "Wczoraj", NotificationType.SYSTEM, true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Powiadomienia",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    // Logika wyboru ikony i koloru bez zmian...
    val icon = when (notification.type) {
        NotificationType.NEW_APPOINTMENT -> Icons.Default.AddCircle
        NotificationType.CANCELLED -> Icons.Default.Cancel
        NotificationType.REMINDER -> Icons.Default.NotificationsActive
        NotificationType.SYSTEM -> Icons.Default.Info
    }

    val iconColor = when (notification.type) {
        NotificationType.NEW_APPOINTMENT -> Color(0xFF4CAF50)
        NotificationType.CANCELLED -> MaterialTheme.colorScheme.error
        NotificationType.REMINDER -> Color(0xFFFF9800)
        NotificationType.SYSTEM -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),

        elevation = CardDefaults.cardElevation(0.dp),
        border = if (!notification.isRead)
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Ikona typu powiadomienia
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        // Zmieniamy kolor tytułu nowych na Secondary
                        color = if (notification.isRead)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
