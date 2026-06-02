package com.example.soccerworld.ui.profile

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import com.example.soccerworld.ui.theme.ThemeConfig
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soccerworld.R
import com.example.soccerworld.ui.auth.AuthViewModel
import com.example.soccerworld.ui.theme.AccentNeonOrange
import com.example.soccerworld.ui.theme.BrandGreenLight
import com.example.soccerworld.ui.theme.BrandGreenMedium
import com.example.soccerworld.ui.theme.DividerColor
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.util.CustomSharedPreferences
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onChangeLeague: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPrefs = CustomSharedPreferences.invoke(context)
    val isPreview = LocalInspectionMode.current

    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    // Safely get Firebase user, avoiding crash in Previews
    val firebaseUser = remember(refreshTrigger) {
        if (isPreview) null else {
            try {
                FirebaseAuth.getInstance().currentUser
            } catch (e: Exception) {
                null
            }
        }
    }
    val isLoggedIn = firebaseUser != null

    val displayName = firebaseUser?.displayName?.ifBlank { null }
        ?: firebaseUser?.email?.substringBefore("@")
        ?: stringResource(R.string.profile_guest_user)

    val currentLeague = sharedPrefs.getLeague()
    val currentLeagueName = currentLeague?.name ?: "Unknown League"

    ProfileScreenContent(
        isLoggedIn = isLoggedIn,
        displayName = displayName,
        currentLeagueName = currentLeagueName,
        currentLanguage = sharedPrefs.getLanguage(),
        currentTheme = ThemeConfig.appThemeState.value,
        onChangeLeague = onChangeLeague,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onLogout = {
            authViewModel.logout(context)
            refreshTrigger++
        },
        onLanguageChange = { lang ->
            sharedPrefs.saveLanguage(lang)
            (context as? android.app.Activity)?.recreate()
        },
        onThemeChange = { theme ->
            sharedPrefs.saveTheme(theme)
            ThemeConfig.appThemeState.value = theme
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    isLoggedIn: Boolean,
    displayName: String,
    currentLeagueName: String,
    currentLanguage: String,
    currentTheme: String,
    onChangeLeague: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onLogout: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                // Profile Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Orange Initials Circle
                    val initials = if (isLoggedIn) displayName.take(2).uppercase() else "GU"
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentNeonOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLoggedIn) stringResource(R.string.profile_member_since) else stringResource(R.string.profile_guest_account),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.profile_help), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    OverviewTab(
                        isLoggedIn = isLoggedIn,
                        leagueName = currentLeagueName,
                        onChangeLeague = onChangeLeague,
                        onChangeLanguage = {
                            showSettingsSheet = false
                            showLanguageDialog = true
                        },
                        onChangeTheme = {
                            showSettingsSheet = false
                            showThemeDialog = true
                        },
                        onLogout = {
                            showSettingsSheet = false
                            showLogoutDialog = true
                        },
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToNotificationSettings = onNavigateToNotificationSettings
                    )
                } else {
                    PredictionsTab()
                }
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(R.string.profile_support_help_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.profile_support_help_desc)) },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.profile_close), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
        SettingsBottomSheet(
            onDismiss = { showSettingsSheet = false },
            isLoggedIn = isLoggedIn,
            onNavigateToLogin = {
                showSettingsSheet = false
                onNavigateToLogin()
            },
            onChangeLanguage = {
                showSettingsSheet = false
                showLanguageDialog = true
            },
            onChangeTheme = {
                showSettingsSheet = false
                showThemeDialog = true
            },
            onLogout = {
                showSettingsSheet = false
                showLogoutDialog = true
            }
        )
    }

    // Language Choice Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_select_language), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentLanguage == "en", onClick = {
                            onLanguageChange("en")
                            showLanguageDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("vi")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentLanguage == "vi", onClick = {
                            onLanguageChange("vi")
                            showLanguageDialog = false
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tiếng Việt")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.profile_close), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Theme Choice Dialog
    if (showThemeDialog) {
        val options = listOf(
            "system" to stringResource(R.string.theme_system),
            "light" to stringResource(R.string.theme_light),
            "dark" to stringResource(R.string.theme_dark)
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_select_theme), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChange(value)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == value,
                                onClick = {
                                    onThemeChange(value)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.profile_close), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_signout_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.profile_signout_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.profile_signout_title), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }
}

// ── Overview Tab Composable ──────────────────────────────────────────────────
@Composable
private fun OverviewTab(
    isLoggedIn: Boolean,
    leagueName: String,
    onChangeLeague: () -> Unit,
    onChangeLanguage: () -> Unit,
    onChangeTheme: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = remember { CustomSharedPreferences.invoke(context).getLanguage() }
    val currentTheme = ThemeConfig.appThemeState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Links Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.profile_quick_links),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ProfileMenuRow(
                    icon = Icons.Default.SportsSoccer,
                    title = stringResource(R.string.profile_change_league),
                    subtitle = stringResource(R.string.profile_active_league, leagueName),
                    iconColor = BrandGreenLight,
                    onClick = onChangeLeague
                )
            }
        }

        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.profile_settings),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                // Language Selector Row
                ProfileMenuRow(
                    icon = Icons.Default.Public,
                    title = stringResource(R.string.settings_app_language),
                    subtitle = if (currentLang == "vi") "Tiếng Việt" else "English",
                    iconColor = Color(0xFF673AB7),
                    onClick = onChangeLanguage
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                // Theme Selector Row
                ProfileMenuRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.settings_app_theme),
                    subtitle = when (currentTheme) {
                        "light" -> stringResource(R.string.theme_light)
                        "dark" -> stringResource(R.string.theme_dark)
                        else -> stringResource(R.string.theme_system)
                    },
                    iconColor = Color(0xFFE91E63),
                    onClick = onChangeTheme
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.profile_notification_settings),
                    subtitle = stringResource(R.string.profile_notification_desc),
                    iconColor = Color(0xFFFB8C00),
                    onClick = onNavigateToNotificationSettings
                )
            }
        }

        // Support & Help Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.profile_support),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ProfileMenuRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.profile_faq),
                    subtitle = stringResource(R.string.profile_faq_desc),
                    iconColor = Color(0xFF2196F3)
                ) {}

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuRow(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.profile_send_feedback),
                    subtitle = stringResource(R.string.profile_feedback_desc),
                    iconColor = Color(0xFFFFB300)
                ) {}
            }
        }

        // Login/Logout Option
        if (isLoggedIn) {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.profile_signout_title),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.profile_signin_google),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
        // Footer version details
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SoccerWorld v1.0.0",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Predictions Tab Composable ────────────────────────────────────────────────
@Composable
private fun PredictionsTab() {
    val mockPredictions = listOf(
        Triple("Chelsea vs Arsenal", "Arsenal to Win", "PENDING"),
        Triple("Real Madrid vs Barcelona", "Real Madrid to Win", "SUCCESS"),
        Triple("Man City vs Liverpool", "Draw", "FAILED")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_predictions_title),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(mockPredictions) { (match, prediction, status) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = match, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = stringResource(R.string.profile_predicted_label, prediction), fontSize = 11.sp, color = Color.Gray)
                    }

                    val badgeBg = when (status) {
                        "SUCCESS" -> Color(0xFFE8F5E9)
                        "FAILED" -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF3E0)
                    }
                    val badgeText = when (status) {
                        "SUCCESS" -> Color(0xFF2E7D32)
                        "FAILED" -> Color(0xFFC62828)
                        else -> Color(0xFFEF6C00)
                    }

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = status,
                            color = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Settings Bottom Sheet Composable ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsBottomSheet(
    onDismiss: () -> Unit,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onChangeLanguage: () -> Unit,
    onChangeTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = remember { CustomSharedPreferences.invoke(context).getLanguage() }
    val currentTheme = ThemeConfig.appThemeState.value

    ModalBottomSheet(onDismissRequest = onDismiss) {
        SettingsBottomSheetContent(
            isLoggedIn = isLoggedIn,
            currentLang = currentLang,
            currentTheme = currentTheme,
            onNavigateToLogin = onNavigateToLogin,
            onChangeLanguage = onChangeLanguage,
            onChangeTheme = onChangeTheme,
            onLogout = onLogout
        )
    }
}

@Composable
private fun SettingsBottomSheetContent(
    isLoggedIn: Boolean,
    currentLang: String,
    currentTheme: String,
    onNavigateToLogin: () -> Unit,
    onChangeLanguage: () -> Unit,
    onChangeTheme: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_settings),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Language Selector Row
        ProfileMenuRow(
            icon = Icons.Default.Public,
            title = stringResource(R.string.settings_app_language),
            subtitle = if (currentLang == "vi") "Tiếng Việt" else "English",
            iconColor = Color(0xFF673AB7),
            onClick = onChangeLanguage
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Theme Selector Row
        ProfileMenuRow(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_app_theme),
            subtitle = when (currentTheme) {
                "light" -> stringResource(R.string.theme_light)
                "dark" -> stringResource(R.string.theme_dark)
                else -> stringResource(R.string.theme_system)
            },
            iconColor = Color(0xFFE91E63),
            onClick = onChangeTheme
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)


        if (isLoggedIn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.profile_signout_title), fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLogin() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.profile_signin_google), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Reusable Menu Row Component ──────────────────────────────────────────────
@Composable
private fun ProfileMenuRow(
    icon: ImageVector?,
    title: String,
    subtitle: String?,
    iconColor: Color,
    contentColor: Color? = null,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onClick: () -> Unit
) {
    val isCentered = horizontalArrangement == Arrangement.Center

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = if (isCentered) Modifier else Modifier.weight(1f),
            horizontalAlignment = if (isCentered) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }

        if (subtitle != null && !isCentered) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SoccerWorldTheme {
        ProfileScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Profile Screen - Dark Mode")
@Composable
fun ProfileScreenDarkModePreview() {
    SoccerWorldTheme(darkTheme = true) {
        ProfileScreenContent(
            isLoggedIn = true,
            displayName = "John Doe",
            currentLeagueName = "Premier League",
            currentLanguage = "en",
            currentTheme = "dark",
            onChangeLeague = {},
            onNavigateToLogin = {},
            onNavigateToNotificationSettings = {},
            onLogout = {},
            onLanguageChange = {},
            onThemeChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Sheet - Logged In")
@Composable
private fun SettingsBottomSheetContentLoggedInPreview() {
    SoccerWorldTheme {
        Surface {
            SettingsBottomSheetContent(
                isLoggedIn = true,
                currentLang = "en",
                currentTheme = "system",
                onNavigateToLogin = {},
                onChangeLanguage = {},
                onChangeTheme = {},
                onLogout = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings Sheet - Guest")
@Composable
private fun SettingsBottomSheetContentGuestPreview() {
    SoccerWorldTheme {
        Surface {
            SettingsBottomSheetContent(
                isLoggedIn = false,
                currentLang = "en",
                currentTheme = "system",
                onNavigateToLogin = {},
                onChangeLanguage = {},
                onChangeTheme = {},
                onLogout = {}
            )
        }
    }
}
