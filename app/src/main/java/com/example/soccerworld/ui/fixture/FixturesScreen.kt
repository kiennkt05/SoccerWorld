package com.example.soccerworld.ui.fixture

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import com.example.soccerworld.work.LivePollingScheduler
import java.text.SimpleDateFormat
import java.util.*

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
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.error ?: "Lỗi tải lịch thi đấu", color = Color.Red)
        }
    } else {
        val groupedForSelected = state.tournamentGroups[state.selectedTab].orEmpty()
        val listState = rememberLazyListState()
        val totalItems = groupedForSelected.size + groupedForSelected.values.sumOf { it.size }

        LaunchedEffect(state.selectedTab, totalItems) {
            if (totalItems == 0) return@LaunchedEffect
            if (state.selectedTab == "SCHEDULED") {
                listState.scrollToItem(totalItems - 1)
            } else {
                listState.scrollToItem(0)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = state.availableTabs.indexOf(state.selectedTab).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp
            ) {
                state.availableTabs.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(formatTabTitle(tab)) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                groupedForSelected.forEach { (tournament, matches) ->
                    val isExpanded = state.expandedTournaments.contains(tournament)
                    item(key = "header-${tournament.id}") {
                        LeagueSectionHeader(
                            title = tournament.name,
                            isExpanded = isExpanded,
                            onToggle = { viewModel.toggleTournamentExpanded(tournament) }
                        )
                    }
                    item(key = "content-${tournament.id}") {
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                matches.forEach { match ->
                                    FixtureCard(
                                        match = match,
                                        isFavorite = state.favoriteIds.contains(match.id ?: ""),
                                        onToggleFavorite = { viewModel.toggleFavorite(match) },
                                        onClick = { onMatchClick(match.id ?: "") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueSectionHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FixtureCard(
    match: Matche,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Live Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (match.status == "IN_PLAY" || match.status == "PAUSED") {
                        Surface(
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.Red,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = formatTime(match.utcDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (match.status == "FINISHED") "FT" else if (match.status == "IN_PLAY" || match.status == "PAUSED") match.status ?: "" else "Matchday ${match.matchday ?: "--"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teams & Scores (Horizontal Layout like Sofascore)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.homeTeam?.crest,
                        contentDescription = match.homeTeam?.name,
                        modifier = Modifier.size(24.dp),
                        placeholder = painterResource(id = R.drawable.ic_ball),
                        error = painterResource(id = R.drawable.ic_ball),
                        fallback = painterResource(id = R.drawable.ic_ball)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if ((match.score?.fullTime?.home ?: 0) > (match.score?.fullTime?.away ?: 0)) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores
                val isFinishedOrLive = match.status == "FINISHED" || match.status == "IN_PLAY" || match.status == "PAUSED"
                Text(
                    text = if (isFinishedOrLive) "${match.score?.fullTime?.home ?: 0} - ${match.score?.fullTime?.away ?: 0}" else "- : -",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (match.status == "IN_PLAY" || match.status == "PAUSED") Color.Red else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Away Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if ((match.score?.fullTime?.away ?: 0) > (match.score?.fullTime?.home ?: 0)) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = match.awayTeam?.crest,
                        contentDescription = match.awayTeam?.name,
                        modifier = Modifier.size(24.dp),
                        placeholder = painterResource(id = R.drawable.ic_ball),
                        error = painterResource(id = R.drawable.ic_ball),
                        fallback = painterResource(id = R.drawable.ic_ball)
                    )
                }
            }
        }
    }
}

private fun formatDate(utcString: String?): String {
    if (utcString.isNullOrEmpty()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(utcString)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: utcString
    } catch (e: Exception) {
        utcString
    }
}

private fun formatTime(utcString: String?): String {
    if (utcString.isNullOrEmpty()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(utcString)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        date?.let { formatter.format(it) } ?: utcString
    } catch (e: Exception) {
        utcString
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
