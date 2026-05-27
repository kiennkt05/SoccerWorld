package com.example.soccerworld.ui.home.topscorer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.soccerworld.ui.theme.FavoriteGold
import com.example.soccerworld.ui.theme.TextSecondary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

@Composable
fun TopScorersScreen(key: Int = 0) {
    val context = LocalContext.current
    val viewModel: TopScorerViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )

    LaunchedEffect(key) {
        if (key > 0) viewModel.refresh()
    }

    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
        }
    } else if (state.topScorerList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No top scorers data available for this league",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp)
        ) {
            itemsIndexed(
                state.topScorerList,
                key = { _, player -> player.playerId },
                contentType = { _, _ -> "top_scorer_row" }
            ) { index, player ->
                TopScorerRow(
                    rank = index + 1,
                    item = player,
                    playerImageUrl = state.playerImageUrls[player.playerId]
                )
            }
        }
    }
}

@Composable
fun TopScorerRow(rank: Int, item: TopScorerEntity, playerImageUrl: String?) {
    val context = LocalContext.current
    val fallbackPainter = painterResource(id = R.drawable.ic_players)
    
    val imageRequest = remember(playerImageUrl) {
        playerImageUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(false)
                .build()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "$rank",
                fontWeight = FontWeight.Bold,
                color = if (rank == 1) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )

            AsyncImage(
                model = imageRequest,
                contentDescription = item.playerName,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Crop,
                placeholder = fallbackPainter,
                error = fallbackPainter,
                fallback = fallbackPainter
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Name & Team
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.playerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = item.teamName,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Goals
            Text(
                text = "${item.goals} Goals",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
