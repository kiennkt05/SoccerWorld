package com.example.soccerworld.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.ui.home.leaguetable.LeagueTableScreen
import com.example.soccerworld.ui.home.topscorer.TopScorersScreen
import com.example.soccerworld.ui.onboarding.popularLeagues
import com.example.soccerworld.ui.theme.*
import com.example.soccerworld.util.CustomSharedPreferences

@Composable
fun HomeScreen(
    onTeamClick: (String) -> Unit = {},
    onChangeLeague: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = remember { CustomSharedPreferences.invoke(context) }

    var refreshKey by remember { mutableIntStateOf(0) }

    val currentLeagueId by remember(refreshKey) {
        derivedStateOf { sharedPrefs.getLeagueId() ?: "PL" }
    }
    val currentLeagueInfo = remember(currentLeagueId) {
        popularLeagues.find { it.id == currentLeagueId }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Standings", "Top Scorers")

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Compact League Bar ─────────────────────────────────────────
        CompactLeagueBar(
            leagueName = currentLeagueInfo?.name ?: currentLeagueId,
            leagueLogoUrl = currentLeagueInfo?.logoUrl,
            onChangeLeague = onChangeLeague
        )

        // ── Original Tab Row ───────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = AccentEmerald,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = AccentEmerald
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    selectedContentColor = AccentEmerald,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Content ────────────────────────────────────────────────────
        when (selectedTabIndex) {
            0 -> LeagueTableScreen(key = refreshKey, onTeamClick = onTeamClick)
            1 -> TopScorersScreen(key = refreshKey)
        }
    }
}

@Composable
private fun CompactLeagueBar(
    leagueName: String,
    leagueLogoUrl: String?,
    onChangeLeague: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BrandNavy, BrandNavyMid)
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // League logo
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            if (leagueLogoUrl != null) {
                AsyncImage(
                    model = leagueLogoUrl,
                    contentDescription = leagueName,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // League name
        Text(
            text = leagueName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        // Change button
        Surface(
            modifier = Modifier.clickable { onChangeLeague() },
            shape = RoundedCornerShape(8.dp),
            color = AccentEmerald.copy(alpha = 0.18f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Change",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentEmerald
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
