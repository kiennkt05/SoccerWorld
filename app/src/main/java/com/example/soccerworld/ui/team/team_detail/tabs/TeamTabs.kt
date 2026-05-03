package com.example.soccerworld.ui.team.team_detail.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.TransferData
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.ui.team.team_detail.TabState
import com.example.soccerworld.ui.fixture.FixtureCard

@Composable
fun TeamDetailsTab(detailsState: TabState<PlayerResponse>) {
    when (detailsState) {
        is TabState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is TabState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(detailsState.message, color = MaterialTheme.colorScheme.error) }
        is TabState.Success -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text(text = "Coach", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = "Unknown Coach", modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                
                Text(text = "Venue", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = "Unknown Venue", modifier = Modifier.padding(top = 8.dp))
            }
        }
        else -> {}
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
