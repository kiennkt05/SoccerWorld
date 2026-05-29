package com.example.soccerworld.ui.profile

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.soccerworld.ui.favorites.FavoritesViewModel
import com.example.soccerworld.ui.onboarding.popularLeagues
import com.example.soccerworld.ui.theme.AccentNeonOrange
import com.example.soccerworld.ui.theme.BrandGreenDark
import com.example.soccerworld.ui.theme.BrandGreenMedium
import com.example.soccerworld.ui.theme.DividerColor
import com.example.soccerworld.ui.theme.LightBackground
import com.example.soccerworld.ui.theme.SofascoreBlue
import com.example.soccerworld.ui.theme.TextSecondary
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
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

    var refreshTrigger by remember { mutableStateOf(0) }
    
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
    val email = firebaseUser?.email ?: stringResource(R.string.profile_signin_sync)

    val currentLeague = sharedPrefs.getLeague()
    val leagueId = currentLeague?.stageId ?: "PL"
    val currentLeagueName = currentLeague?.name ?: "Unknown League"
    val currentLeagueLogo = popularLeagues.find { it.id == leagueId }?.logoUrl
        ?: "https://crests.football-data.org/PL.png"

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        refreshTrigger++
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandGreenMedium)
                    .statusBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                // Top header icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.profile_help), tint = Color.White)
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.profile_settings), tint = Color.White)
                    }
                }

                // Profile Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
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
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLoggedIn) stringResource(R.string.profile_member_since) else stringResource(R.string.profile_guest_account),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightBackground)
        ) {
            // Profile Tabs (Overview / Predictions)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = BrandGreenMedium,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BrandGreenMedium
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.profile_tab_overview), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.profile_tab_predictions), fontWeight = FontWeight.Bold) }
                )
            }

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
                    Text(stringResource(R.string.profile_close), color = BrandGreenMedium)
                }
            }
        )
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
        SettingsBottomSheet(
            onDismiss = { showSettingsSheet = false },
            isLoggedIn = isLoggedIn,
            onChangeLeague = {
                showSettingsSheet = false
                onChangeLeague()
            },
            onNavigateToLogin = {
                showSettingsSheet = false
                onNavigateToLogin()
            },
            onNavigateToNotificationSettings = {
                showSettingsSheet = false
                onNavigateToNotificationSettings()
            },
            onChangeLanguage = {
                showSettingsSheet = false
                showLanguageDialog = true
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
                    val currentLang = sharedPrefs.getLanguage()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sharedPrefs.saveLanguage("en")
                                showLanguageDialog = false
                                (context as? android.app.Activity)?.recreate()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentLang == "en", onClick = {
                            sharedPrefs.saveLanguage("en")
                            showLanguageDialog = false
                            (context as? android.app.Activity)?.recreate()
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sharedPrefs.saveLanguage("vi")
                                showLanguageDialog = false
                                (context as? android.app.Activity)?.recreate()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentLang == "vi", onClick = {
                            sharedPrefs.saveLanguage("vi")
                            showLanguageDialog = false
                            (context as? android.app.Activity)?.recreate()
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tiếng Việt")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.profile_close), color = BrandGreenMedium)
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
                        authViewModel.logout(context)
                        refreshTrigger++
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
    onNavigateToLogin: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit
) {
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.profile_quick_links),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreenMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ProfileMenuRow(
                    icon = Icons.Default.SportsSoccer,
                    title = stringResource(R.string.profile_change_league),
                    subtitle = stringResource(R.string.profile_active_league, leagueName),
                    iconColor = BrandGreenMedium,
                    onClick = onChangeLeague
                )

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.profile_notification_settings),
                    subtitle = stringResource(R.string.profile_notification_desc),
                    iconColor = AccentNeonOrange,
                    onClick = onNavigateToNotificationSettings
                )
            }
        }

        // Support & Help Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.profile_support),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreenMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                ProfileMenuRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.profile_faq),
                    subtitle = stringResource(R.string.profile_faq_desc),
                    iconColor = Color(0xFF2196F3)
                ) {}

                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileMenuRow(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.profile_send_feedback),
                    subtitle = stringResource(R.string.profile_feedback_desc),
                    iconColor = Color(0xFFFFB300)
                ) {}
            }
        }

        // Fantasy League Banner Promo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(BrandGreenDark, AccentNeonOrange)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.65f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SOCCERWORLD FANTASY",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Build your squad, compete with friends, and win prizes!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandGreenMedium),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterEnd),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(stringResource(R.string.profile_play_now), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
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
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(mockPredictions) { (match, prediction, status) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, DividerColor),
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
                            .background(BrandGreenMedium.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = BrandGreenMedium, modifier = Modifier.size(18.dp))
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
    onChangeLeague: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = remember { CustomSharedPreferences.invoke(context).getLanguage() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
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

            // Settings items
            ProfileMenuRow(
                icon = Icons.Default.SportsSoccer,
                title = stringResource(R.string.profile_change_league),
                subtitle = stringResource(R.string.profile_change_league_desc),
                iconColor = BrandGreenMedium,
                onClick = onChangeLeague
            )

            HorizontalDivider(color = DividerColor)

            ProfileMenuRow(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.profile_notification_settings),
                subtitle = stringResource(R.string.profile_notification_desc),
                iconColor = AccentNeonOrange,
                onClick = {
                    onDismiss()
                    onNavigateToNotificationSettings()
                }
            )

            HorizontalDivider(color = DividerColor)

            // Language Selector Row
            ProfileMenuRow(
                icon = Icons.Default.Public,
                title = stringResource(R.string.settings_app_language),
                subtitle = if (currentLang == "vi") "Tiếng Việt" else "English",
                iconColor = Color(0xFF673AB7),
                onClick = onChangeLanguage
            )

            HorizontalDivider(color = DividerColor)

            if (isLoggedIn) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandGreenMedium.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = BrandGreenMedium, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.profile_signin_google), fontWeight = FontWeight.Bold, color = BrandGreenMedium, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Reusable Menu Row Component ──────────────────────────────────────────────
@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SoccerWorldTheme {
        ProfileScreen()
    }
}
