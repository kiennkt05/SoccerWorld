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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
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
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.ui.theme.*
import com.example.soccerworld.ui.components.MatchDisplayModel
import com.example.soccerworld.ui.components.MatchScoreRow
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

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun FixturesScreen(onMatchClick: (String) -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: FixtureViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.hasLiveMatches) {
        if (state.hasLiveMatches) {
            Log.d("FixturesScreen", "LivePolling started")
            LivePollingScheduler.start(context)
        } else {
            Log.d("FixturesScreen", "LivePolling stopped")
            LivePollingScheduler.stop(context)
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SofascoreBlue)
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.error ?: "Error loading fixtures", color = MaterialTheme.colorScheme.error)
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
                            viewModel.loadMoreMatches()
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
                        onClick = { viewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                text = formatTabTitle(tab),
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                state = listState
            ) {
                groupedForSelected.forEach { (tournament, matches) ->
                    val isExpanded = state.expandedTournaments.contains(tournament)
                    item(key = "header-${tournament.id}", contentType = "header") {
                        SectionHeader(
                            title = tournament.name ?: "Unknown League",
                            flagUrl = tournament.emblemUrl,
                            isExpanded = isExpanded,
                            onToggle = { viewModel.toggleTournamentExpanded(tournament) }
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
                                onToggleFavorite = { viewModel.toggleFavorite(match) },
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
    onClick: () -> Unit = {}
) {
    val displayModel = remember(match) { mapToDisplayModel(match) }
    
    MatchScoreRow(
        match = displayModel,
        onClick = onClick,
        actionIcon = {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Toggle favorite",
                    tint = if (isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

private fun mapToDisplayModel(match: Matche): MatchDisplayModel {
    val isLive = match.status == "IN_PLAY" || match.status == "PAUSED"
    return MatchDisplayModel(
        id = match.id ?: "",
        timeText = formatTime(match.utcDate),
        statusText = when (match.status) {
            "PAUSED" -> "HT"
            "IN_PLAY" -> "Live"
            "FINISHED" -> "FT"
            else -> ""
        },
        isLive = isLive,
        homeTeamName = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "TBD",
        homeTeamCrest = match.homeTeam?.crest,
        awayTeamName = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "TBD",
        awayTeamCrest = match.awayTeam?.crest,
        homeScore = match.score?.fullTime?.home,
        awayScore = match.score?.fullTime?.away,
        isFinishedOrLive = match.status == "FINISHED" || isLive
    )
}

private fun formatTime(utcString: String?): String {
    if (utcString.isNullOrEmpty()) return ""
    return try {
        val date = INPUT_FORMATTER.get()?.parse(utcString)
        date?.let { OUTPUT_FORMATTER.get()?.format(it) } ?: utcString
    } catch (_: Exception) {
        utcString
    }
}

private val INPUT_FORMATTER = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}

private val OUTPUT_FORMATTER = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("HH:mm", Locale.US)
    }
}

private fun formatTabTitle(tab: String): String {
    return when (tab) {
        "IN_PLAY" -> "Live"
        "SCHEDULED" -> "Scheduled"
        "FINISHED" -> "Finished"
        else -> tab
    }
}
