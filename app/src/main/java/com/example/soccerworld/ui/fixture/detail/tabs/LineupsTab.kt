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
import androidx.compose.foundation.lazy.itemsIndexed
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
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
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
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
                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
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
        }
        val subs = activeTeam?.substitutes ?: emptyList()
        if (subs.isNotEmpty()) {
            itemsIndexed(
                items = subs,
                key = { index, sub -> "sub_${sub.name}_$index" },
                contentType = { _, _ -> "substitute_row" }
            ) { index, sub ->
                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                ) {
                    DetailedSubstituteRow(sub, events)
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp, end = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        thickness = 0.5.dp
                    )
                }
            }
        } else {
            item {
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

