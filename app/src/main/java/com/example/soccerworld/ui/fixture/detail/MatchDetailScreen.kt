package com.example.soccerworld.ui.fixture.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.EventStatsGroup
import com.example.soccerworld.data.remote.flashlive.EventStatsItem
import com.example.soccerworld.data.remote.flashlive.EventStatsStage
import com.example.soccerworld.model.h2h.AwayTeamX
import com.example.soccerworld.model.h2h.FullTime
import com.example.soccerworld.model.h2h.HomeTeamX
import com.example.soccerworld.model.h2h.Matche
import com.example.soccerworld.model.h2h.Score
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import com.example.soccerworld.model.matchdetail.MatchEnrichmentDetail
import com.example.soccerworld.model.matchdetail.MatchEvent
import com.example.soccerworld.model.matchdetail.MatchHighlight
import com.example.soccerworld.model.matchdetail.MatchLineupPlayer
import com.example.soccerworld.model.matchdetail.MatchLineupTeam
import com.example.soccerworld.model.matchdetail.MatchNews
import com.example.soccerworld.model.statistic.AwayTeam
import com.example.soccerworld.model.statistic.HomeTeam
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.ui.fixture.detail.components.HighlightList
import com.example.soccerworld.ui.fixture.detail.components.getRatingColor
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.ui.theme.TextDark
import com.example.soccerworld.ui.theme.TextSecondary
import com.example.soccerworld.ui.theme.DividerColor
import com.example.soccerworld.ui.theme.LoserText
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import com.example.soccerworld.model.statistic.Score as StatScore
import com.example.soccerworld.model.statistic.FullTime as StatFullTime

@Composable
fun MatchDetailScreen(fixtureId: String, onBack: () -> Unit, onNavigateToLogin: () -> Unit = {}) {
    val context = LocalContext.current
    val factory = ViewModelFactory(Injection.provideFootballRepository(context))
    val matchDetailViewModel: MatchDetailViewModel = viewModel(factory = factory)

    LaunchedEffect(fixtureId) {
        matchDetailViewModel.loadMatchDetail(fixtureId)
    }
    val state by matchDetailViewModel.uiState.collectAsState()
    val selectedTabIndex by matchDetailViewModel.selectedTab.collectAsState()

    MatchDetailContent(
        fixtureId = fixtureId,
        state = state,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { matchDetailViewModel.selectTab(it) },
        onBack = onBack,
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailContent(
    fixtureId: String = "",
    state: MatchDetailUiState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        val tabs = listOf("Details", "Lineups", "Statistics", "News", "Comments", "Matches")

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // ── Match Header ─────────────────────────────────────────
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Brush.verticalGradient(listOf(primaryContainer, primary))),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = onPrimary) }
            } else {
                MatchHeader(core = state.data?.core, enrichment = state.data?.enrichment)
            }

            // ── Tab Row ──────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), thickness = 0.5.dp) },
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 1.5.dp, // Thin premium line
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp, // Sleek font size
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    )
                }
            }

            // ── Tab content ──────────────────────────────────────────
            val error = state.error
            if (error != null && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            } else if (!state.isLoading) {
                val aggregate = state.data
                when (selectedTabIndex) {
                    0 -> SummaryTab(events = aggregate?.enrichment?.events ?: emptyList(), highlights = aggregate?.enrichment?.highlights ?: emptyList())
                    1 -> LineupsTab(
                        lineups = aggregate?.enrichment?.lineups ?: emptyList(),
                        events = aggregate?.enrichment?.events ?: emptyList(),
                        homeTeam = aggregate?.core?.homeTeam,
                        awayTeam = aggregate?.core?.awayTeam
                    )
                    2 -> StatsTab(stages = aggregate?.enrichment?.statStages ?: emptyList())
                    3 -> NewsTab(newsList = aggregate?.enrichment?.news ?: emptyList())
                    4 -> CommentTab(
                        fixtureId = fixtureId,
                        onNavigateToLogin = onNavigateToLogin
                    )
                    5 -> H2HTab(
                        h2hList = aggregate?.h2h ?: emptyList(),
                        homeTeam = aggregate?.core?.homeTeam,
                        awayTeam = aggregate?.core?.awayTeam
                    )
                }
            }
        }
    }
}

// Helper function to format match ISO-8601 UTC date string to local "dd/MM/yyyy • HH:mm"
private fun formatMatchDateTime(utcDateStr: String?): String {
    if (utcDateStr.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDateStr) ?: return ""
        val outputFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        outputFormat.format(date)
    } catch (e: Exception) {
        try {
            val altFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = altFormat.parse(utcDateStr) ?: return ""
            val outputFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            outputFormat.format(date)
        } catch (e2: Exception) {
            utcDateStr
        }
    }
}

// Helper function to group goals by scorer name and aggregate minutes
private fun groupGoals(goalEvents: List<MatchEvent>): List<Pair<String, String>> {
    val grouped = mutableMapOf<String, MutableList<String>>()
    goalEvents.forEach { event ->
        val name = event.description.split(" |")[0].trim()
        val min = event.minute.trim()
        if (name.isNotBlank() && name != "Event") {
            grouped.getOrPut(name) { mutableListOf() }.add(min)
        }
    }
    return grouped.map { (name, minutes) ->
        name to minutes.joinToString(", ")
    }
}

@Composable
fun MatchHeader(core: StatisticsResponse?, enrichment: MatchEnrichmentDetail?) {
    val status = enrichment?.status ?: core?.status ?: "UNKNOWN"
    val homeScore = core?.score?.fullTime?.home
    val awayScore = core?.score?.fullTime?.away
    val isLive = status == "IN_PLAY" || status == "PAUSED"
    val isFinished = status == "FINISHED"
    val hasScore = isLive || isFinished

    // Extract all goal events and group them by team
    val goalEvents = enrichment?.events?.filter { event ->
        val typeUpper = event.type.uppercase()
        typeUpper.contains("GOAL") && !typeUpper.contains("MISSED")
    }.orEmpty()

    val homeGoalEvents = goalEvents.filter { event ->
        val team = event.team
        team == null || (!team.contains("away", ignoreCase = true) && !team.contains("khách", ignoreCase = true))
    }

    val awayGoalEvents = goalEvents.filter { event ->
        val team = event.team
        team != null && (team.contains("away", ignoreCase = true) || team.contains("khách", ignoreCase = true))
    }

    val homeScorers = remember(homeGoalEvents) { groupGoals(homeGoalEvents) }
    val awayScorers = remember(awayGoalEvents) { groupGoals(awayGoalEvents) }

    // Favorites states for interactive home/away favorite stars
    var isHomeFav by remember { mutableStateOf(false) }
    var isAwayFav by remember { mutableStateOf(false) }

    // Redesigned modern match header
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Date-Time Pill centered on top
        val formattedDateTime = remember(core?.utcDate) {
            formatMatchDateTime(core?.utcDate)
        }
        if (formattedDateTime.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Text(
                    text = formattedDateTime,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
        }

        // 2. Symmetrical Scoreboard Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Section (Left)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1.3f)
            ) {
                IconButton(
                    onClick = { isHomeFav = !isHomeFav },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isHomeFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite Home Team",
                        tint = if (isHomeFav) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = core?.homeTeam?.crest,
                        contentDescription = core?.homeTeam?.name,
                        placeholder = painterResource(id = R.drawable.ic_ball), // Shows in Preview
                        error = painterResource(id = R.drawable.ic_ball),       // Shows if URL fails
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = core?.homeTeam?.shortName ?: core?.homeTeam?.name ?: "Home",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Score/Status Column (Center)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (hasScore) {
                    Text(
                        text = "$homeScore - $awayScore",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "vs",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isLive -> "LIVE"
                        isFinished -> "Finished"
                        status == "SCHEDULED" -> "Scheduled"
                        status == "TIMED" -> "Upcoming"
                        else -> status
                    },
                    color = if (isLive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Away Section (Right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1.3f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = core?.awayTeam?.crest,
                        contentDescription = core?.awayTeam?.name,
                        placeholder = painterResource(id = R.drawable.ic_ball), // Shows in Preview
                        error = painterResource(id = R.drawable.ic_ball),       // Shows if URL fails
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = core?.awayTeam?.shortName ?: core?.awayTeam?.name ?: "Away",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { isAwayFav = !isAwayFav },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isAwayFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite Away Team",
                        tint = if (isAwayFav) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. Goal Scorers List at the bottom
        val compactTextStyle = TextStyle(
            fontSize = 10.sp,
            lineHeight = 10.sp, // Match line height to font size
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both // Trims extra space above and below
            )
        )

        if (homeScorers.isNotEmpty() || awayScorers.isNotEmpty()) {
            // Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Home Goal Scorers (Right-aligned)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    homeScorers.forEach { (name, mins) ->
                        val formattedMins = mins.split(",")
                            .joinToString(separator = ", ") { it.trim().removeSuffix("'") + "'" }

                        Text(
                            text = "$name $formattedMins",
                            style = compactTextStyle.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.padding(vertical = 0.dp)
                        )
                    }
                }

                // Centered Soccer Ball Icon
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚽",
                        style = compactTextStyle.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End
                        )
                    )
                }

                // Away Goal Scorers (Left-aligned)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    awayScorers.forEach { (name, mins) ->
                        val formattedMins = mins.split(",")
                            .joinToString(separator = ", ") { it.trim().removeSuffix("'") + "'" }

                        Text(
                            text = "$name $formattedMins",
                            style = compactTextStyle.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.padding(vertical = 0.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Summary Tab ─────────────────────────────────────────────────────────────

@Composable
fun SummaryTab(events: List<MatchEvent>, highlights: List<MatchHighlight>) {
    if (events.isEmpty()) {
        EmptyState(message = "No match events available")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        if (highlights.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Match Highlights",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    HighlightList(highlights)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = 8.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        items(events) { event ->
            EventRow(event = event)
        }
    }
}

@Composable
private fun EventRow(event: MatchEvent) {
    if (event.type == "STAGE_HEADER") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
            Text(
                text = event.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
        return
    }

    if (event.type == "ADDITIONAL_TIME") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = event.description,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        return
    }

    val isHome = event.team?.let { t ->
        !t.contains("away", ignoreCase = true) && !t.contains("khách", ignoreCase = true)
    } ?: true

    val minText = remember(event.minute) {
        val m = event.minute.trim()
        if (m.isEmpty()) ""
        else if (m.endsWith("'")) m
        else "$m'"
    }

    // Extract running score if it is a goal
    val isGoal = event.type.uppercase().contains("GOAL") && !event.type.uppercase().contains("MISSED")
    val goalParts = if (isGoal) event.description.split(" | ") else emptyList()
    val homeScore = goalParts.getOrNull(2)?.trim().orEmpty()
    val awayScore = goalParts.getOrNull(3)?.trim().orEmpty()
    val hasScores = homeScore.isNotBlank() && awayScore.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isHome) {
            // Home event layout: [Minute] [Icon] [Play Score Badge] [Details]
            Text(
                text = minText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Start
            )
            
            Box(
                modifier = Modifier.width(26.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                EventIcon(type = event.type)
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                EventDetails(event = event, isHome = true)
            }
        } else {
            // Away event layout: [Details] [Play Score Badge] [Icon] [Minute]
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                EventDetails(event = event, isHome = false)
            }

            Box(
                modifier = Modifier.width(26.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                EventIcon(type = event.type)
            }

            Text(
                text = minText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun EventIcon(type: String) {
    val upperType = type.uppercase()
    when {
        upperType.contains("GOAL") -> {
            Text(text = "⚽", fontSize = 13.sp)
        }
        upperType.contains("YELLOW_CARD") -> {
            Box(
                modifier = Modifier
                    .size(width = 9.dp, height = 13.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFFFBC02D))
            )
        }
        upperType.contains("RED_CARD") || upperType.contains("YELLOW_RED_CARD") -> {
            Box(
                modifier = Modifier
                    .size(width = 9.dp, height = 13.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFFD32F2F))
            )
        }
        upperType.contains("SUBSTITUTION") -> {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Substitution",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
        }
        upperType.contains("VAR") -> {
            Surface(
                color = Color(0xFF1E88E5),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "VAR",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                )
            }
        }
        else -> {
            Text(text = "•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun EventDetails(event: MatchEvent, isHome: Boolean) {
    val upperType = event.type.uppercase()
    when {
        upperType.contains("GOAL") -> {
            val parts = event.description.split(" |")
            val scorer = parts.getOrNull(0)?.trim().orEmpty()
            val assist = parts.getOrNull(1)?.trim().orEmpty()
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isHome) {
                    Text(text = scorer, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (assist.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = assist, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                } else {
                    if (assist.isNotBlank()) {
                        Text(text = assist, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(text = scorer, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        upperType.contains("SUBSTITUTION") -> {
            val parts = event.description.split(" |")
            val playerIn = parts.getOrNull(0)?.trim().orEmpty()
            val playerOut = parts.getOrNull(1)?.trim().orEmpty()
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isHome) {
                    Text(text = playerIn, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (playerOut.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = playerOut, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                } else {
                    if (playerOut.isNotBlank()) {
                        Text(text = playerOut, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(text = playerIn, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        upperType.contains("CARD") -> {
            val parts = event.description.split(" |")
            val playerName = parts.getOrNull(0)?.trim().orEmpty()
            val reason = parts.getOrNull(1)?.trim().orEmpty()
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isHome) {
                    Text(text = playerName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (reason.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                } else {
                    if (reason.isNotBlank()) {
                        Text(text = reason, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(text = playerName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        else -> {
            Text(
                text = event.description,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (isHome) TextAlign.Start else TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Stats Tab ────────────────────────────────────────────────────────────────
// Sofascore standard colors
private val HomeColor = Color(0xFF00B050) // Vibrant Green
private val AwayColor = Color(0xFF2B44FF) // Deep Blue
private val AppBackground = Color(0xFFF0F1F5) // Light gray-blue app background

fun isHighBetter(statName: String): Boolean {
    val lowIsBetterStats = listOf(
        "Fouls", "Errors leading to shot", "Errors leading to goal",
        "Offsides", "Yellow cards", "Red cards"
    )
    return !lowIsBetterStats.any { statName.contains(it, ignoreCase = true) }
}

@Composable
fun StatsTab(stages: List<EventStatsStage>) {
    if (stages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có thống kê", color = Color.Gray)
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // 1. Top Stage Toggle (ALL / 1ST / 2ND)
        StageToggleBar(
            stages = stages,
            selectedIndex = selectedTabIndex,
            onSelect = { selectedTabIndex = it }
        )

        val selectedStage = stages.getOrNull(selectedTabIndex)

        // 2. Scrollable List of Stat Groups
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            selectedStage?.groups?.forEach { group ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Group Header (e.g., "Match overview", "Attack")
                            Text(
                                text = group.groupLabel ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF111111),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                textAlign = TextAlign.Center
                            )

                            // Stat Items
                            group.items?.forEach { stat ->
                                StatProgressRow(stat = stat)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageToggleBar(
    stages: List<EventStatsStage>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color(0xFFEBECEF), RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stages.forEachIndexed { index, stage ->
            val isSelected = index == selectedIndex
            // Map JSON names to Sofascore UI labels
            val tabName = when (stage.stageName) {
                "Match" -> "ALL"
                "1st Half" -> "1ST"
                "2nd Half" -> "2ND"
                else -> stage.stageName?.uppercase() ?: ""
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) Color(0xFF222226) else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabName,
                    color = if (isSelected) Color.White else Color(0xFF444444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatProgressRow(stat: EventStatsItem) {
    val homeStr = stat.valueHome ?: "0"
    val awayStr = stat.valueAway ?: "0"

    // Sofascore differentiates styling between percentages and absolute numbers
    val isPercentage = homeStr.contains("%") || awayStr.contains("%")

    // Parse floats safely (strip '%' if present)
    val homeVal = homeStr.replace("%", "").toFloatOrNull() ?: 0f
    val awayVal = awayStr.replace("%", "").toFloatOrNull() ?: 0f

    val highIsBetter = isHighBetter(stat.incidentName ?: "")

    val isHomeWorse = if (highIsBetter) homeVal < awayVal else homeVal > awayVal
    val isAwayWorse = if (highIsBetter) awayVal < homeVal else awayVal > homeVal

    val homeBarColor = if (isHomeWorse && homeVal != awayVal) HomeColor.copy(alpha = 0.3f) else HomeColor
    val awayBarColor = if (isAwayWorse && homeVal != awayVal) AwayColor.copy(alpha = 0.3f) else AwayColor

    // Use absolute values to handle negative stats safely (e.g., "Goals prevented" -> -1.07)
    val homeAbs = abs(homeVal)
    val awayAbs = abs(awayVal)
    val totalAbs = homeAbs + awayAbs

    // Calculate fractions for the bars
    val homeFrac = if (totalAbs > 0f) homeAbs / totalAbs else 0f
    val awayFrac = if (totalAbs > 0f) awayAbs / totalAbs else 0f

    val homeAnim by animateFloatAsState(targetValue = homeFrac, animationSpec = tween(600), label = "homeAnim")
    val awayAnim by animateFloatAsState(targetValue = awayFrac, animationSpec = tween(600), label = "awayAnim")

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {

        // ─── 1. Header Values & Labels ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Value
            if (isPercentage) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(HomeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = homeStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = homeStr,
                    fontSize = 14.sp,
                    color = Color(0xFF111111),
                    modifier = Modifier.weight(1f)
                )
            }

            // Stat Title (Center)
            Text(
                text = stat.incidentName ?: "",
                fontSize = 13.sp,
                color = Color(0xFF555555), // Muted dark gray
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(if (isPercentage) 1.5f else 2f)
            )

            // Away Value
            if (isPercentage) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AwayColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = awayStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = awayStr,
                    fontSize = 14.sp,
                    color = Color(0xFF111111),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─── 2. Progress Bars ───
        if (isPercentage) {
            // Continuous connected bar for percentages (e.g., Ball Possession 53% | 47%)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(modifier = Modifier.weight(homeAnim.coerceAtLeast(0.01f)).fillMaxHeight().background(HomeColor))
                Box(modifier = Modifier.weight(awayAnim.coerceAtLeast(0.01f)).fillMaxHeight().background(AwayColor))
            }
        } else {
            // Split, center-anchored bars for absolute counts (e.g., Total Shots 9 vs 14)
            Row(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Home Bar (Right-aligned inside its left-half bounds)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)),
                    contentAlignment = Alignment.CenterEnd // Anchors growth to the center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(HomeColor.copy(alpha = 0.2f)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = homeAnim)
                            .fillMaxHeight()
                            .background(homeBarColor)
                    )
                }

                // Center Gap
                Spacer(modifier = Modifier.width(4.dp))

                // Away Bar (Left-aligned inside its right-half bounds)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)),
                    contentAlignment = Alignment.CenterStart // Anchors growth to the center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(AwayColor.copy(alpha = 0.2f)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = awayAnim)
                            .fillMaxHeight()
                            .background(awayBarColor)
                    )
                }
            }
        }
    }
}

// ── Lineups Tab ──────────────────────────────────────────────────────────────

@Composable
fun LineupsTab(
    lineups: List<MatchLineupTeam>,
    events: List<MatchEvent>,
    homeTeam: HomeTeam?,
    awayTeam: AwayTeam?
) {
    if (lineups.isEmpty()) {
        EmptyState(message = "Không có dữ liệu đội hình")
        return
    }

    val home = lineups.getOrNull(0)
    val away = lineups.getOrNull(1)

    var selectedTeamTabIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 12.dp)
    ) {
        // Formation headers and Pitch View wrapped inside a unified premium Card container
        if (home != null && away != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        TeamLineupHeader(
                            teamName = homeTeam?.name ?: home.teamName,
                            formation = home.formation,
                            crestUrl = homeTeam?.crest,
                            averageRating = home.averageRating
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))

                        com.example.soccerworld.ui.fixture.detail.components.InteractivePitchView(
                            homeFormation = home.toFormation(isHome = true),
                            awayFormation = away.toFormation(isHome = false)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        TeamLineupHeader(
                            teamName = awayTeam?.name ?: away.teamName,
                            formation = away.formation,
                            crestUrl = awayTeam?.crest,
                            averageRating = away.averageRating
                        )
                    }
                }
            }
        }

        // Tabbed Substitutes
        item {
            Spacer(modifier = Modifier.height(14.dp))
            TabRow(
                selectedTabIndex = selectedTeamTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 0.5.dp) }
            ) {
                Tab(
                    selected = selectedTeamTabIndex == 0,
                    onClick = { selectedTeamTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (homeTeam?.crest != null) {
                                AsyncImage(model = homeTeam.crest, contentDescription = "Home", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text("Home Subs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTeamTabIndex == 1,
                    onClick = { selectedTeamTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (awayTeam?.crest != null) {
                                AsyncImage(model = awayTeam.crest, contentDescription = "Away", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text("Away Subs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        val activeTeam = if (selectedTeamTabIndex == 0) home else away
        
        // Coach
        if (activeTeam?.coach != null) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val coachImage = if (activeTeam.coach.imageUrl != null) "https://www.flashscore.com/res/image/data/${activeTeam.coach.imageUrl}" else null
                        AsyncImage(
                            model = coachImage,
                            contentDescription = "Coach",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = activeTeam.coach.name, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = "Coach", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Substitutions Card List
        item {
            Spacer(modifier = Modifier.height(12.dp))
            val subs = activeTeam?.substitutes ?: emptyList()
            if (subs.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        subs.forEachIndexed { index, sub ->
                            DetailedSubstituteRow(sub, events)
                            if (index < subs.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 52.dp, end = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No substitutes available", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TeamLineupHeader(
    teamName: String,
    formation: String?,
    crestUrl: String?,
    averageRating: Double?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Crest + Name + Average Rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (crestUrl != null) {
                AsyncImage(
                    model = crestUrl,
                    contentDescription = teamName,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = teamName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (averageRating != null) {
                Spacer(modifier = Modifier.width(6.dp))
                val ratingColor = getRatingColor(averageRating)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(ratingColor)
                        .padding(horizontal = 3.5.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", averageRating),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        
        // Right Side: Formation Label
        if (!formation.isNullOrBlank()) {
            val displayFormation = formation.removePrefix("1-")
            if (displayFormation.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = displayFormation,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedSubstituteRow(sub: MatchLineupPlayer, events: List<MatchEvent>) {
    val subEvent = events.find { 
        (it.type.equals("SUBSTITUTION", ignoreCase = true) || it.type.equals("subst", ignoreCase = true)) && 
        it.description.contains(sub.name, ignoreCase = true) 
    }
    val outPlayer = if (subEvent != null && subEvent.description.contains(" |")) {
        subEvent.description.split(" |").getOrNull(1)?.trim()
    } else {
        subEvent?.description?.split(",")?.find { it.contains("out", ignoreCase = true) }?.replace("out", "", ignoreCase = true)?.trim()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to player profile */ }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        val imageUrl = if (sub.imageUrl != null) "https://www.flashscore.com/res/image/data/${sub.imageUrl}" else null
        AsyncImage(
            model = imageUrl,
            contentDescription = sub.name,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            error = painterResource(id = R.drawable.ic_ball)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name and details
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (sub.number != null) {
                    Text(
                        text = sub.number.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(18.dp)
                    )
                }
                Text(
                    text = sub.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                sub.incidents?.let { incidents ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (inc in incidents) {
                            if (inc == 3 || inc == 10) {
                                com.example.soccerworld.ui.fixture.detail.components.IncidentIcon(inc)
                            }
                        }
                    }
                }
            }
            if (subEvent != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getSwapHorizIcon(),
                        contentDescription = "Sub In",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF388E3C)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${subEvent.minute}'",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Out: ${outPlayer ?: "unknown"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Rating
        if (sub.rating != null) {
            Spacer(modifier = Modifier.width(8.dp))
            val ratingColor = getRatingColor(sub.rating)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ratingColor)
                    .padding(horizontal = 3.dp, vertical = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sub.rating,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getSwapHorizIcon() = Icons.Default.SwapHoriz

// ── H2H Tab ──────────────────────────────────────────────────────────────────

@Composable
fun H2HTab(
    h2hList: List<Matche>,
    homeTeam: HomeTeam? = null,
    awayTeam: AwayTeam? = null
) {
    if (h2hList.isEmpty()) {
        EmptyState(message = "No Match found")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "Previous Matches",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    h2hList.forEachIndexed { index, match ->
                        H2HMatchRow(match = match, homeTeam = homeTeam, awayTeam = awayTeam)
                        if (index < h2hList.size - 1) {
                            HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun H2HMatchRow(
    match: Matche,
    homeTeam: HomeTeam?,
    awayTeam: AwayTeam?
) {
    val homeGoals = match.score?.fullTime?.home
    val awayGoals = match.score?.fullTime?.away
    val homeWins = (homeGoals ?: 0) > (awayGoals ?: 0)
    val awayWins = (awayGoals ?: 0) > (homeGoals ?: 0)
    val ballPainter = painterResource(id = R.drawable.ic_ball)

    // Remove any prepended '*' from team names
    val rawHomeName = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "TBD"
    val rawAwayName = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "TBD"
    val cleanHomeName = remember(rawHomeName) { rawHomeName.removePrefix("*").trim() }
    val cleanAwayName = remember(rawAwayName) { rawAwayName.removePrefix("*").trim() }

    // Retrieve clean current match team names for mapping the crests
    val currentHomeName = remember(homeTeam?.name) { homeTeam?.name?.removePrefix("*")?.trim() ?: "" }
    val currentAwayName = remember(awayTeam?.name) { awayTeam?.name?.removePrefix("*")?.trim() ?: "" }

    // Resolve the crest URLs based on name matches with the current Home/Away teams
    val resolvedHomeCrest = remember(cleanHomeName, currentHomeName, currentAwayName, homeTeam?.crest, awayTeam?.crest) {
        when {
            currentHomeName.isNotEmpty() && (cleanHomeName.contains(currentHomeName, ignoreCase = true) || currentHomeName.contains(cleanHomeName, ignoreCase = true)) -> homeTeam?.crest
            currentAwayName.isNotEmpty() && (cleanHomeName.contains(currentAwayName, ignoreCase = true) || currentAwayName.contains(cleanHomeName, ignoreCase = true)) -> awayTeam?.crest
            else -> match.homeTeam?.crest
        }
    }
    val resolvedAwayCrest = remember(cleanAwayName, currentHomeName, currentAwayName, homeTeam?.crest, awayTeam?.crest) {
        when {
            currentHomeName.isNotEmpty() && (cleanAwayName.contains(currentHomeName, ignoreCase = true) || currentHomeName.contains(cleanAwayName, ignoreCase = true)) -> homeTeam?.crest
            currentAwayName.isNotEmpty() && (cleanAwayName.contains(currentAwayName, ignoreCase = true) || currentAwayName.contains(cleanAwayName, ignoreCase = true)) -> awayTeam?.crest
            else -> match.awayTeam?.crest
        }
    }

    val formattedDate = remember(match.utcDate) {
        if (match.utcDate.isNullOrEmpty()) ""
        else {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = parser.parse(match.utcDate)
                val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
                date?.let { formatter.format(it) } ?: ""
            } catch (_: Exception) {
                ""
            }
        }
    }
    val formattedYear = remember(match.utcDate) {
        if (match.utcDate.isNullOrEmpty()) ""
        else {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = parser.parse(match.utcDate)
                val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
                date?.let { formatter.format(it) } ?: ""
            } catch (_: Exception) {
                ""
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date / Year column — fixed width
        Column(
            modifier = Modifier.width(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formattedYear,
                fontSize = 10.sp,
                color = TextSecondary
            )
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
                    model = resolvedHomeCrest,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = ballPainter,
                    error = ballPainter,
                    fallback = ballPainter
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cleanHomeName,
                    fontSize = 13.sp,
                    fontWeight = if (homeWins) FontWeight.Bold else FontWeight.Normal,
                    color = if (awayWins) LoserText else TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Home score
                Text(
                    text = "${homeGoals ?: 0}",
                    fontSize = 13.sp,
                    fontWeight = if (homeWins) FontWeight.Bold else FontWeight.Normal,
                    color = if (awayWins) LoserText else TextDark,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Away team row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = resolvedAwayCrest,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = ballPainter,
                    error = ballPainter,
                    fallback = ballPainter
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cleanAwayName,
                    fontSize = 13.sp,
                    fontWeight = if (awayWins) FontWeight.Bold else FontWeight.Normal,
                    color = if (homeWins) LoserText else TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Away score
                Text(
                    text = "${awayGoals ?: 0}",
                    fontSize = 13.sp,
                    fontWeight = if (awayWins) FontWeight.Bold else FontWeight.Normal,
                    color = if (homeWins) LoserText else TextDark,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(24.dp)
                )
            }
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
    } catch (_: Exception) {
        utcString
    }
}

private fun MatchLineupTeam.toFormation(isHome: Boolean): com.example.soccerworld.data.remote.flashlive.dto.Formation {
    var disp = this.formation?.takeIf { it.isNotBlank() } ?: "4-4-2"
    val sum = disp.split("-").mapNotNull { it.toIntOrNull() }.sum()
    if (sum < 11 && !disp.startsWith("1-")) {
        disp = "1-$disp"
    }

    val members = this.starters.mapIndexed { index, p ->
        val safeName = p.name
        com.example.soccerworld.data.remote.flashlive.dto.LineupPlayer(
            id = index.toString(),
            fullName = safeName + if (p.isCaptain) " (C)" else "",
            shortName = p.shortName.takeIf { it.isNotBlank() } ?: safeName.split(" ").lastOrNull() ?: safeName,
            number = p.number,
            rating = p.rating,
            imageId = p.imageUrl,
            incidents = p.incidents,
            position = p.fieldPosition ?: (index + 1)
        )
    }
    return com.example.soccerworld.data.remote.flashlive.dto.Formation(
        teamSide = if (isHome) 1 else 2,
        disposition = disp,
        members = members
    )
}

@Preview(showBackground = true)
@Composable
fun MatchDetailScreenPreview() {
    val mockData = MatchDetailAggregate(
        core = StatisticsResponse(
            utcDate = "20/10/2026 - 21:00",
            homeTeam = HomeTeam(name = "Arsenal", shortName = "ARS", crest = null),
            awayTeam = AwayTeam(name = "Chelsea", shortName = "CHE", crest = null),
            score = StatScore(fullTime = StatFullTime(home = 2, away = 0)),
            status = "FINISHED"
        ),
        h2h = emptyList(),
        enrichment = MatchEnrichmentDetail(
            eventId = "1",
            venue = "Emirates Stadium",
            status = "FINISHED",
            lastUpdated = 0L,
            events = listOf(
                MatchEvent(minute = "15", type = "GOAL", description = "Odegaard", team = "home"),
                MatchEvent(minute = "30", type = "GOAL", description = "Saka", team = "home")
            ),
            stats = emptyList(),
            highlights = emptyList(),
            lineups = listOf(
                MatchLineupTeam(
                    teamName = "Arsenal",
                    formation = "1-4-3-3",
                    averageRating = 7.5,
                    starters = emptyList(),
                    substitutes = emptyList()
                ),
                MatchLineupTeam(
                    teamName = "Chelsea",
                    formation = "1-4-2-3-1",
                    averageRating = 6.8,
                    starters = emptyList(),
                    substitutes = emptyList()
                )
            )
        )
    )

    SoccerWorldTheme {
        MatchDetailContent(
            state = MatchDetailUiState(isLoading = false, data = mockData),
            selectedTabIndex = 0,
            onTabSelected = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchHeaderPreview() {
    val core = StatisticsResponse(
        utcDate = "02/02/2026 20:00",
        homeTeam = HomeTeam(name = "Arsenal", shortName = "ARS", crest = "https://www.flashscore.com/res/image/data/40juIezB-b92lfEJC.png"),
        awayTeam = AwayTeam(name = "Chelsea", shortName = "CHE", crest = null),
        score = StatScore(fullTime = StatFullTime(home = 4, away = 1)),
        status = "FINISHED"
    )

    val events = listOf(
        MatchEvent(minute = "10", type = "GOAL", description = "Martinelli", team = "home"),
        MatchEvent(minute = "60", type = "GOAL", description = "Mudryk", team = "away"),
        MatchEvent(minute = "70", type = "GOAL", description = "Martinelli", team = "home"),
        MatchEvent(minute = "80", type = "GOAL", description = "Martinelli", team = "home"),
        MatchEvent(minute = "82", type = "GOAL", description = "Saka", team = "home")
    )
    val enrichment = MatchEnrichmentDetail(
        eventId = "1",
        venue = "Emirates Stadium",
        status = "FINISHED",
        lastUpdated = 0L,
        events = events,
        stats = emptyList(),
        highlights = emptyList(),
        lineups = emptyList()
    )

    SoccerWorldTheme {
        MatchHeader(core = core, enrichment = enrichment)
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryTabPreview() {
    val mockEvents = listOf(
        MatchEvent(minute = "10", type = "GOAL", description = "Goal by Martinelli", team = "home"),
        MatchEvent(minute = "25", type = "YELLOW_CARD", description = "Yellow card for Xhaka", team = "home"),
        MatchEvent(minute = "45", type = "SUBSTITUTION", description = "Jesus out, Nketiah in", team = "home"),
        MatchEvent(minute = "60", type = "GOAL", description = "Goal by Mudryk", team = "away"),
        MatchEvent(minute = "75", type = "RED_CARD", description = "Red card for Cucurella", team = "away")
    )
    SoccerWorldTheme {
        SummaryTab(events = mockEvents, highlights = emptyList())
    }
}

@Preview(showBackground = true)
@Composable
fun StatsTabPreview() {
    val mockStages = listOf(
        EventStatsStage(
            stageName = "Match",
            groups = listOf(
                EventStatsGroup(
                    groupLabel = "Top stats",
                    items = listOf(
                        EventStatsItem(incidentName = "Expected goals (xG)", valueHome = "1.14", valueAway = "2.26"),
                        EventStatsItem(incidentName = "Ball possession", valueHome = "53%", valueAway = "47%"),
                        EventStatsItem(incidentName = "Total shots", valueHome = "9", valueAway = "14"),
                        EventStatsItem(incidentName = "Shots on target", valueHome = "3", valueAway = "7"),
                        EventStatsItem(incidentName = "Big chances", valueHome = "3", valueAway = "5"),
                        EventStatsItem(incidentName = "Corner kicks", valueHome = "5", valueAway = "8"),
                        EventStatsItem(incidentName = "Passes", valueHome = "81% (378/465)", valueAway = "82% (326/400)"),
                        EventStatsItem(incidentName = "Yellow cards", valueHome = "1", valueAway = "3")
                    )
                ),
                EventStatsGroup(
                    groupLabel = "Shots",
                    items = listOf(
                        EventStatsItem(incidentName = "xG on target (xGOT)", valueHome = "1.93", valueAway = "1.48"),
                        EventStatsItem(incidentName = "Shots off target", valueHome = "2", valueAway = "6"),
                        EventStatsItem(incidentName = "Blocked shots", valueHome = "4", valueAway = "1"),
                        EventStatsItem(incidentName = "Shots inside the box", valueHome = "6", valueAway = "11"),
                        EventStatsItem(incidentName = "Shots outside the box", valueHome = "3", valueAway = "3"),
                        EventStatsItem(incidentName = "Hit the woodwork", valueHome = "0", valueAway = "2")
                    )
                ),
                EventStatsGroup(
                    groupLabel = "Defense",
                    items = listOf(
                        EventStatsItem(incidentName = "Tackles", valueHome = "67% (6/9)", valueAway = "77% (10/13)"),
                        EventStatsItem(incidentName = "Duels won", valueHome = "45", valueAway = "42"),
                        EventStatsItem(incidentName = "Clearances", valueHome = "36", valueAway = "29"),
                        EventStatsItem(incidentName = "Errors leading to shot", valueHome = "2", valueAway = "1")
                    )
                ),
                EventStatsGroup(
                    groupLabel = "Goalkeeping",
                    items = listOf(
                        EventStatsItem(incidentName = "Goalkeeper saves", valueHome = "5", valueAway = "0"),
                        EventStatsItem(incidentName = "Goals prevented", valueHome = "0.48", valueAway = "-1.07")
                    )
                )
            )
        ),
        EventStatsStage(
            stageName = "1st Half",
            groups = listOf(
                EventStatsGroup(
                    groupLabel = "Top stats",
                    items = listOf(
                        EventStatsItem(incidentName = "Expected goals (xG)", valueHome = "1.07", valueAway = "1.32"),
                        EventStatsItem(incidentName = "Ball possession", valueHome = "57%", valueAway = "43%"),
                        EventStatsItem(incidentName = "Total shots", valueHome = "7", valueAway = "8")
                    )
                )
            )
        ),
        EventStatsStage(
            stageName = "2nd Half",
            groups = listOf(
                EventStatsGroup(
                    groupLabel = "Top stats",
                    items = listOf(
                        EventStatsItem(
                            incidentName = "Expected goals (xG)",
                            valueHome = "0.07",
                            valueAway = "0.94"
                        ),
                        EventStatsItem(
                            incidentName = "Ball possession",
                            valueHome = "49%",
                            valueAway = "51%"
                        ),
                        EventStatsItem(
                            incidentName = "Total shots",
                            valueHome = "2",
                            valueAway = "6"
                        )
                    )
                )
            )
        )
    )

    SoccerWorldTheme {
        StatsTab(stages = mockStages)
    }
}

@Preview(showBackground = true)
@Composable
fun LineupsTabPreview() {
    val homeStarters = listOf(
        MatchLineupPlayer(name = "Ramsdale", shortName = "Ramsdale", number = 1, position = "G", imageUrl = null, rating = "7.0"),
        MatchLineupPlayer(name = "White", shortName = "White", number = 4, position = "D", imageUrl = null, rating = "7.2"),
        MatchLineupPlayer(name = "Saliba", shortName = "Saliba", number = 2, position = "D", imageUrl = null, rating = "7.5"),
        MatchLineupPlayer(name = "Gabriel", shortName = "Gabriel", number = 6, position = "D", imageUrl = null, rating = "7.3"),
        MatchLineupPlayer(name = "Zinchenko", shortName = "Zinchenko", number = 35, position = "D", imageUrl = null, rating = "7.1"),
        MatchLineupPlayer(name = "Odegaard", shortName = "Odegaard", number = 8, position = "M", imageUrl = null, rating = "8.2", isCaptain = true),
        MatchLineupPlayer(name = "Partey", shortName = "Partey", number = 5, position = "M", imageUrl = null, rating = "7.4"),
        MatchLineupPlayer(name = "Xhaka", shortName = "Xhaka", number = 34, position = "M", imageUrl = null, rating = "7.6"),
        MatchLineupPlayer(name = "Saka", shortName = "Saka", number = 7, position = "M", imageUrl = null, rating = "8.5", incidents = listOf(3)),
        MatchLineupPlayer(name = "Jesus", shortName = "Jesus", number = 9, position = "F", imageUrl = null, rating = "7.8"),
        MatchLineupPlayer(name = "Martinelli", shortName = "Martinelli", number = 11, position = "F", imageUrl = null, rating = "7.9")
    )
    val awayStarters = listOf(
        MatchLineupPlayer(name = "Kepa", shortName = "Kepa", number = 1, position = "G", imageUrl = null, rating = "6.5"),
        MatchLineupPlayer(name = "James", shortName = "James", number = 24, position = "D", imageUrl = null, rating = "6.8"),
        MatchLineupPlayer(name = "Silva", shortName = "Silva", number = 6, position = "D", imageUrl = null, rating = "7.0"),
        MatchLineupPlayer(name = "Fofana", shortName = "Fofana", number = 33, position = "D", imageUrl = null, rating = "6.4"),
        MatchLineupPlayer(name = "Chilwell", shortName = "Chilwell", number = 21, position = "D", imageUrl = null, rating = "6.7"),
        MatchLineupPlayer(name = "Enzo", shortName = "Enzo", number = 5, position = "M", imageUrl = null, rating = "6.9"),
        MatchLineupPlayer(name = "Kovacic", shortName = "Kovacic", number = 8, position = "M", imageUrl = null, rating = "6.6"),
        MatchLineupPlayer(name = "Mudryk", shortName = "Mudryk", number = 15, position = "M", imageUrl = null, rating = "6.5"),
        MatchLineupPlayer(name = "Sterling", shortName = "Sterling", number = 17, position = "F", imageUrl = null, rating = "6.3"),
        MatchLineupPlayer(name = "Havertz", shortName = "Havertz", number = 29, position = "F", imageUrl = null, rating = "6.2"),
        MatchLineupPlayer(name = "Felix", shortName = "Felix", number = 11, position = "F", imageUrl = null, rating = "6.8")
    )

    val lineups = listOf(
        MatchLineupTeam(
            teamName = "Arsenal",
            formation = "1-4-3-3",
            averageRating = 7.5,
            starters = homeStarters,
            substitutes = listOf(MatchLineupPlayer(name = "Trossard", shortName = "Trossard", number = 19, position = "F", imageUrl = null, rating = "6.0")),
            coach = MatchLineupPlayer(name = "Mikel Arteta", shortName = "Arteta", position = "Coach", imageUrl = null)
        ),
        MatchLineupTeam(
            teamName = "Chelsea",
            formation = "1-4-3-3",
            averageRating = 6.8,
            starters = awayStarters,
            substitutes = emptyList(),
            coach = MatchLineupPlayer(name = "Frank Lampard", shortName = "Lampard", position = "Coach", imageUrl = null)
        )
    )

    val events = listOf(
        MatchEvent(minute = "70", type = "subst", description = "Trossard in, Saka out", team = "home")
    )

    SoccerWorldTheme {
        LineupsTab(
            lineups = lineups,
            events = events,
            homeTeam = HomeTeam(name = "Arsenal", shortName = "ARS", crest = null),
            awayTeam = AwayTeam(name = "Chelsea", shortName = "CHE", crest = null)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun H2HTabPreview() {
    val mockH2H = listOf(
        Matche(
            utcDate = "2023-04-26T19:00:00Z",
            homeTeam = HomeTeamX(name = "Man City", shortName = "MCI", crest = null),
            awayTeam = AwayTeamX(name = "Arsenal", shortName = "ARS", crest = null),
            score = Score(fullTime = FullTime(home = 4, away = 1))

    ),
        Matche(
            utcDate = "2023-02-15T19:30:00Z",
            homeTeam = HomeTeamX(name = "Arsenal", shortName = "ARS", crest = null),
            awayTeam = AwayTeamX(name = "Man City", shortName = "MCI", crest = null),
            score = Score(fullTime = FullTime(home = 1, away = 3))
        ),
        Matche(
            utcDate = "2022-01-01T12:30:00Z",
            homeTeam = HomeTeamX(name = "Arsenal", shortName = "ARS", crest = null),
            awayTeam = AwayTeamX(name = "Man City", shortName = "MCI", crest = null),
            score = Score(fullTime = FullTime(home = 1, away = 2))
        )
    )
    SoccerWorldTheme {
        H2HTab(h2hList = mockH2H)
    }
}

@Composable
fun NewsTab(newsList: List<MatchNews>) {
    if (newsList.isEmpty()) {
        EmptyState(message = "Không có tin tức liên quan")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(newsList) { item ->
            NewsCard(item = item)
        }
    }
}

@Composable
fun NewsCard(item: MatchNews) {
    val context = LocalContext.current
    val imageUrl = remember(item.imageUrl) { item.imageUrl }
    val formattedTime = remember(item.published) { formatPublishedTime(item.published) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!item.link.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
                    context.startActivity(intent)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Provider badge & Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.providerName ?: "News",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                
                // News Title
                Text(
                    text = item.title ?: "",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            
            // Thumbnail image
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(width = 96.dp, height = 72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_ball)
                )
            }
        }
    }
}

private fun formatPublishedTime(publishedSeconds: Long?): String {
    publishedSeconds ?: return ""
    val now = System.currentTimeMillis()
    val diffMs = now - (publishedSeconds * 1000L)
    if (diffMs < 0) return "Just now"
    
    val diffMinutes = diffMs / 60000L
    if (diffMinutes < 60) return "${diffMinutes.coerceAtLeast(1)}m ago"
    
    val diffHours = diffMinutes / 60
    if (diffHours < 24) return "${diffHours}h ago"
    
    val diffDays = diffHours / 24
    if (diffDays == 1L) return "Yesterday"
    if (diffDays < 7) return "${diffDays}d ago"
    
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(java.util.Date(publishedSeconds * 1000L))
}
