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
import com.example.soccerworld.ui.fixture.detail.util.formatMatchDateTime
import com.example.soccerworld.ui.fixture.detail.util.groupGoals
import com.example.soccerworld.ui.fixture.detail.tabs.*

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
