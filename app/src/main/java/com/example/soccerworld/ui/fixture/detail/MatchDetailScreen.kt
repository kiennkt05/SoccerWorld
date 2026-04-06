package com.example.soccerworld.ui.fixture.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soccerworld.ui.fixture.detail.h2h.H2HViewModel
import com.example.soccerworld.ui.fixture.detail.statistic.StatisticViewModel
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(fixtureId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val factory = ViewModelFactory(Injection.provideFootballRepository(context))

    val statViewModel: StatisticViewModel = viewModel(factory = factory)
    val h2hViewModel: H2HViewModel = viewModel(factory = factory)

    LaunchedEffect(fixtureId) {
        statViewModel.getFixtureStatistics(fixtureId)
        h2hViewModel.getHeadToHead(fixtureId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        val tabs = listOf("Tổng quan", "Đối đầu (H2H)")

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

            when (selectedTabIndex) {
                0 -> MatchStatTab(statViewModel)
                1 -> MatchH2HTab(h2hViewModel)
            }
        }
    }
}

@Composable
fun MatchStatTab(viewModel: StatisticViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error ?: "Error", color = Color.Red)
        }
    } else {
        val stat = state.statistics
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tỷ số: ${stat?.score?.fullTime?.home ?: 0} - ${stat?.score?.fullTime?.away ?: 0}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${stat?.homeTeam?.name ?: "Home"} vs ${stat?.awayTeam?.name ?: "Away"}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Trạng thái: ${stat?.status}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Giải đấu: ${stat?.competition?.name}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Matchday: ${stat?.matchday}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun MatchH2HTab(viewModel: H2HViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error ?: "Error", color = Color.Red)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.h2hList) { match ->
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
}
