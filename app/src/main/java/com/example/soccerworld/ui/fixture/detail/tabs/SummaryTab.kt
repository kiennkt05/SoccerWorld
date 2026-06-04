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
import com.example.soccerworld.ui.theme.LocalSoccerColors
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import com.example.soccerworld.model.statistic.Score as StatScore
import com.example.soccerworld.model.statistic.FullTime as StatFullTime

@Composable
fun SummaryTab(events: List<MatchEvent>, highlights: List<MatchHighlight>) {
    if (events.isEmpty()) {
        EmptyState(message = "Không có sự kiện trận đấu")
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
        items(
            items = events,
            key = { event -> "${event.minute}_${event.type}_${event.description.hashCode()}" },
            contentType = { if (it.type == "STAGE_HEADER") "header" else "event" }
        ) { event ->
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
        upperType.contains("PENALTY_SCORED") || (upperType.contains("PENALTY") && !upperType.contains("MISSED")) -> {
            Icon(
                painter = painterResource(id = R.drawable.penalty),
                contentDescription = "Penalty Goal",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
        }
        upperType.contains("PENALTY_MISSED") -> {
            Icon(
                painter = painterResource(id = R.drawable.penalty),
                contentDescription = "Penalty Missed",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
        }
        upperType.contains("GOAL") -> {
            Text(text = "⚽", fontSize = 13.sp)
        }
        upperType.contains("YELLOW_CARD") -> {
            Box(
                modifier = Modifier
                    .size(width = 9.dp, height = 13.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(LocalSoccerColors.current.yellowCard)
            )
        }
        upperType.contains("RED_CARD") || upperType.contains("YELLOW_RED_CARD") -> {
            Box(
                modifier = Modifier
                    .size(width = 9.dp, height = 13.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(LocalSoccerColors.current.redCard)
            )
        }
        upperType.contains("SUBSTITUTION") -> {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Substitution",
                tint = LocalSoccerColors.current.subIn,
                modifier = Modifier.size(16.dp)
            )
        }
        upperType.contains("VAR") -> {
            Surface(
                color = LocalSoccerColors.current.assistText,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "VAR",
                    color = MaterialTheme.colorScheme.onPrimary,
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
        upperType.contains("GOAL") || upperType.contains("PENALTY") -> {
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



fun isHighBetter(statName: String): Boolean {
    val lowIsBetterStats = listOf(
        "Fouls", "Errors leading to shot", "Errors leading to goal",
        "Offsides", "Yellow cards", "Red cards"
    )
    return !lowIsBetterStats.any { statName.contains(it, ignoreCase = true) }
}

