package com.example.soccerworld.ui.fixture.detail.tabs

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
fun H2HTab(
    h2hList: List<Matche>,
    homeTeam: HomeTeam? = null,
    awayTeam: AwayTeam? = null
) {
    if (h2hList.isEmpty()) {
        EmptyState(message = "No Match found")
        return
    }

    // Group H2H matches by competition to replicate SofaScore layout
    val groupedMatches = remember(h2hList) {
        val groups = linkedMapOf<String, MutableList<Matche>>()
        h2hList.forEach { match ->
            val compName = match.competition?.name ?: "Tournament"
            val list = groups.getOrPut(compName) { mutableListOf() }
            list.add(match)
        }
        groups
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Previous Matches",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
            )
        }

        groupedMatches.forEach { (compName, matchesList) ->
            item(key = compName) {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp))) {
                    // Tournament Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dynamic Tournament Emblem (No simple emoji fallback unless unavailable)
                        val emblemUrl = matchesList.firstOrNull()?.competition?.emblem
                        val ballPainter = painterResource(id = R.drawable.ic_ball)
                        AsyncImage(
                            model = emblemUrl ?: R.drawable.ic_ball,
                            contentDescription = compName,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape),
                            placeholder = ballPainter,
                            error = ballPainter,
                            fallback = ballPainter,
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = compName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Divider between header and first match row
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = DividerColor
                    )
                }
            }
            
            items(
                items = matchesList,
                key = { match -> match.utcDate ?: match.hashCode() },
                contentType = { "h2h_match" }
            ) { match ->
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    H2HMatchRow(
                        match = match,
                        homeTeam = homeTeam,
                        awayTeam = awayTeam
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 0.5.dp,
                        color = DividerColor
                    )
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
    val homeGoals = match.score?.fullTime?.home ?: 0
    val awayGoals = match.score?.fullTime?.away ?: 0

    // Remove any prepended/appended '*' from team names
    val rawHomeName = match.homeTeam?.shortName ?: match.homeTeam?.name ?: "TBD"
    val rawAwayName = match.awayTeam?.shortName ?: match.awayTeam?.name ?: "TBD"
    val cleanHomeName = remember(rawHomeName) { rawHomeName.replace("*", "").trim() }
    val cleanAwayName = remember(rawAwayName) { rawAwayName.replace("*", "").trim() }

    // Retrieve clean current match team names for mapping the crests
    val currentHomeName = remember(homeTeam?.name) { homeTeam?.name?.replace("*", "")?.trim() ?: "" }
    val currentAwayName = remember(awayTeam?.name) { awayTeam?.name?.replace("*", "")?.trim() ?: "" }

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
            currentAwayName.isNotEmpty() && (currentAwayName.contains(cleanAwayName, ignoreCase = true) || cleanAwayName.contains(currentAwayName, ignoreCase = true)) -> awayTeam?.crest
            else -> match.awayTeam?.crest
        }
    }

    val sofaDate = remember(match.utcDate) {
        if (match.utcDate.isNullOrEmpty()) ""
        else {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = parser.parse(match.utcDate)
                val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                date?.let { formatter.format(it) } ?: ""
            } catch (_: Exception) {
                ""
            }
        }
    }

    val ballPainter = painterResource(id = R.drawable.ic_ball)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click if needed */ }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Date & FT
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = sofaDate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "FT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary.copy(alpha = 0.8f)
            )
        }

        // Vertical Line Divider
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(0.7.dp)
                .height(38.dp)
                .background(DividerColor)
        )

        // Center Column: Team Stack (Crests & Names)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            // Home Team Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                AsyncImage(
                    model = resolvedHomeCrest ?: R.drawable.ic_ball,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = ballPainter,
                    error = ballPainter,
                    fallback = ballPainter
                )
                Spacer(modifier = Modifier.width(8.dp))
                val isHomeCurrent = remember(match.homeTeam, currentHomeName) {
                    val name = match.homeTeam?.name ?: ""
                    val id = match.homeTeam?.id ?: ""
                    val currId = homeTeam?.id ?: ""
                    (id.isNotEmpty() && currId.isNotEmpty() && id == currId) ||
                    (name.isNotEmpty() && currentHomeName.isNotEmpty() &&
                     (name.contains(currentHomeName, ignoreCase = true) || currentHomeName.contains(name, ignoreCase = true)))
                }
                Text(
                    text = cleanHomeName,
                    fontSize = 13.sp,
                    fontWeight = if (isHomeCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHomeCurrent) TextDark else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Away Team Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                AsyncImage(
                    model = resolvedAwayCrest ?: R.drawable.ic_ball,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = ballPainter,
                    error = ballPainter,
                    fallback = ballPainter
                )
                Spacer(modifier = Modifier.width(8.dp))
                val isAwayCurrent = remember(match.awayTeam, currentHomeName) {
                    val name = match.awayTeam?.name ?: ""
                    val id = match.awayTeam?.id ?: ""
                    val currId = homeTeam?.id ?: ""
                    (id.isNotEmpty() && currId.isNotEmpty() && id == currId) ||
                    (name.isNotEmpty() && currentHomeName.isNotEmpty() &&
                     (name.contains(currentHomeName, ignoreCase = true) || currentHomeName.contains(name, ignoreCase = true)))
                }
                Text(
                    text = cleanAwayName,
                    fontSize = 13.sp,
                    fontWeight = if (isAwayCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAwayCurrent) TextDark else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scores Column
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$homeGoals",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "$awayGoals",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        // Right Outcome Indicator Column (relative to the Home Team reference)
        val isHomeCurrent = remember(match.homeTeam, currentHomeName) {
            val name = match.homeTeam?.name ?: ""
            val id = match.homeTeam?.id ?: ""
            val currId = homeTeam?.id ?: ""
            (id.isNotEmpty() && currId.isNotEmpty() && id == currId) ||
            (name.isNotEmpty() && currentHomeName.isNotEmpty() &&
             (name.contains(currentHomeName, ignoreCase = true) || currentHomeName.contains(name, ignoreCase = true)))
        }

        val outcome = remember(homeGoals, awayGoals, isHomeCurrent) {
            when {
                homeGoals == awayGoals -> "D"
                homeGoals > awayGoals -> if (isHomeCurrent) "W" else "L"
                else -> if (isHomeCurrent) "L" else "W"
            }
        }

        val badgeColor = when (outcome) {
            "W" -> Color(0xFF2EA64F) // SofaScore Green
            "L" -> Color(0xFFE53935) // Red
            else -> Color(0xFF9E9E9E) // Grey
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
                .background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = outcome,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ── Common ───────────────────────────────────────────────────────────────────
