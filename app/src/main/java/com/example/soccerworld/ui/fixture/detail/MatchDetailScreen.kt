package com.example.soccerworld.ui.fixture.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.soccerworld.model.h2h.Matche
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import com.example.soccerworld.model.matchdetail.MatchEnrichmentDetail
import com.example.soccerworld.model.matchdetail.MatchEvent
import com.example.soccerworld.model.matchdetail.MatchLineupTeam
import com.example.soccerworld.model.matchdetail.MatchStatItem
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(fixtureId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val factory = ViewModelFactory(Injection.provideFootballRepository(context))
    val matchDetailViewModel: MatchDetailViewModel = viewModel(factory = factory)

    LaunchedEffect(fixtureId) {
        matchDetailViewModel.loadMatchDetail(fixtureId)
    }
    val state by matchDetailViewModel.uiState.collectAsState()

    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi Tiết Trận Đấu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary,
                    titleContentColor = onPrimary,
                    navigationIconContentColor = onPrimary
                )
            )
        }
    ) { paddingValues ->
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Tóm tắt", "Thống kê", "Đội hình", "H2H")

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ── Match Header ─────────────────────────────────────────
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                        .background(Brush.verticalGradient(listOf(primaryContainer, primary))),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = onPrimary) }
            } else {
                MatchHeader(core = state.data?.core, enrichment = state.data?.enrichment)
            }

            // ── Tab Row ──────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // ── Tab content ──────────────────────────────────────────
            if (state.error != null && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.error ?: "Lỗi tải dữ liệu", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else if (!state.isLoading) {
                val aggregate = state.data
                when (selectedTabIndex) {
                    0 -> SummaryTab(events = aggregate?.enrichment?.events ?: emptyList())
                    1 -> StatsTab(stats = aggregate?.enrichment?.stats ?: emptyList())
                    2 -> LineupsTab(lineups = aggregate?.enrichment?.lineups ?: emptyList())
                    3 -> H2HTab(h2hList = aggregate?.h2h ?: emptyList())
                }
            }
        }
    }
}

// ── Match Header ────────────────────────────────────────────────────────────

@Composable
fun MatchHeader(core: StatisticsResponse?, enrichment: MatchEnrichmentDetail?) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val status = enrichment?.status ?: core?.status ?: "UNKNOWN"
    val homeScore = core?.score?.fullTime?.home
    val awayScore = core?.score?.fullTime?.away
    val isLive = status == "IN_PLAY" || status == "PAUSED"
    val isFinished = status == "FINISHED"
    val hasScore = isLive || isFinished

    val venue = enrichment?.venue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(primaryContainer, primary)))
            .padding(vertical = 20.dp, horizontal = 16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            // Status badge
            Surface(
                color = when {
                    isLive -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    isFinished -> onPrimary.copy(alpha = 0.25f)
                    else -> onPrimary.copy(alpha = 0.18f)
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when {
                        isLive -> "● LIVE"
                        isFinished -> "Full Time"
                        status == "SCHEDULED" -> "Scheduled"
                        status == "TIMED" -> "Sắp diễn ra"
                        else -> status
                    },
                    color = onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teams + Score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home team
                TeamHeaderItem(
                    name = core?.homeTeam?.name ?: "Home",
                    shortName = core?.homeTeam?.shortName ?: core?.homeTeam?.tla ?: core?.homeTeam?.name?.take(3)?.uppercase() ?: "HME",
                    crestUrl = core?.homeTeam?.crest,
                    modifier = Modifier.weight(1f)
                )

                // Score / VS
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (hasScore) {
                        Text(
                            text = "${homeScore ?: 0}  -  ${awayScore ?: 0}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = onPrimary,
                            letterSpacing = 2.sp
                        )
                        if (core?.score?.halfTime != null) {
                            Text(
                                text = "HT: ${core.score.halfTime.home ?: 0}-${core.score.halfTime.away ?: 0}",
                                fontSize = 11.sp,
                                color = onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = "VS",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = onPrimary.copy(alpha = 0.6f)
                        )
                        val dateStr = formatHeaderDate(core?.utcDate)
                        if (dateStr.isNotEmpty()) {
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = onPrimary.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Away team
                TeamHeaderItem(
                    name = core?.awayTeam?.name ?: "Away",
                    shortName = core?.awayTeam?.shortName ?: core?.awayTeam?.tla ?: core?.awayTeam?.name?.take(3)?.uppercase() ?: "AWY",
                    crestUrl = core?.awayTeam?.crest,
                    modifier = Modifier.weight(1f)
                )
            }

            // Venue
            if (!venue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📍 $venue",
                    fontSize = 11.sp,
                    color = onPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TeamHeaderItem(
    name: String,
    shortName: String,
    crestUrl: String?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        AsyncImage(
            model = crestUrl,
            contentDescription = name,
            modifier = Modifier.size(56.dp),
            placeholder = painterResource(id = R.drawable.ic_ball),
            error = painterResource(id = R.drawable.ic_ball),
            fallback = painterResource(id = R.drawable.ic_ball)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = shortName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Summary Tab ─────────────────────────────────────────────────────────────

@Composable
fun SummaryTab(events: List<MatchEvent>) {
    if (events.isEmpty()) {
        EmptyState(message = "Không có sự kiện trận đấu")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(events) { event ->
            EventRow(event = event)
        }
    }
}

@Composable
private fun EventRow(event: MatchEvent) {
    val isHome = event.team?.let { t ->
        !t.contains("away", ignoreCase = true) && !t.contains("khách", ignoreCase = true)
    } ?: true

    val (emoji, bgColor) = when (event.type.uppercase()) {
        "GOAL", "REGULAR_SEASON_GOAL" -> "⚽" to MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        "YELLOW_CARD" -> "🟨" to MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        "RED_CARD", "YELLOW_RED_CARD" -> "🟥" to MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        "SUBSTITUTION" -> "🔄" to MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        "PENALTY_GOAL", "PENALTY_MISSED" -> "🎯" to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
        else -> "•" to Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isHome) {
            // minute | emoji | description (left-aligned for home)
            Text(
                text = "${event.minute}'",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(36.dp)
            )
            Text(text = emoji, fontSize = 18.sp, modifier = Modifier.width(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.description, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                if (!event.team.isNullOrBlank()) {
                    Text(text = event.team, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            // Away: right-aligned
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(text = event.description, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
                if (!event.team.isNullOrBlank()) {
                    Text(text = event.team, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                }
            }
            Text(text = emoji, fontSize = 18.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
            Text(
                text = "${event.minute}'",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ── Stats Tab ────────────────────────────────────────────────────────────────

@Composable
fun StatsTab(stats: List<MatchStatItem>) {
    if (stats.isEmpty()) {
        EmptyState(message = "Không có thống kê")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(stats) { stat ->
            StatProgressRow(stat = stat)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatProgressRow(stat: MatchStatItem) {
    val homeVal = stat.homeValue.toIntOrNull() ?: 0
    val awayVal = stat.awayValue.toIntOrNull() ?: 0
    val total = homeVal + awayVal
    val homeFrac = if (total > 0) homeVal.toFloat() / total else 0.5f
    val awayFrac = 1f - homeFrac

    val homeAnim by animateFloatAsState(
        targetValue = homeFrac,
        animationSpec = tween(durationMillis = 600),
        label = "homeBar"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Column(modifier = Modifier.fillMaxWidth()) {
        // Stat name + values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.homeValue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
            Text(
                text = stat.name,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stat.awayValue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = secondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Dual progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(homeAnim.coerceAtLeast(0.02f))
                    .fillMaxHeight()
                    .background(primary)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight((1f - homeAnim).coerceAtLeast(0.02f))
                    .fillMaxHeight()
                    .background(secondary)
            )
        }
    }
}

// ── Lineups Tab ──────────────────────────────────────────────────────────────

@Composable
fun LineupsTab(lineups: List<MatchLineupTeam>) {
    if (lineups.isEmpty()) {
        EmptyState(message = "Không có dữ liệu đội hình")
        return
    }

    val home = lineups.getOrNull(0)
    val away = lineups.getOrNull(1)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Formation header
        if (home != null || away != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FormationBadge(teamName = home?.teamName ?: "", formation = home?.formation)
                    FormationBadge(teamName = away?.teamName ?: "", formation = away?.formation, alignEnd = true)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Starters
        item {
            Text(
                "Đội hình chính",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        val maxStarters = maxOf(home?.starters?.size ?: 0, away?.starters?.size ?: 0)
        items(maxStarters) { i ->
            val hp = home?.starters?.getOrNull(i)
            val ap = away?.starters?.getOrNull(i)
            PlayerDuelRow(
                homeName = hp?.name,
                awayName = ap?.name,
                homePos = hp?.position,
                awayPos = ap?.position
            )
        }

        // Substitutes
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Text(
                "Dự bị",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        val maxSubs = maxOf(home?.substitutes?.size ?: 0, away?.substitutes?.size ?: 0)
        items(maxSubs) { i ->
            val hp = home?.substitutes?.getOrNull(i)
            val ap = away?.substitutes?.getOrNull(i)
            PlayerDuelRow(
                homeName = hp?.name,
                awayName = ap?.name,
                homePos = hp?.position,
                awayPos = ap?.position,
                isSub = true
            )
        }
    }
}

@Composable
private fun FormationBadge(teamName: String, formation: String?, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(text = teamName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!formation.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = formation,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerDuelRow(
    homeName: String?,
    awayName: String?,
    homePos: String?,
    awayPos: String?,
    isSub: Boolean = false
) {
    val textAlpha = if (isSub) 0.7f else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home player
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = homeName ?: "—",
                fontSize = 13.sp,
                fontWeight = if (!isSub) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!homePos.isNullOrBlank()) {
                Text(text = homePos, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // Center divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        // Away player
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(
                text = awayName ?: "—",
                fontSize = 13.sp,
                fontWeight = if (!isSub) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!awayPos.isNullOrBlank()) {
                Text(text = awayPos, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
            }
        }
    }
}

// ── H2H Tab ──────────────────────────────────────────────────────────────────

@Composable
fun H2HTab(h2hList: List<Matche>) {
    if (h2hList.isEmpty()) {
        EmptyState(message = "Không có lịch sử đối đầu")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "Lịch sử đối đầu (${h2hList.size} trận)",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        items(h2hList) { match ->
            H2HMatchCard(match = match)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun H2HMatchCard(match: Matche) {
    val homeGoals = match.score?.fullTime?.home ?: 0
    val awayGoals = match.score?.fullTime?.away ?: 0
    val resultColor = when {
        homeGoals > awayGoals -> MaterialTheme.colorScheme.tertiary
        homeGoals < awayGoals -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    val resultLabel = when {
        homeGoals > awayGoals -> "W"
        homeGoals < awayGoals -> "L"
        else -> "D"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Result badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(resultColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = resultLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = resultColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.homeTeam?.name} vs ${match.awayTeam?.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatHeaderDate(match.utcDate),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$homeGoals - $awayGoals",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = resultColor
            )
        }
    }
}

// ── Common ───────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚽", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatHeaderDate(utcString: String?): String {
    if (utcString.isNullOrEmpty()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(utcString)
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        date?.let { formatter.format(it) } ?: utcString
    } catch (e: Exception) {
        utcString
    }
}
