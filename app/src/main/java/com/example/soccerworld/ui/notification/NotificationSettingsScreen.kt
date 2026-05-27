package com.example.soccerworld.ui.notification

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soccerworld.ui.theme.AccentEmerald
import com.example.soccerworld.ui.theme.AccentNeonOrange
import com.example.soccerworld.ui.theme.BrandGreenMedium
import com.example.soccerworld.ui.theme.BrandNavy
import com.example.soccerworld.ui.theme.BrandNavyMid
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.util.FcmTopicManager
import com.example.soccerworld.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { CustomSharedPreferences.invoke(context) }

    // Notification permission state
    var notifPermissionGranted by remember {
        mutableStateOf(NotificationHelper.areNotificationsEnabled(context))
    }

    // Toggle states (đọc từ SharedPreferences)
    var matchReminderEnabled by remember { mutableStateOf(prefs.isMatchReminderEnabled()) }
    var liveScoreEnabled     by remember { mutableStateOf(prefs.isLiveScoreEnabled()) }
    var matchResultEnabled   by remember { mutableStateOf(prefs.isMatchResultEnabled()) }

    // Permission launcher (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifPermissionGranted = granted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notification Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // ── Header gradient ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(listOf(BrandNavy, BrandNavyMid))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AccentEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Stay Up to Date",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        "Manage your match alerts",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── System notification warning ───────────────────────────────────
            AnimatedVisibility(
                visible = !notifPermissionGranted,
                enter   = fadeIn(tween(300)) + expandVertically(),
                exit    = fadeOut(tween(300)) + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint   = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Notifications Disabled",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "Enable notifications in system settings to receive alerts.",
                                style  = MaterialTheme.typography.bodySmall,
                                color  = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            Text("Enable", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Notification types ────────────────────────────────────────────
            SectionHeader("ALERT TYPES")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // 1. Match Reminder
                NotificationToggleRow(
                    icon        = Icons.Default.Schedule,
                    iconColor   = BrandGreenMedium,
                    title       = "Match Reminder",
                    description = "Nhắc nhở 15 phút trước khi trận yêu thích bắt đầu",
                    checked     = matchReminderEnabled,
                    enabled     = notifPermissionGranted,
                    onChecked   = { on ->
                        matchReminderEnabled = on
                        prefs.setMatchReminderEnabled(on)
                        if (on) FcmTopicManager.subscribeToTopic(FcmTopicManager.TOPIC_MATCH_REMINDER)
                        else    FcmTopicManager.unsubscribeFromTopic(FcmTopicManager.TOPIC_MATCH_REMINDER)
                    },
                    showDivider = true
                )

                // 2. Live Score
                NotificationToggleRow(
                    icon        = Icons.Default.SportsSoccer,
                    iconColor   = AccentEmerald,
                    title       = "Live Score Update",
                    description = "Thông báo ngay khi có bàn thắng trong trận đang diễn ra",
                    checked     = liveScoreEnabled,
                    enabled     = notifPermissionGranted,
                    onChecked   = { on ->
                        liveScoreEnabled = on
                        prefs.setLiveScoreEnabled(on)
                        if (on) FcmTopicManager.subscribeToTopic(FcmTopicManager.TOPIC_LIVE_SCORE)
                        else    FcmTopicManager.unsubscribeFromTopic(FcmTopicManager.TOPIC_LIVE_SCORE)
                    },
                    showDivider = true
                )

                // 3. Match Result
                NotificationToggleRow(
                    icon        = Icons.Default.Notifications,
                    iconColor   = AccentNeonOrange,
                    title       = "Match Result",
                    description = "Nhận kết quả cuối trận ngay sau khi trận kết thúc",
                    checked     = matchResultEnabled,
                    enabled     = notifPermissionGranted,
                    onChecked   = { on ->
                        matchResultEnabled = on
                        prefs.setMatchResultEnabled(on)
                        if (on) FcmTopicManager.subscribeToTopic(FcmTopicManager.TOPIC_MATCH_RESULT)
                        else    FcmTopicManager.unsubscribeFromTopic(FcmTopicManager.TOPIC_MATCH_RESULT)
                    },
                    showDivider = false
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Info note ─────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text  = "Thông báo hoạt động ở background. " +
                                "Live score được cập nhật mỗi 30 giây khi có trận đang diễn ra. " +
                                "Match Reminder được kiểm tra mỗi 15 phút.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Reusable Composables ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelSmall,
        fontWeight   = FontWeight.ExtraBold,
        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
    )
}

@Composable
private fun NotificationToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onChecked: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint     = if (enabled) iconColor else iconColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.4f
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            Switch(
                checked  = checked && enabled,
                onCheckedChange = { if (enabled) onChecked(it) },
                enabled  = enabled,
                colors   = SwitchDefaults.colors(
                    checkedThumbColor       = Color.White,
                    checkedTrackColor       = iconColor,
                    uncheckedThumbColor     = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor     = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}
