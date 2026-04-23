package com.example.soccerworld.ui.fixture.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soccerworld.model.h2h.Matche
import com.example.soccerworld.model.matchdetail.MatchLineupTeam
import com.example.soccerworld.model.matchdetail.MatchEvent
import com.example.soccerworld.model.matchdetail.MatchStatItem
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Summary", "Stats", "Lineups", "H2H")

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Error", color = Color.Red)
                }
            } else {
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

@Composable
fun SummaryTab(events: List<MatchEvent>) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No summary events available")
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(events) { event ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${event.minute}' ${event.type}", fontWeight = FontWeight.Bold)
                    Text(event.description)
                    if (!event.team.isNullOrBlank()) {
                        Text(event.team, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsTab(stats: List<MatchStatItem>) {
    if (stats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No stats available")
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(stats) { stat ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stat.name, modifier = Modifier.weight(1f))
                Text("${stat.homeValue} - ${stat.awayValue}", fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun LineupsTab(lineups: List<MatchLineupTeam>) {
    if (lineups.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No lineup data available")
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(lineups) { lineup ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(lineup.teamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Starters", fontWeight = FontWeight.SemiBold)
                    lineup.starters.forEach { p -> Text("- ${p.name} ${p.position ?: ""}") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Substitutes", fontWeight = FontWeight.SemiBold)
                    lineup.substitutes.forEach { p -> Text("- ${p.name} ${p.position ?: ""}") }
                }
            }
        }
    }
}

@Composable
fun H2HTab(h2hList: List<Matche>) {
    if (h2hList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No H2H data available")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(h2hList) { match ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${match.homeTeam?.name} vs ${match.awayTeam?.name}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Score: ${match.score?.fullTime?.home ?: 0} - ${match.score?.fullTime?.away ?: 0}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
