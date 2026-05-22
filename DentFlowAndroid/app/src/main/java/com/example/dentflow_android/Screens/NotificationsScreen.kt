package com.example.dentflow_android.Screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dentflow_android.data.ViewModel.NotificationViewModel
import com.example.dentflow_android.data.remote.NotificationDTO
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel = hiltViewModel()
) {
    // Obserwujemy stan powiadomień
    val notifications by viewModel.notifications.collectAsState()

    // Ładowanie powiadomień przy wejściu na ekran - ViewModel sam wie, jakie jest ID użytkownika
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Powiadomienia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Przycisk "Oznacz wszystkie" - wywołanie bez parametrów
            TextButton(onClick = { viewModel.markAllAsRead() }) {
                Text("Oznacz wszystkie")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Brak nowych powiadomień", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            if (!notification.read) {
                                viewModel.markRead(notification.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationDTO,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        "NEW_APPOINTMENT" -> Icons.Default.AddCircle
        "CANCELLED" -> Icons.Default.Cancel
        "REMINDER" -> Icons.Default.NotificationsActive
        else -> Icons.Default.Info
    }

    val iconColor = when (notification.type) {
        "NEW_APPOINTMENT" -> Color(0xFF4CAF50)
        "CANCELLED" -> MaterialTheme.colorScheme.error
        "REMINDER" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    val formattedTime = remember(notification.createdAt) {
        try {
            val zdt = ZonedDateTime.parse(notification.createdAt)
            zdt.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM", Locale("pl")))
        } catch (e: Exception) {
            notification.createdAt.take(16).replace("T", " ")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(if (notification.read) 0.dp else 4.dp),
        border = if (!notification.read)
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        else
            null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
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
                        text = when(notification.type) {
                            "NEW_APPOINTMENT" -> "Nowa wizyta"
                            "CANCELLED" -> "Wizyta odwołana"
                            "REMINDER" -> "Przypomnienie"
                            else -> "System"
                        },
                        fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (notification.read)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                    if (!notification.read) {
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
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}