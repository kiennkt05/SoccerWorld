package com.example.soccerworld.ui.home.topscorer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

@Composable
fun TopScorersScreen() {
    val context = LocalContext.current
    val viewModel: TopScorerViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )

    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = state.error ?: "Unknown error", color = Color.Red)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(state.topScorerList) { index, player ->
                TopScorerRow(rank = index + 1, item = player)
            }
        }
    }
}

@Composable
fun TopScorerRow(rank: Int, item: TopScorerEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                color = if (rank <= 3) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.width(32.dp)
            )

            // Name & Team
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.playerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = item.teamName,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Goals
            Text(
                text = "${item.goals} Goals",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
