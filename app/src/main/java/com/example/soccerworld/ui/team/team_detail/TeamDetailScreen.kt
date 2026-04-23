package com.example.soccerworld.ui.team.team_detail

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.ui.team.team_detail.player.PlayerViewModel
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(teamId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val factory = ViewModelFactory(Injection.provideFootballRepository(context))

    val playerViewModel: PlayerViewModel = viewModel(factory = factory)

    LaunchedEffect(teamId) {
        playerViewModel.getAllPlayersOfTeam(teamId)
    }

    val state by playerViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Squad") },
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
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Error loading players", color = Color.Red)
            }
        } else {
            val players = state.playerList.filterNotNull()
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                items(players) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                            Column {
                                Text(text = player.name ?: "Unknown Player", fontWeight = FontWeight.Bold)
                                Text(
                                    text = player.position ?: "Unknown Position",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
