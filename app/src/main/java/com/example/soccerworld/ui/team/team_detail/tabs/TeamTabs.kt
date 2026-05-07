package com.example.soccerworld.ui.team.team_detail.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.TransferData
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.leaguetable.Table
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.ui.home.leaguetable.LeagueTableViewModel
import com.example.soccerworld.ui.team.team_detail.TabState
import com.example.soccerworld.ui.fixture.FixtureCard
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext


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
                    SectionTitle("Thông Tin Câu Lạc Bộ")
                }
                item {
                    InfoCard {
                        InfoRow(
                            icon = "👔",
                            label = "Huấn luyện viên",
                            value = team.coach?.name ?: "Chưa có thông tin"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                        InfoRow(
                            icon = "🏟️",
                            label = "Sân vận động",
                            value = team.venue ?: "Chưa có thông tin"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                        InfoRow(
                            icon = "📅",
                            label = "Năm thành lập",
                            value = team.founded?.toString() ?: "Chưa có thông tin"
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                        InfoRow(
                            icon = "🎨",
                            label = "Màu áo",
                            value = team.clubColors ?: "Chưa có thông tin"
                        )
                        if (!team.address.isNullOrBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                            InfoRow(icon = "📍", label = "Địa chỉ", value = team.address)
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
                    item { SectionTitle("Đang Tham Gia") }
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
                                        color = Color.Gray
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
                    item { SectionTitle("Huấn Luyện Viên") }
                    item {
                        InfoCard {
                            if (!coach.nationality.isNullOrBlank()) {
                                InfoRow(icon = "🌍", label = "Quốc tịch", value = coach.nationality)
                                HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                            }
                            if (!coach.dateOfBirth.isNullOrBlank()) {
                                InfoRow(icon = "🎂", label = "Ngày sinh", value = coach.dateOfBirth.take(10))
                            }
                            val contractEnd = coach.contract?.until
                            if (!contractEnd.isNullOrBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(start = 44.dp))
                                InfoRow(icon = "📋", label = "Hợp đồng đến", value = contractEnd.take(10))
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


@Composable
fun TeamMatchesTab(matchesState: TabState<List<Matche>>, onLoadMore: () -> Unit) {
    when (matchesState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(matchesState.message, color = MaterialTheme.colorScheme.error) }
        is TabState.Success -> {
            val listState = rememberLazyListState()
            
            // Check if we need to load more (reached the end)
            val isAtBottom by derivedStateOf {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
            }
            
            LaunchedEffect(isAtBottom) {
                if (isAtBottom) {
                    onLoadMore()
                }
            }

            LazyColumn(state = listState, contentPadding = PaddingValues(16.dp)) {
                items(matchesState.data) { match ->
                    FixtureCard(
                        match = match,
                        isFavorite = false,
                        onToggleFavorite = { },
                        onClick = { }
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(text = transfer.playerName ?: "Unknown", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.weight(1f)) {
                                        Text("From", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(transfer.fromTeamName ?: "-", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                        Text("To", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(transfer.toTeamName ?: "-", style = MaterialTheme.typography.bodyMedium)
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
fun TeamSquadTab(squadState: TabState<PlayerResponse>) {
    when (squadState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(squadState.message, color = MaterialTheme.colorScheme.error) }
        is TabState.Success -> {
            val players = squadState.data.squad.orEmpty().filterNotNull()
            val groupedPlayers = players.groupBy { it.position ?: "Unknown" }
            
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                groupedPlayers.forEach { (position, positionPlayers) ->
                    item {
                        Text(
                            text = position,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(positionPlayers) { player ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = player.imageUrl,
                                    contentDescription = player.name,
                                    modifier = Modifier.size(36.dp),
                                    placeholder = painterResource(id = R.drawable.ic_players),
                                    error = painterResource(id = R.drawable.ic_players),
                                    fallback = painterResource(id = R.drawable.ic_players)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = player.name ?: "Unknown Player", fontWeight = FontWeight.Bold)
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

    val primaryBlue = Color(0xFF1565C0)
    val highlightBg  = Color(0xFF1E88E5).copy(alpha = 0.12f)
    val highlightBorder = Color(0xFF1E88E5)

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = primaryBlue)
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error ?: "Lỗi tải bảng xếp hạng",
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
                    Text("Chưa có bảng xếp hạng", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            highlightBorder = highlightBorder,
                            primaryBlue = primaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsHeaderRow() {
    val primaryBlue = Color(0xFF1565C0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1565C0).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
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
            text = "CLB",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = primaryBlue,
            modifier = Modifier.weight(1f)
        )
        listOf("Trận", "T", "H", "B", "HS", "Điểm").forEach { label ->
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
        position <= 4  -> Color(0xFF1E88E5)
        position == 5  -> Color(0xFFFF6D00)
        position >= 18 -> Color(0xFFF44336)
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
            "${if ((item.goalDifference ?: 0) >= 0) "+${item.goalDifference ?: 0}" else "${item.goalDifference ?: 0}"}",
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
                    isPoints -> Color(0xFF1565C0)
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
