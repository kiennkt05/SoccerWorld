package com.example.soccerworld.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soccerworld.ui.fixture.FixtureCard
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

@Composable
fun FavoritesScreen(onMatchClick: (Int) -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: FavoritesViewModel = viewModel(factory = ViewModelFactory(Injection.provideFootballRepository(context)))
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.matches.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorite matches yet", style = MaterialTheme.typography.bodyLarge)
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(state.matches) { match ->
                    FixtureCard(
                        match = match,
                        isFavorite = true,
                        onToggleFavorite = {},
                        onClick = { onMatchClick(match.id ?: 0) }
                    )
                }
            }
        }
    }
}
