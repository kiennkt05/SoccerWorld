package com.example.soccerworld.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.model.fixture.*
import com.example.soccerworld.ui.theme.FavoriteGold
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun formatMatchTime(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDate)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: utcDate
    } catch (e: Exception) {
        utcDate
    }
}

private fun String.cleanTeamName(): String {
    return this.replace("*", "").trim()
}

private fun formatSofaDate(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDate)
        val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: utcDate
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(utcDate)
            val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: utcDate
        } catch (ex: Exception) {
            utcDate
        }
    }
}

private fun isCurrentTeam(id: String?, name: String?, currentId: String, currentName: String): Boolean {
    if (id != null && currentId.isNotEmpty() && id == currentId) return true
    val cleanName = (name ?: "").replace("*", "").trim().lowercase()
    val cleanCurrent = currentName.replace("*", "").trim().lowercase()
    if (cleanCurrent.isNotEmpty() && cleanName.contains(cleanCurrent)) return true
    if (cleanName.contains("arsenal")) return true
    return false
}

@Composable
fun MatchRow(
    match: Matche,
    activeFilter: String,
    teamId: String = "",
    teamName: String = "",
    isFavorite: Boolean = false,
    onClick: (String) -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { match.id?.let { onClick(it) } }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Date & Status
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val sofaDate = remember(match.utcDate) { formatSofaDate(match.utcDate) }
            Text(
                text = sofaDate,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            val statusText = when (activeFilter) {
                "Finished" -> "FT"
                "Live" -> if (match.status == "PAUSED") "HT" else "Live"
                else -> remember(match.utcDate) { formatMatchTime(match.utcDate) }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when (activeFilter) {
                    "Live" -> Color(0xFFE53935)
                    "Finished" -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }

        // Vertical Line Divider
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(0.7.dp)
                .height(38.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                val homeCrest = match.homeTeam?.crest
                AsyncImage(
                    model = homeCrest ?: R.drawable.ic_ball,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball),
                    fallback = painterResource(id = R.drawable.ic_ball)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val homeNameCleaned = (match.homeTeam?.name ?: "TBD").cleanTeamName()
                val isHomeCurrent = remember(match.homeTeam, teamId, teamName) {
                    isCurrentTeam(match.homeTeam?.id, match.homeTeam?.name, teamId, teamName)
                }
                Text(
                    text = homeNameCleaned,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isHomeCurrent && (teamId.isNotEmpty() || teamName.isNotEmpty())) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHomeCurrent && (teamId.isNotEmpty() || teamName.isNotEmpty())) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
                val awayCrest = match.awayTeam?.crest
                AsyncImage(
                    model = awayCrest ?: R.drawable.ic_ball,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball),
                    fallback = painterResource(id = R.drawable.ic_ball)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val awayNameCleaned = (match.awayTeam?.name ?: "TBD").cleanTeamName()
                val isAwayCurrent = remember(match.awayTeam, teamId, teamName) {
                    isCurrentTeam(match.awayTeam?.id, match.awayTeam?.name, teamId, teamName)
                }
                Text(
                    text = awayNameCleaned,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isAwayCurrent && (teamId.isNotEmpty() || teamName.isNotEmpty())) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAwayCurrent && (teamId.isNotEmpty() || teamName.isNotEmpty())) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scores Column (if finished or live)
        if (activeFilter == "Finished" || activeFilter == "Live") {
            val homeScore = match.score?.fullTime?.home
            val awayScore = match.score?.fullTime?.away
            
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = homeScore?.toString() ?: "0",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = awayScore?.toString() ?: "0",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        // Right Outcome Indicator Column (if finished)
        if (activeFilter == "Finished" && (teamId.isNotEmpty() || teamName.isNotEmpty())) {
            val homeScore = match.score?.fullTime?.home ?: 0
            val awayScore = match.score?.fullTime?.away ?: 0
            val homeIsCurrent = remember(match.homeTeam, teamId, teamName) {
                isCurrentTeam(match.homeTeam?.id, match.homeTeam?.name, teamId, teamName)
            }
            val awayIsCurrent = remember(match.awayTeam, teamId, teamName) {
                isCurrentTeam(match.awayTeam?.id, match.awayTeam?.name, teamId, teamName)
            }
            val outcome = remember(homeScore, awayScore, homeIsCurrent, awayIsCurrent) {
                when {
                    homeScore == awayScore -> "D"
                    homeScore > awayScore -> if (homeIsCurrent) "W" else if (awayIsCurrent) "L" else "D"
                    else -> if (awayIsCurrent) "W" else if (homeIsCurrent) "L" else "D"
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

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Toggle favorite",
                tint = if (isFavorite) FavoriteGold else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchRowFinishedPreview() {
    val sampleMatch = Matche(
        id = "1",
        utcDate = "2023-10-27T18:30:00Z",
        status = "FINISHED",
        homeTeam = HomeTeam(id = "1", name = "Arsenal FC", crest = "https://crests.football-data.org/57.png"),
        awayTeam = AwayTeam(id = "2", name = "Chelsea FC", crest = "https://crests.football-data.org/61.png"),
        score = Score(fullTime = FullTime(home = 2, away = 1))
    )
    SoccerWorldTheme {
        MatchRow(
            match = sampleMatch,
            activeFilter = "Finished",
            isFavorite = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchRowLivePreview() {
    val sampleMatch = Matche(
        id = "2",
        utcDate = "2023-10-27T20:00:00Z",
        status = "IN_PLAY",
        homeTeam = HomeTeam(id = "3", name = "Liverpool FC", crest = "https://crests.football-data.org/64.png"),
        awayTeam = AwayTeam(id = "4", name = "Manchester City FC", crest = "https://crests.football-data.org/65.png"),
        score = Score(fullTime = FullTime(home = 1, away = 1))
    )
    SoccerWorldTheme {
        MatchRow(
            match = sampleMatch,
            activeFilter = "Live"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MatchRowScheduledPreview() {
    val sampleMatch = Matche(
        id = "3",
        utcDate = "2023-10-28T15:00:00Z",
        status = "TIMED",
        homeTeam = HomeTeam(id = "5", name = "Manchester United FC", crest = "https://crests.football-data.org/66.png"),
        awayTeam = AwayTeam(id = "6", name = "Tottenham Hotspur FC", crest = "https://crests.football-data.org/73.png"),
        score = Score(fullTime = FullTime(home = null, away = null))
    )
    SoccerWorldTheme {
        MatchRow(
            match = sampleMatch,
            activeFilter = "All"
        )
    }
}
