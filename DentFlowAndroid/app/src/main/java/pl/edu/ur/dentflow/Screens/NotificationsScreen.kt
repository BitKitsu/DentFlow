package pl.edu.ur.dentflow.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.edu.ur.dentflow.R
import pl.edu.ur.dentflow.data.ViewModel.NotificationViewModel
import pl.edu.ur.dentflow.data.remote.NotificationDTO
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Ekran powiadomień
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications  by viewModel.notifications.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val errorMessage   by viewModel.errorMessage.collectAsState()
    val unreadCount    by viewModel.unreadCount.collectAsState()

    // Filtr: 0 = Wszystkie, 1 = Nieprzeczytane
    var selectedFilter by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    val displayed = remember(notifications, selectedFilter) {
        if (selectedFilter == 1) notifications.filter { !it.read } else notifications
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // ── Nagłówek ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.notifications_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Odśwież
                IconButton(onClick = { viewModel.fetchNotifications() }) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.notifications_refresh),
                        tint = MaterialTheme.colorScheme.primary)
                }
                // Oznacz wszystkie
                if (unreadCount > 0) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text(stringResource(R.string.notifications_mark_all))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Filtry (chips) ────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedFilter == 0,
                onClick  = { selectedFilter = 0 },
                label    = { Text(stringResource(R.string.notifications_all, notifications.size)) },
                leadingIcon = {
                    if (selectedFilter == 0)
                        Icon(Icons.Default.Check, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                }
            )
            FilterChip(
                selected = selectedFilter == 1,
                onClick  = { selectedFilter = 1 },
                label    = { Text(stringResource(R.string.notifications_unread, unreadCount)) },
                leadingIcon = {
                    if (selectedFilter == 1)
                        Icon(Icons.Default.Check, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Błąd sieci ────────────────────────────────────────────────────────
        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WifiOff, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = { viewModel.fetchNotifications() }) {
                        Text(stringResource(R.string.notifications_retry))
                    }
                }
            }
        }

        // ── Spinner ───────────────────────────────────────────────────────────
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        // ── Lista / pusty stan ────────────────────────────────────────────────
        if (displayed.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedFilter == 1)
                            Icons.Default.MarkEmailRead
                        else
                            Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedFilter == 1)
                            stringResource(R.string.notifications_no_unread)
                        else
                            stringResource(R.string.notifications_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(displayed, key = { it.id }) { notification ->
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

// ─────────────────────────────────────────────────────────────────────────────
// Karta powiadomienia
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotificationCard(
    notification: NotificationDTO,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        "APPOINTMENT"             -> Icons.Default.AddCircle
        "APPOINTMENT_CANCELLED"   -> Icons.Default.Cancel
        "APPOINTMENT_COMPLETED"   -> Icons.Default.CheckCircle
        "APPOINTMENT_CONFIRMED"   -> Icons.Default.CheckCircle
        "APPOINTMENT_NO_SHOW"     -> Icons.Default.Warning
        "APPOINTMENT_REMINDER_24H",
        "APPOINTMENT_REMINDER_12H" -> Icons.Default.NotificationsActive
        else                      -> Icons.Default.Info
    }

    val iconColor = when (notification.type) {
        "APPOINTMENT"             -> Color(0xFF4CAF50)
        "APPOINTMENT_CANCELLED"   -> MaterialTheme.colorScheme.error
        "APPOINTMENT_COMPLETED"   -> Color(0xFF9E9E9E)
        "APPOINTMENT_CONFIRMED"   -> Color(0xFF4CAF50)
        "APPOINTMENT_NO_SHOW"     -> Color(0xFF9C27B0)
        "APPOINTMENT_REMINDER_24H",
        "APPOINTMENT_REMINDER_12H" -> Color(0xFFFF9800)
        else                      -> MaterialTheme.colorScheme.primary
    }

    val typeLabel = when (notification.type) {
        "APPOINTMENT"             -> stringResource(R.string.notification_type_visit)
        "APPOINTMENT_CANCELLED"   -> stringResource(R.string.notification_type_cancelled)
        "APPOINTMENT_COMPLETED"   -> stringResource(R.string.notification_type_completed)
        "APPOINTMENT_CONFIRMED"   -> stringResource(R.string.notification_type_confirmed)
        "APPOINTMENT_NO_SHOW"     -> stringResource(R.string.notification_type_absence)
        "APPOINTMENT_REMINDER_24H" -> stringResource(R.string.notification_type_reminder_24h)
        "APPOINTMENT_REMINDER_12H" -> stringResource(R.string.notification_type_reminder_12h)
        else                      -> stringResource(R.string.notification_type_system)
    }

    val formattedTime = remember(notification.createdAt) {
        try {
            val zdt = ZonedDateTime.parse(notification.createdAt)
            zdt.format(DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy", Locale.forLanguageTag("pl")))
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
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(if (notification.read) 0.dp else 3.dp),
        border = if (!notification.read)
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
        else
            null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Ikona z kółkiem
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null,
                    tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = typeLabel,
                        fontWeight = if (notification.read) FontWeight.Medium else FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (notification.read)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                    // Dot badge dla nieprzeczytanych
                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
