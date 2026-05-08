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
            Text(text = state.error ?: "Lỗi tải lịch thi đấu", color = MaterialTheme.colorScheme.error)
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

        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
            // Sofascore-style tab bar — xanh dương đậm
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp,
                containerColor = SofascoreBlue,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color.White
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
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)),
                state = listState
            ) {
                groupedForSelected.forEach { (tournament, matches) ->
                    val isExpanded = state.expandedTournaments.contains(tournament)
                    item(key = "header-${tournament.id}", contentType = "header") {
                        LeagueSectionHeader(
                            tournament = tournament,
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
                            MatchRow(
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

/**
 * League section header — Sofascore style
 * [Emblem] [League Name]        [▼]
 *          [Country]
 */
@Composable
private fun LeagueSectionHeader(
    tournament: TournamentInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val ballPainter = painterResource(id = R.drawable.ic_ball)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // League emblem
            AsyncImage(
                model = tournament.emblemUrl,
                contentDescription = tournament.name,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
                placeholder = ballPainter,
                error = ballPainter,
                fallback = ballPainter
            )

            Spacer(modifier = Modifier.width(10.dp))

            // League name + country
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tournament.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tournament.areaName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Country flag
                        if (tournament.areaFlag != null) {
                            AsyncImage(
                                model = tournament.areaFlag,
                                contentDescription = tournament.areaName,
                                modifier = Modifier.size(14.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = tournament.areaName,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Expand/collapse arrow
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Bottom divider
        HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
    }
}

/**
 * Backward-compatible alias for MatchRow — used by FavoritesScreen and TeamTabs.
 */
@Composable
fun FixtureCard(
    match: Matche,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit = {}
) {
    MatchRow(match = match, isFavorite = isFavorite, onToggleFavorite = onToggleFavorite, onClick = onClick)
}

/**
 * Match row — Sofascore style (flat, no Card)
 * [Time]  [🏠 Home Team]     [score]  [☆]
 * [FT  ]  [🏟️ Away Team]     [score]
 */
@Composable
fun MatchRow(
    match: Matche,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val formattedTime = remember(match.utcDate) { formatTime(match.utcDate) }
    val homeScore = match.score?.fullTime?.home
    val awayScore = match.score?.fullTime?.away
    val isLive = match.status == "IN_PLAY" || match.status == "PAUSED"
    val isFinishedOrLive = match.status == "FINISHED" || isLive
    val homeWins = isFinishedOrLive && (homeScore ?: 0) > (awayScore ?: 0)
    val awayWins = isFinishedOrLive && (awayScore ?: 0) > (homeScore ?: 0)
    val ballPainter = painterResource(id = R.drawable.ic_ball)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time / Status column — fixed width
            Column(
                modifier = Modifier.width(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLive) {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = LiveRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (match.status == "PAUSED") "HT" else "Live",
                        fontSize = 10.sp,
                        color = LiveRed,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (match.status == "FINISHED") "FT" else "",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(36.dp)
                    .background(DividerColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Teams column — 2 rows (home on top, away on bottom)
            Column(modifier = Modifier.weight(1f)) {
                // Home team row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = match.homeTeam?.crest,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = ballPainter,
                        error = ballPainter,
                        fallback = ballPainter
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "TBD",
                        fontSize = 13.sp,
                        fontWeight = if (homeWins) FontWeight.Bold else FontWeight.Normal,
                        color = if (awayWins) LoserText else TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Home score
                    if (isFinishedOrLive) {
                        Text(
                            text = "${homeScore ?: 0}",
                            fontSize = 13.sp,
                            fontWeight = if (homeWins) FontWeight.Bold else FontWeight.Normal,
                            color = if (awayWins) LoserText else TextDark,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Away team row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = match.awayTeam?.crest,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = ballPainter,
                        error = ballPainter,
                        fallback = ballPainter
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "TBD",
                        fontSize = 13.sp,
                        fontWeight = if (awayWins) FontWeight.Bold else FontWeight.Normal,
                        color = if (homeWins) LoserText else TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Away score
                    if (isFinishedOrLive) {
                        Text(
                            text = "${awayScore ?: 0}",
                            fontSize = 13.sp,
                            fontWeight = if (awayWins) FontWeight.Bold else FontWeight.Normal,
                            color = if (homeWins) LoserText else TextDark,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite star icon — simple clickable, no IconButton wrapper
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Toggle favorite",
                tint = if (isFavorite) Color(0xFFFFC107) else Color(0xFFD0D0D0),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onToggleFavorite() }
            )
        }

        // Bottom divider
        HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
    }
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
