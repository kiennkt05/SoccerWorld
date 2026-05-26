package com.example.soccerworld.ui.home.leaguetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.soccerworld.R
import com.example.soccerworld.model.leaguetable.Table
import com.example.soccerworld.model.leaguetable.Team
import com.example.soccerworld.ui.theme.*
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

// ==========================================
// 1. HÀM STATEFUL (Dùng để chạy thật trên máy)
// ==========================================
@Composable
fun LeagueTableScreen(key: Int = 0, onTeamClick: (String) -> Unit = {}) {
    val context = LocalContext.current

    val viewModel: LeagueTableViewModel = viewModel(factory = ViewModelFactory(
            Injection.provideFootballRepository(
                context
            )
        )
    )

    // Re-fetch when key changes (league was switched)
    LaunchedEffect(key) {
        if (key > 0) viewModel.refresh()
    }

    val state by viewModel.uiState.collectAsState()

    LeagueTableContent(state = state, onTeamClick = onTeamClick)
}

// ==========================================
// 2. HÀM STATELESS (Dùng để vẽ giao diện và Preview)
// ==========================================
@Composable
fun LeagueTableContent(state: LeagueTableUiState, onTeamClick: (String) -> Unit = {}) {
    var highlightedTeamId by remember { mutableStateOf<String?>(null) }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SofascoreBlue)
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error, color = Color.Red)
            }
        }
        else -> {
            val safeList = remember(state.tableList) {
                (state.tableList ?: emptyList()).filterNotNull()
            }
            val context = LocalContext.current
            val imageSizePx = with(LocalDensity.current) { 28.dp.roundToPx() }

            // Prefetch images
            LaunchedEffect(safeList) {
                val imageLoader = context.imageLoader
                androidx.compose.runtime.snapshotFlow { safeList }
                    .collect { items ->
                        items.forEach { item ->
                            val crest = item.team?.crest
                            if (!crest.isNullOrBlank()) {
                                val request = ImageRequest.Builder(context).data(crest).build()
                                imageLoader.enqueue(request)
                            }
                        }
                    }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 88.dp)
            ) {
                // Header row
                item(key = "header", contentType = "header") {
                    TableHeaderRow()
                }

                items(
                    items = safeList,
                    key = { item -> item.team?.id ?: item.position ?: item.hashCode() },
                    contentType = { "team_row" }
                ) { item ->
                    TeamRow(
                        item = item,
                        highlightedTeamId = highlightedTeamId,
                        onTeamClick = onTeamClick,
                        onTeamLongClick = { teamId ->
                            highlightedTeamId = if (highlightedTeamId == teamId) null else teamId
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// HEADER ROW
// ==========================================
@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = "#",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = AccentEmerald,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Team name placeholder
        Text(
            text = "Team",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        // Stats columns
        listOf("P", "W", "D", "L", "GD", "PTS").forEach { label ->
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(if (label == "GD") 28.dp else 24.dp)
            )
        }
    }
}

// ==========================================
// TEAM ROW — clickable, with zone indicator and contrast
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamRow(
    item: Table,
    highlightedTeamId: String?,
    onTeamClick: (String) -> Unit = {},
    onTeamLongClick: (String) -> Unit = {}
) {
    val position = item.position ?: 0
    val teamId = item.team?.id ?: ""
    val ballPainter = painterResource(id = R.drawable.ic_ball)
    val isHighlighted = teamId.isNotEmpty() && teamId == highlightedTeamId

    // Zone color for position indicator
    val soccerColors = LocalSoccerColors.current
    val zoneColor = remember(position) {
        when {
            position <= 4 -> soccerColors.zoneChampionsLeague
            position == 5 -> soccerColors.zoneEuropaLeague
            position == 6 -> soccerColors.zoneConferenceLeague
            position >= 18 -> soccerColors.zoneRelegation
            else -> Color.Transparent
        }
    }

    // Alternating row background for contrast, or premium highlighted light blue/indigo
    val normalBg = if (position % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val rowBg = if (isHighlighted) {
        if (isSystemInDarkTheme()) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFE3F2FD)
    } else {
        normalBg
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .combinedClickable(
                onClick = { if (teamId.isNotEmpty()) onTeamClick(teamId) },
                onLongClick = { if (teamId.isNotEmpty()) onTeamLongClick(teamId) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number with zone color indicator
            Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                // Zone indicator bar on the left
                if (zoneColor != Color.Transparent) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(3.dp, 18.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(zoneColor)
                    )
                }
                Text(
                    text = "$position",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (zoneColor != Color.Transparent) zoneColor else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Team crest
            com.example.soccerworld.ui.components.TeamCrestImage(
                model = item.team?.crest,
                size = 24.dp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Team name
            Text(
                text = item.team?.shortName ?: item.team?.name ?: "Unknown",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Stats: GP, W, D, L, GD, Pts (with robust math calculation)
            val wins = item.won ?: 0
            val played = item.playedGames ?: 0
            val pts = item.points ?: 0
            val draws = maxOf(0, pts - wins * 3)
            val losses = maxOf(0, played - wins - draws)
            val gd = item.goalDifference ?: 0
            
            val stats = listOf(
                "$played",
                "$wins",
                "$draws",
                "$losses",
                if (gd >= 0) "+$gd" else "$gd",
                "$pts"
            )

            stats.forEachIndexed { index, value ->
                val isPoints = index == stats.lastIndex
                val isGD = index == stats.lastIndex - 1
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = if (isPoints) FontWeight.ExtraBold else FontWeight.Normal,
                    color = when {
                        isPoints -> AccentEmerald
                        isGD && gd > 0 -> Color(0xFF00D9A3)
                        isGD && gd < 0 -> LiveRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(if (isGD) 28.dp else 24.dp)
                )
            }
        }

        // Subtle divider
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

// ==========================================
// 3. KHU VỰC PREVIEW (Chỉ chạy trong Android Studio)
// ==========================================
@Preview(showBackground = true, name = "Success - With Data")
@Composable
fun PreviewLeagueTableSuccess() {
    val fakeData = listOf(
        Table(
            position = 1, team = Team(
                id = "57",
                name = "Arsenal FC",
                crest = "https://crests.football-data.org/57.png"
            ), playedGames = 31, points = 70, goalDifference = 39
        ),
        Table(position = 2, team = Team(id = "65", name = "Manchester City", crest = "https://crests.football-data.org/65.png"), playedGames = 30, points = 61, goalDifference = 32),
        Table(
            position = 3,
            team = Team(id = "66", name = "Manchester United", crest = "https://crests.football-data.org/66.png"),
            playedGames = 31,
            points = 55,
            goalDifference = 13
        )
    )

    val fakeState = LeagueTableUiState(
        isLoading = false,
        tableList = fakeData,
        error = null
    )

    MaterialTheme {
        LeagueTableContent(state = fakeState)
    }
}

@Preview(showBackground = true, name = "Loading Data")
@Composable
fun PreviewLeagueTableLoading() {
    MaterialTheme {
        LeagueTableContent(state = LeagueTableUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Network Error")
@Composable
fun PreviewLeagueTableError() {
    MaterialTheme {
        LeagueTableContent(state = LeagueTableUiState(isLoading = false, error = "No Internet connection!"))
    }
}