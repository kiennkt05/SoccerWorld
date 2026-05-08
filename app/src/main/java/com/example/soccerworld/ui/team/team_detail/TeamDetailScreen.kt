package com.example.soccerworld.ui.team.team_detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.soccerworld.R
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import com.example.soccerworld.ui.team.team_detail.tabs.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(teamId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val factory = ViewModelFactory(Injection.provideFootballRepository(context))

    val viewModel: TeamDetailViewModel = viewModel(factory = factory)

    LaunchedEffect(teamId) {
        viewModel.initTeam(teamId)
    }

    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Details", "Matches", "Standings", "Squad", "Transfers")

    val fallbackPainter = painterResource(id = R.drawable.ic_ball)
    
    val imageRequest = remember(state.teamCrest) {
        state.teamCrest?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(false)
                .build()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.teamName.ifEmpty { "Team Profile" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Favorite Team */ }) {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Team Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = state.teamName,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = fallbackPainter,
                    error = fallbackPainter,
                    fallback = fallbackPainter
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = state.teamName.ifEmpty { "Loading..." }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "0 Followers", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (state.selectedTab) {
                    0 -> TeamDetailsTab(state.detailsState)
                    1 -> TeamMatchesTab(state.matchesState, onLoadMore = { viewModel.loadMoreMatches() })
                    2 -> TeamStandingsTab(teamId = state.teamId)
                    3 -> TeamSquadTab(state.squadState)
                    4 -> TeamTransfersTab(state.transfersState)
                }
            }
        }
    }
}
