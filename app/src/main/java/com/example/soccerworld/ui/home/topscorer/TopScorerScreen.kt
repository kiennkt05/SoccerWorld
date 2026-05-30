package com.example.soccerworld.ui.home.topscorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.soccerworld.R
import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.ui.theme.SoccerWorldTheme
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

    TopScorersContent(state = state)
}

@Composable
fun TopScorersContent(state: TopScorerUiState) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
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
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            val rankBgColor = when (rank) {
                1 -> FavoriteGold
                2 -> Color(0xFFB8B8B8) // Silver
                3 -> Color(0xFFCD7F32) // Bronze
                else -> Color.Transparent
            }
            val rankTextColor = when (rank) {
                1, 2, 3 -> Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = rankTextColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))

            // Player Avatar (Circular Box)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = item.playerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = fallbackPainter,
                    error = fallbackPainter,
                    fallback = fallbackPainter
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))

            // Name & Team with spacing
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.playerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.teamName,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Goals Stack (Prominent easy-to-scan design)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.goals}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "goals",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(showBackground = true)
@Composable
fun TopScorersScreenPreview() {
    val sampleTopScorers = listOf(
        TopScorerEntity(playerId = "1", playerName = "Erling Haaland", teamName = "Manchester City", goals = 25),
        TopScorerEntity(playerId = "2", playerName = "Mohamed Salah", teamName = "Liverpool", goals = 18),
        TopScorerEntity(playerId = "3", playerName = "Ollie Watkins", teamName = "Aston Villa", goals = 16),
        TopScorerEntity(playerId = "4", playerName = "Son Heung-min", teamName = "Tottenham", goals = 15),
        TopScorerEntity(playerId = "5", playerName = "Jarrod Bowen", teamName = "West Ham", goals = 14)
    )
    
    val state = TopScorerUiState(
        isLoading = false,
        topScorerList = sampleTopScorers,
        playerImageUrls = emptyMap(),
        error = null
    )
    
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TopScorersContent(state = state)
        }
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun TopScorersScreenLoadingPreview() {
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TopScorersContent(state = TopScorerUiState(isLoading = true))
        }
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
fun TopScorersScreenEmptyPreview() {
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TopScorersContent(state = TopScorerUiState(isLoading = false, topScorerList = emptyList()))
        }
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun TopScorersScreenErrorPreview() {
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TopScorersContent(state = TopScorerUiState(isLoading = false, error = "Failed to load data"))
        }
    }
}
