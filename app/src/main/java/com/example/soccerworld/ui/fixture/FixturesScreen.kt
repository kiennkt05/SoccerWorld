package com.example.soccerworld.ui.fixture

import android.util.Log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.model.fixture.*
import com.example.soccerworld.ui.theme.*
import com.example.soccerworld.ui.components.MatchRow
import com.example.soccerworld.ui.components.SectionHeader
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import com.example.soccerworld.work.LivePollingScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.tooling.preview.Preview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun FixturesScreen(key: Int = 0, onMatchClick: (String) -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: FixtureViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(key) {
        if (key > 0) {
            viewModel.getAllFixtureOfLeague(forceRefresh = true)
        }
    }

    FixturesContent(
        state = state,
        onTabSelected = { viewModel.onTabSelected(it) },
        onToggleTournamentExpanded = { viewModel.toggleTournamentExpanded(it) },
        onToggleFavorite = { viewModel.toggleFavorite(it) },
        onLoadMoreMatches = { viewModel.loadMoreMatches() },
        onMatchClick = onMatchClick
    )
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun FixturesContent(
    state: FixtureUiState,
    onTabSelected: (String) -> Unit,
    onToggleTournamentExpanded: (TournamentInfo) -> Unit,
    onToggleFavorite: (Matche) -> Unit,
    onLoadMoreMatches: () -> Unit,
    onMatchClick: (String) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SofascoreBlue)
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }
    } else {
        val groupedForSelected = remember(state.tournamentGroups, state.selectedTab) {
            state.tournamentGroups[state.selectedTab].orEmpty()
        }
        val listState = rememberLazyListState()
        val selectedTabIndex by remember(state.availableTabs, state.selectedTab) {
            derivedStateOf { state.availableTabs.indexOf(state.selectedTab).coerceAtLeast(0) }
        }
        val totalItems by remember(groupedForSelected, state.expandedTournaments) {
            derivedStateOf {
                groupedForSelected.entries.sumOf { (tournament, matches) ->
                    1 + if (state.expandedTournaments.contains(tournament)) matches.size else 0
                }
            }
        }
        val currentTotalItems by rememberUpdatedState(totalItems)
        val currentIsLoadingMore by rememberUpdatedState(state.isLoadingMore)
        val currentHasMorePages by rememberUpdatedState(state.hasMorePages)

        LaunchedEffect(state.selectedTab, totalItems) {
            if (totalItems == 0) return@LaunchedEffect
            if (state.selectedTab == "SCHEDULED") {
                listState.scrollToItem(totalItems - 1)
            } else {
                listState.scrollToItem(0)
            }
        }

        // Detect scroll tới cuối để load more (with debounce)
        LaunchedEffect(listState) {
            snapshotFlow { 
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) -1 else visibleItems.last().index
            }
                .distinctUntilChanged()
                .debounce(500)
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex >= 0 && currentTotalItems > 0) {
                        val threshold = (currentTotalItems * 0.8).toInt()
                        if (lastVisibleIndex >= threshold && !currentIsLoadingMore && currentHasMorePages) {
                            onLoadMoreMatches()
                        }
                    }
                }
        }

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Sofascore-style tab bar — Clean white/surface background
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                divider = {}
            ) {
                state.availableTabs.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = formatTabTitle(tab),
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                state = listState
            ) {
                groupedForSelected.forEach { (tournament, matches) ->
                    val isExpanded = state.expandedTournaments.contains(tournament)
                    item(key = "header-${tournament.id}", contentType = "header") {
                        SectionHeader(
                            title = tournament.name ?: "Unknown League",
                            flagUrl = tournament.emblemUrl,
                            isExpanded = isExpanded,
                            onToggle = { onToggleTournamentExpanded(tournament) }
                        )
                    }
                    if (isExpanded) {
                        items(
                            items = matches,
                            key = { match -> match.id ?: "${tournament.id}-${match.utcDate ?: match.hashCode()}" },
                            contentType = { "match_row" }
                        ) { match ->
                            FixtureCard(
                                match = match,
                                isFavorite = state.favoriteIds.contains(match.id ?: ""),
                                onToggleFavorite = { onToggleFavorite(match) },
                                onClick = { onMatchClick(match.id ?: "") }
                            )
                        }
                    }
                }
                
                // Loading indicator khi load more
                if (state.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SofascoreBlue,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FixtureCard(
    match: Matche,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit = {},
    showDateForFinished: Boolean = false
) {
    val activeFilter = remember(match.status) {
        when (match.status) {
            "IN_PLAY", "PAUSED" -> "Live"
            "FINISHED" -> "Finished"
            else -> "Scheduled"
        }
    }
    
    MatchRow(
        match = match,
        activeFilter = activeFilter,
        isFavorite = isFavorite,
        onClick = { onClick() },
        onToggleFavorite = onToggleFavorite
    )
}

private fun formatTabTitle(tab: String): String {
    return when (tab) {
        "IN_PLAY" -> "Live"
        "SCHEDULED" -> "Scheduled"
        "FINISHED" -> "Finished"
        else -> tab
    }
}

@Preview(showBackground = true)
@Composable
fun FixturesScreenPreview() {
    val sampleTournament = TournamentInfo(
        id = "PL",
        name = "Premier League",
        emblemUrl = "https://crests.football-data.org/PL.png"
    )
    val sampleMatch = Matche(
        id = "1",
        utcDate = "2023-10-27T18:30:00Z",
        status = "FINISHED",
        homeTeam = HomeTeam(id = "1", name = "Arsenal FC", crest = "https://crests.football-data.org/57.png"),
        awayTeam = AwayTeam(id = "2", name = "Chelsea FC", crest = "https://crests.football-data.org/61.png"),
        score = Score(fullTime = FullTime(home = 2, away = 1))
    )
    
    val state = FixtureUiState(
        isLoading = false,
        availableTabs = listOf("IN_PLAY", "SCHEDULED", "FINISHED"),
        selectedTab = "FINISHED",
        tournamentGroups = mapOf(
            "FINISHED" to mapOf(sampleTournament to listOf(sampleMatch))
        ),
        expandedTournaments = setOf(sampleTournament)
    )
    
    SoccerWorldTheme {
        FixturesContent(
            state = state,
            onTabSelected = {},
            onToggleTournamentExpanded = {},
            onToggleFavorite = {},
            onLoadMoreMatches = {},
            onMatchClick = {}
        )
    }
}
