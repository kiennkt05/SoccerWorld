package com.example.soccerworld.ui.fixture

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
fun FixturesScreen(onMatchClick: (Int) -> Unit = {}) {
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
        // Group matches by "matchday" or just display them
        val matches = state.fixtureList.reversed() // Usually recent matches are at the bottom, reverse to show latest/upcoming
        Column(modifier = Modifier.fillMaxSize()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.availableDates) { date ->
                    val selected = date == state.selectedDate
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onDateSelected(date) },
                        label = { Text(date) }
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(matches) { match ->
                    FixtureCard(
                        match = match,
                        isFavorite = state.favoriteIds.contains(match.id ?: -1),
                        onToggleFavorite = { viewModel.toggleFavorite(match) },
                        onClick = { onMatchClick(match.id ?: 0) }
                    )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Matchday ${match.matchday ?: "--"} • ${formatDate(match.utcDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                // Home Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.homeTeam?.crest,
                        contentDescription = match.homeTeam?.name,
                        modifier = Modifier.size(48.dp),
                        placeholder = painterResource(id = R.drawable.ic_ball),
                        error = painterResource(id = R.drawable.ic_ball),
                        fallback = painterResource(id = R.drawable.ic_ball)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score / Status
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (match.status == "FINISHED" || match.status == "IN_PLAY" || match.status == "PAUSED") {
                        val homeScore = match.score?.fullTime?.home ?: 0
                        val awayScore = match.score?.fullTime?.away ?: 0
                        Text(
                            text = "$homeScore - $awayScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (match.status == "FINISHED") "FT" else match.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = formatTime(match.utcDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upcoming",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Away Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.awayTeam?.crest,
                        contentDescription = match.awayTeam?.name,
                        modifier = Modifier.size(48.dp),
                        placeholder = painterResource(id = R.drawable.ic_ball),
                        error = painterResource(id = R.drawable.ic_ball),
                        fallback = painterResource(id = R.drawable.ic_ball)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
