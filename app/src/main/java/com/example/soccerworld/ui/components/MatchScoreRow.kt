package com.example.soccerworld.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soccerworld.ui.theme.LocalSoccerColors
import com.example.soccerworld.ui.theme.WinGreen
import com.example.soccerworld.ui.theme.LiveRed
import com.example.soccerworld.ui.theme.DrawAmber

@Immutable
data class MatchDisplayModel(
    val id: String,
    val timeText: String,
    val statusText: String,
    val isLive: Boolean,
    val homeTeamName: String,
    val homeTeamCrest: String?,
    val awayTeamName: String,
    val awayTeamCrest: String?,
    val homeScore: Int?,
    val awayScore: Int?,
    val isFinishedOrLive: Boolean
) {
    val homeWins: Boolean get() = isFinishedOrLive && !isLive && (homeScore ?: 0) > (awayScore ?: 0)
    val awayWins: Boolean get() = isFinishedOrLive && !isLive && (awayScore ?: 0) > (homeScore ?: 0)
}

@Composable
fun MatchScoreRow(
    match: MatchDisplayModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionIcon: (@Composable () -> Unit)? = null
) {
    val soccerColors = LocalSoccerColors.current
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time / Status column
            Column(
                modifier = Modifier.width(44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (match.isLive) {
                    Text(text = match.timeText, style = MaterialTheme.typography.labelSmall, color = soccerColors.liveRed, fontWeight = FontWeight.Bold)
                    Text(text = match.statusText, fontSize = 10.sp, color = soccerColors.liveRed, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = match.timeText, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(text = match.statusText, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
            }

            Box(modifier = Modifier.width(0.5.dp).height(36.dp).background(colorScheme.outlineVariant))
            Spacer(modifier = Modifier.width(10.dp))

            // Teams
            Column(modifier = Modifier.weight(1f)) {
                // Home
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    TeamCrestImage(model = match.homeTeamCrest, size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (match.homeWins) FontWeight.Bold else FontWeight.Normal,
                        color = if (match.awayWins) soccerColors.loserText else colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (match.isFinishedOrLive) {
                        val isDraw = !match.isLive && (match.homeScore ?: 0) == (match.awayScore ?: 0)
                        val homeScoreColor = when {
                            match.isLive -> soccerColors.liveRed
                            match.homeWins -> WinGreen
                            isDraw -> DrawAmber
                            else -> soccerColors.loserText
                        }
                        Text(
                            text = "${match.homeScore ?: 0}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (match.homeWins) FontWeight.Bold else FontWeight.Normal,
                            color = homeScoreColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Away
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    TeamCrestImage(model = match.awayTeamCrest, size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (match.awayWins) FontWeight.Bold else FontWeight.Normal,
                        color = if (match.homeWins) soccerColors.loserText else colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (match.isFinishedOrLive) {
                        val isDraw = !match.isLive && (match.homeScore ?: 0) == (match.awayScore ?: 0)
                        val awayScoreColor = when {
                            match.isLive -> soccerColors.liveRed
                            match.awayWins -> WinGreen
                            isDraw -> DrawAmber
                            else -> soccerColors.loserText
                        }
                        Text(
                            text = "${match.awayScore ?: 0}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (match.awayWins) FontWeight.Bold else FontWeight.Normal,
                            color = awayScoreColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                }
            }
            
            if (actionIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                actionIcon()
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)
    }
}
