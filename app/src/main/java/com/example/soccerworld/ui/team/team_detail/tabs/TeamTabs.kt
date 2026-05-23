package com.example.soccerworld.ui.team.team_detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.TransferData
import com.example.soccerworld.data.remote.flashlive.TransferPlayer
import com.example.soccerworld.data.remote.flashlive.TransferTeam
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.leaguetable.Table
import com.example.soccerworld.model.player.Coach
import com.example.soccerworld.model.player.Contract
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.model.player.RunningCompetition
import com.example.soccerworld.ui.fixture.FixtureCard
import com.example.soccerworld.ui.home.leaguetable.LeagueTableViewModel
import com.example.soccerworld.ui.team.team_detail.TabState
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory


@Composable
fun TeamDetailsTab(detailsState: TabState<PlayerResponse>) {
    when (detailsState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(detailsState.message, color = MaterialTheme.colorScheme.error)
        }
        is TabState.Success -> {
            val team = detailsState.data
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // ── Club Info ────────────────────────────────────────
                item {
                    SectionTitle("Club Information")
                }
                item {
                    InfoCard {
                        InfoRow(
                            icon = "👔",
                            label = "Coach",
                            value = team.coach?.name ?: "No info"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                        InfoRow(
                            icon = "🏟️",
                            label = "Stadium",
                            value = team.venue ?: "No info"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                        InfoRow(
                            icon = "📅",
                            label = "Founded year",
                            value = team.founded?.toString() ?: "No info"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                        InfoRow(
                            icon = "🎨",
                            label = "Club colors",
                            value = team.clubColors ?: "No info"
                        )
                        if (!team.address.isNullOrBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                            InfoRow(icon = "📍", label = "Address", value = team.address)
                        }
                        if (!team.website.isNullOrBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                            InfoRow(icon = "🌐", label = "Website", value = team.website)
                        }
                    }
                }

                // ── Competitions ─────────────────────────────────────
                val comps = team.runningCompetitions?.filterNotNull() ?: emptyList()
                if (comps.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionTitle("Current Competitions") }
                    items(comps) { comp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = comp.emblem,
                                    contentDescription = comp.name,
                                    modifier = Modifier.size(36.dp),
                                    placeholder = painterResource(id = R.drawable.ic_ball),
                                    error = painterResource(id = R.drawable.ic_ball),
                                    fallback = painterResource(id = R.drawable.ic_ball)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = comp.name ?: "Unknown",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = comp.type ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Coach detail ─────────────────────────────────────
                val coach = team.coach
                if (coach != null) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionTitle("Coach") }
                    item {
                        InfoCard {
                            if (!coach.nationality.isNullOrBlank()) {
                                InfoRow(icon = "🌍", label = "Nationality", value = coach.nationality)
                                HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                            }
                            if (!coach.dateOfBirth.isNullOrBlank()) {
                                InfoRow(icon = "🎂", label = "Birthdate", value = coach.dateOfBirth.take(10))
                            }
                            val contractEnd = coach.contract?.until
                            if (!contractEnd.isNullOrBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                                InfoRow(icon = "📋", label = "Contract until", value = contractEnd.take(10))
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 18.sp, modifier = Modifier.width(28.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


private fun formatFullMatchDate(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDate)
        val outputFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: utcDate
    } catch (e: Exception) {
        utcDate
    }
}

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
    // Fallback specifically for Arsenal (since that is the main team in the prototype)
    if (cleanName.contains("arsenal")) return true
    return false
}

@Composable
fun MatchRow(
    match: Matche,
    activeFilter: String,
    teamId: String,
    teamName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click if needed */ }
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
            val statusText = if (activeFilter == "Finished") {
                "FT"
            } else {
                remember(match.utcDate) { formatMatchTime(match.utcDate) }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (activeFilter == "Finished") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
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
                val isHomeCurrent = remember(match.homeTeam) {
                    isCurrentTeam(match.homeTeam?.id, match.homeTeam?.name, teamId, teamName)
                }
                Text(
                    text = homeNameCleaned,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isHomeCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHomeCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
                val isAwayCurrent = remember(match.awayTeam) {
                    isCurrentTeam(match.awayTeam?.id, match.awayTeam?.name, teamId, teamName)
                }
                Text(
                    text = awayNameCleaned,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isAwayCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAwayCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scores Column (if finished)
        if (activeFilter == "Finished") {
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
        if (activeFilter == "Finished") {
            val homeScore = match.score?.fullTime?.home ?: 0
            val awayScore = match.score?.fullTime?.away ?: 0
            val homeIsCurrent = remember(match.homeTeam) {
                isCurrentTeam(match.homeTeam?.id, match.homeTeam?.name, teamId, teamName)
            }
            val awayIsCurrent = remember(match.awayTeam) {
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
    }
}

@Composable
fun TeamMatchesTab(
    matchesState: TabState<List<Matche>>,
    teamId: String = "",
    teamName: String = "",
    onLoadMore: () -> Unit
) {
    when (matchesState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(matchesState.message, color = MaterialTheme.colorScheme.error) }
        is TabState.Success -> {
            val listState = rememberLazyListState()
            var activeFilter by remember { mutableStateOf("Finished") }
            
            val finishedMatches = remember(matchesState.data) {
                matchesState.data.filter {
                    it.status == "FINISHED" || it.stage == "FINISHED"
                }.sortedByDescending { it.utcDate.orEmpty() }
            }
            
            val scheduledMatches = remember(matchesState.data) {
                matchesState.data.filter {
                    it.status == "SCHEDULED" || it.stage == "SCHEDULED" || it.status == "TIMED"
                }.sortedBy { it.utcDate.orEmpty() }
            }

            val activeMatches = if (activeFilter == "Finished") finishedMatches else scheduledMatches

            // Group by Competition/Tournament to replicate SofaScore layout
            val groupedMatches = remember(activeMatches) {
                val groups = linkedMapOf<String, MutableList<Matche>>()
                activeMatches.forEach { match ->
                    val compName = match.competition?.name ?: "Tournament"
                    val list = groups.getOrPut(compName) { mutableListOf() }
                    list.add(match)
                }
                groups
            }

            // Check if we need to load more (reached the end)
            val isAtBottom by remember(listState) {
                derivedStateOf {
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
                }
            }
            
            LaunchedEffect(isAtBottom) {
                if (isAtBottom) {
                    onLoadMore()
                }
            }

            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                // Segmented Selector Row (Compact & Premium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf("Finished", "Scheduled")
                    filters.forEach { filterName ->
                        val isSelected = activeFilter == filterName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.85f) else Color.Transparent)
                                .clickable { activeFilter = filterName },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filterName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (groupedMatches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No $activeFilter matches found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedMatches.forEach { (compName, matchesList) ->
                            item(key = compName) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Tournament Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Dynamic Tournament Emblem (No simple emoji fallback unless unavailable)
                                            val emblemUrl = matchesList.firstOrNull()?.competition?.emblem
                                            AsyncImage(
                                                model = emblemUrl ?: R.drawable.ic_ball,
                                                contentDescription = compName,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape),
                                                placeholder = painterResource(id = R.drawable.ic_ball),
                                                error = painterResource(id = R.drawable.ic_ball),
                                                fallback = painterResource(id = R.drawable.ic_ball),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = compName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        // Divider between header and first match row
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )

                                        // Matches List inside tournament card
                                        matchesList.forEachIndexed { index, match ->
                                            if (index > 0) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(horizontal = 14.dp),
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                )
                                            }
                                            MatchRow(
                                                match = match,
                                                activeFilter = activeFilter,
                                                teamId = teamId,
                                                teamName = teamName
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun TeamTransfersTab(transfersState: TabState<List<TransferData>>) {
    when (transfersState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(transfersState.message, color = MaterialTheme.colorScheme.error) }
        is TabState.Success -> {
            val transfers = transfersState.data
            if (transfers.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No transfers found") }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(transfers) { transfer ->
                        TransferCard(transfer = transfer)
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun TeamSquadTab(squadState: TabState<PlayerResponse>) {
    val context = LocalContext.current
    when (squadState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(squadState.message, color = MaterialTheme.colorScheme.error) }
        is TabState.Success -> {
            val players = squadState.data.squad.orEmpty().filterNotNull()
            val groupedPlayers = players.groupBy { it.position ?: "Unknown" }
            
            // Map positions to their standard visual soccer layout ordering: GK -> DF -> MF -> FW -> COACH -> Others
            val positionOrder = mapOf(
                "Goalkeeper" to 1,
                "Defender" to 2,
                "Midfielder" to 3,
                "Forward" to 4,
                "Coach" to 5
            )
            val sortedGroups = groupedPlayers.entries.sortedBy { positionOrder[it.key] ?: 99 }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                sortedGroups.forEach { (position, positionPlayers) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayHeaderName = if (position == "Coach") "Coach" else "${position}s"
                            Text(
                                text = displayHeaderName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Position group player count badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${positionPlayers.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    items(positionPlayers) { player ->
                        val flagId = player.flagId
                        val flagResId = remember(flagId) {
                            if (flagId != null && flagId > 0) {
                                val resId = context.resources.getIdentifier("country_flag_$flagId", "drawable", context.packageName)
                                if (resId != 0) resId else null
                            } else {
                                null
                            }
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Jersey number / Technical Staff badge
                                if (position == "Coach") {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "👔",
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    val jerseyNumber = player.jerseyNumber
                                    val isNumberValid = jerseyNumber != null && jerseyNumber > 0
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = if (isNumberValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isNumberValid) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isNumberValid) "$jerseyNumber" else "—",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNumberValid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Player Avatar Frame
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = player.imageUrl,
                                        contentDescription = player.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        placeholder = painterResource(id = R.drawable.ic_players),
                                        error = painterResource(id = R.drawable.ic_players),
                                        fallback = painterResource(id = R.drawable.ic_players)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Player Name
                                Text(
                                    text = player.name ?: "Unknown Player",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // National Flag
                                if (flagResId != null) {
                                    Image(
                                        painter = painterResource(id = flagResId),
                                        contentDescription = "Nation Flag",
                                        modifier = Modifier
                                            .size(width = 30.dp, height = 20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

// ── P6: Tab Standings ──────────────────────────────────────────────────────────

@Composable
fun TeamStandingsTab(teamId: String) {
    val context = LocalContext.current
    val viewModel: LeagueTableViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()

    val primary = MaterialTheme.colorScheme.primary
    val highlightBg = primary.copy(alpha = 0.12f)

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = primary)
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "Error loading standings",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else -> {
            val list = state.tableList?.filterNotNull() ?: emptyList()
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No standings found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Header row
                    item {
                        StandingsHeaderRow()
                        Spacer(Modifier.height(4.dp))
                    }
                    items(list) { item ->
                        val isHighlighted = item.team?.id == teamId
                        StandingsRow(
                            item = item,
                            isHighlighted = isHighlighted,
                            highlightBg = highlightBg,
                            highlightBorder = primary,
                            primaryBlue = primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsHeaderRow() {
    val primaryBlue = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryBlue.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = primaryBlue,
            modifier = Modifier.width(28.dp)
        )
        Text(
            text = "Club",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = primaryBlue,
            modifier = Modifier.weight(1f)
        )
        listOf("P", "W", "D", "L", "GD", "PTS").forEach { label ->
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = primaryBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp)
            )
        }
    }
}

@Composable
private fun StandingsRow(
    item: Table,
    isHighlighted: Boolean,
    highlightBg: Color,
    highlightBorder: Color,
    primaryBlue: Color
) {
    val position = item.position ?: 0
    // Zone color: top 4 = blue (CL), 5th = orange (EL), bottom 3 = red (relegation)
    val zoneColor = when {
        position <= 4  -> MaterialTheme.colorScheme.primary
        position == 5  -> MaterialTheme.colorScheme.tertiary
        position >= 18 -> MaterialTheme.colorScheme.error
        else           -> Color.Transparent
    }

    val rowModifier = if (isHighlighted) {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(highlightBg, RoundedCornerShape(10.dp))
            .border(1.5.dp, highlightBorder, RoundedCornerShape(10.dp))
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    }

    Row(
        modifier = rowModifier.padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position with zone indicator
        Box(modifier = Modifier.width(28.dp)) {
            if (zoneColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(zoneColor)
                        .align(Alignment.CenterStart)
                )
            }
            Text(
                text = "$position",
                fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (isHighlighted) primaryBlue else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Crest + name
        AsyncImage(
            model = item.team?.crest,
            contentDescription = item.team?.name,
            modifier = Modifier.size(22.dp),
            placeholder = painterResource(R.drawable.ic_ball),
            error = painterResource(R.drawable.ic_ball),
            fallback = painterResource(R.drawable.ic_ball)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = item.team?.shortName ?: item.team?.name ?: "?",
            fontSize = 13.sp,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isHighlighted) primaryBlue else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        // Stats columns: GP, W, D, L, GD, Pts
        val stats = listOf(
            "${item.playedGames ?: 0}",
            "${item.won ?: 0}",
            "${item.draw ?: 0}",
            "${item.lost ?: 0}",
            if ((item.goalDifference ?: 0) >= 0) "+${item.goalDifference ?: 0}" else (item.goalDifference ?: 0).toString(),
            "${item.points ?: 0}"
        )
        stats.forEachIndexed { index, value ->
            val isPoints = index == stats.lastIndex
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = if (isPoints || isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isPoints && isHighlighted -> primaryBlue
                    isPoints -> primaryBlue
                    isHighlighted -> primaryBlue
                    else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp)
            )
        }
    }

    // Divider (skip if highlighted)
    if (!isHighlighted) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeamTransfersTabPreview() {
    val sampleTransfers = listOf(
        TransferData(
            transferDate = 1738454400L,
            transferTypeStr = "Transfer",
            transferDirection = "in",
            fromTeam = TransferTeam(image = null, value = "Old Club"),
            toTeam = TransferTeam(image = null, value = "Current Club"),
            player = TransferPlayer(participantId = "1", value = "John Doe", image = "flag-1", countryName = "England")
        ),
        TransferData(
            transferDate = 1738368000L,
            transferTypeStr = "Free",
            transferDirection = "out",
            fromTeam = TransferTeam(image = null, value = "Current Club"),
            toTeam = TransferTeam(image = null, value = "New Club"),
            player = TransferPlayer(participantId = "2", value = "Jane Smith", image = "flag-2", countryName = "France")
        )
    )
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TeamTransfersTab(transfersState = TabState.Success(sampleTransfers))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamDetailsTabPreview() {
    val sampleTeam = PlayerResponse(
        name = "Manchester City FC",
        venue = "Etihad Stadium",
        founded = 1880,
        clubColors = "Sky Blue / White",
        address = "Etihad Campus Manchester M11 3FF",
        website = "https://www.mancity.com",
        coach = Coach(
            name = "Pep Guardiola",
            nationality = "Spain",
            dateOfBirth = "1971-01-18",
            contract = Contract(until = "2025-06-30")
        ),
        runningCompetitions = listOf(
            RunningCompetition(name = "Premier League", type = "LEAGUE", emblem = null),
            RunningCompetition(name = "UEFA Champions League", type = "CUP", emblem = null)
        )
    )
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TeamDetailsTab(detailsState = TabState.Success(sampleTeam))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeamMatchesTabPreview() {
    val sampleMatches = listOf(
        Matche(
            id = "1",
            utcDate = "2026-05-24T20:00:00Z",
            status = "FINISHED",
            stage = "FINISHED",
            group = "Round 38",
            competition = com.example.soccerworld.model.fixture.Competition(name = "England: Premier League"),
            homeTeam = com.example.soccerworld.model.fixture.HomeTeam(name = "Crystal Palace", shortName = "CRY"),
            awayTeam = com.example.soccerworld.model.fixture.AwayTeam(name = "Arsenal", shortName = "ARS"),
            score = com.example.soccerworld.model.fixture.Score(
                fullTime = com.example.soccerworld.model.fixture.FullTime(home = 1, away = 3)
            )
        ),
        Matche(
            id = "2",
            utcDate = "2026-05-28T21:00:00Z",
            status = "SCHEDULED",
            stage = "SCHEDULED",
            group = "Final",
            competition = com.example.soccerworld.model.fixture.Competition(name = "Europe: Champions League"),
            homeTeam = com.example.soccerworld.model.fixture.HomeTeam(name = "PSG", shortName = "PSG"),
            awayTeam = com.example.soccerworld.model.fixture.AwayTeam(name = "Arsenal", shortName = "ARS"),
            score = com.example.soccerworld.model.fixture.Score()
        )
    )
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TeamMatchesTab(
                matchesState = TabState.Success(sampleMatches),
                onLoadMore = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SectionTitlePreview() {
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SectionTitle(title = "Section Title")
        }
    }
}
