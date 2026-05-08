package com.example.soccerworld.ui.home.leaguetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun LeagueTableScreen(onTeamClick: (String) -> Unit = {}) {
    val context = LocalContext.current

    val viewModel: LeagueTableViewModel = viewModel(factory = ViewModelFactory(
            Injection.provideFootballRepository(
                context
            )
        )
    )

    val state by viewModel.uiState.collectAsState()

    LeagueTableContent(state = state, onTeamClick = onTeamClick)
}

// ==========================================
// 2. HÀM STATELESS (Dùng để vẽ giao diện và Preview)
// ==========================================
@Composable
fun LeagueTableContent(state: LeagueTableUiState, onTeamClick: (String) -> Unit = {}) {
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
            LaunchedEffect(safeList, imageSizePx) {
                val imageLoader = context.imageLoader
                safeList.forEach { item ->
                    val crest = item.team?.crest
                    if (!crest.isNullOrBlank()) {
                        val request = ImageRequest.Builder(context)
                            .data(crest)
                            .size(imageSizePx)
                            .build()
                        imageLoader.enqueue(request)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
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
                        onTeamClick = onTeamClick
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
            .background(SofascoreBlue.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position
        Text(
            text = "#",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = SofascoreBlue,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Team name placeholder
        Text(
            text = "Đội",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = SofascoreBlue,
            modifier = Modifier.weight(1f)
        )

        // Stats columns
        listOf("Trận", "T", "H", "B", "HS", "Đ").forEach { label ->
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = SofascoreBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(if (label == "HS") 28.dp else 24.dp)
            )
        }
    }
}

// ==========================================
// TEAM ROW — clickable, with zone indicator and contrast
// ==========================================
@Composable
fun TeamRow(
    item: Table,
    onTeamClick: (String) -> Unit = {}
) {
    val position = item.position ?: 0
    val teamId = item.team?.id ?: ""
    val ballPainter = painterResource(id = R.drawable.ic_ball)

    // Zone color for position indicator
    val zoneColor = when {
        position <= 4 -> SofascoreBlue                  // Champions League
        position == 5 -> Color(0xFFFF8C00)              // Europa League
        position == 6 -> Color(0xFF2ECC71)              // Conference League
        position >= 18 -> Color(0xFFE74C3C)             // Relegation
        else -> Color.Transparent
    }

    // Alternating row background for contrast
    val rowBg = if (position % 2 == 0) Color(0xFFF8F8F8) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable { if (teamId.isNotEmpty()) onTeamClick(teamId) }
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
                    color = if (zoneColor != Color.Transparent) zoneColor else TextDark,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Team crest — using simple AsyncImage instead of SubcomposeAsyncImage
            AsyncImage(
                model = item.team?.crest,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
                placeholder = ballPainter,
                error = ballPainter,
                fallback = ballPainter
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Team name
            Text(
                text = item.team?.shortName ?: item.team?.name ?: "Unknown",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Stats: GP, W, D, L, GD, Pts
            val gd = item.goalDifference ?: 0
            val stats = listOf(
                "${item.playedGames ?: 0}",
                "${item.won ?: 0}",
                "${item.draw ?: 0}",
                "${item.lost ?: 0}",
                if (gd >= 0) "+$gd" else "$gd",
                "${item.points ?: 0}"
            )

            stats.forEachIndexed { index, value ->
                val isPoints = index == stats.lastIndex
                val isGD = index == stats.lastIndex - 1
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = if (isPoints) FontWeight.ExtraBold else FontWeight.Normal,
                    color = when {
                        isPoints -> SofascoreBlue
                        isGD && gd > 0 -> Color(0xFF2ECC71)
                        isGD && gd < 0 -> Color(0xFFE74C3C)
                        else -> TextSecondary
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(if (isGD) 28.dp else 24.dp)
                )
            }
        }

        // Subtle divider
        HorizontalDivider(
            thickness = 0.5.dp,
            color = DividerColor,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

// ==========================================
// 3. KHU VỰC PREVIEW (Chỉ chạy trong Android Studio)
// ==========================================
@Preview(showBackground = true, name = "Thành công - Có dữ liệu")
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

@Preview(showBackground = true, name = "Đang tải dữ liệu")
@Composable
fun PreviewLeagueTableLoading() {
    MaterialTheme {
        LeagueTableContent(state = LeagueTableUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Lỗi mạng")
@Composable
fun PreviewLeagueTableError() {
    MaterialTheme {
        LeagueTableContent(state = LeagueTableUiState(isLoading = false, error = "Không có kết nối Internet!"))
    }
}