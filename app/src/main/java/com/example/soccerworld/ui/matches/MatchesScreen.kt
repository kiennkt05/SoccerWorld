package com.example.soccerworld.ui.matches

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.soccerworld.ui.home.leaguetable.LeagueTableScreen
import com.example.soccerworld.ui.home.topscorer.TopScorersScreen
import com.example.soccerworld.ui.fixture.FixturesScreen
import com.example.soccerworld.ui.onboarding.popularLeagues
import com.example.soccerworld.util.CustomSharedPreferences

@Composable
fun MatchesScreen(
    onTeamClick: (String) -> Unit = {},
    onChangeLeague: () -> Unit = {},
    onMatchClick: (String) -> Unit = {}
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
    val tabs = listOf("Standings", "Matches", "Top Scorers")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Flat League Selector Bar (M3 Clean Style) ──
        LeagueSelectorBar(
            leagueName = currentLeagueInfo?.name ?: currentLeagueId,
            leagueLogoUrl = currentLeagueInfo?.logoUrl,
            onChangeLeague = onChangeLeague
        )

        // ── Sub Tab Row ──
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
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
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Tab Contents ──
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> LeagueTableScreen(key = refreshKey, onTeamClick = onTeamClick)
                1 -> FixturesScreen(onMatchClick = onMatchClick)
                2 -> TopScorersScreen(key = refreshKey)
            }
        }
    }
}

@Composable
fun LeagueSelectorBar(
    leagueName: String,
    leagueLogoUrl: String?,
    onChangeLeague: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // League crest container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (leagueLogoUrl != null) {
                AsyncImage(
                    model = leagueLogoUrl,
                    contentDescription = leagueName,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // League Name Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leagueName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "England · 2025/26",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Change button
        Surface(
            modifier = Modifier.clickable { onChangeLeague() },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Change",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
