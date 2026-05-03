package com.example.soccerworld.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.PlayerSearchItemDto
import com.example.soccerworld.data.remote.flashlive.TeamSearchItemDto
import com.example.soccerworld.data.remote.flashlive.TournamentSearchItemDto
import com.example.soccerworld.data.remote.flashlive.UnknownSearchItemDto
import com.example.soccerworld.data.remote.flashlive.SearchItemDto
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onTeamClick: (String) -> Unit = {},
    onPlayerClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = state.query,
            onQueryChange = viewModel::onSearchQueryChanged,
            onSearch = { },
            active = true,
            onActiveChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search teams, players, leagues...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") }
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (state.results.isEmpty() && state.query.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.results) { item ->
                        SearchItemCard(item, onTeamClick, onPlayerClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItemCard(
    item: SearchItemDto,
    onTeamClick: (String) -> Unit,
    onPlayerClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                when (item) {
                    is TeamSearchItemDto -> onTeamClick(item.id)
                    is PlayerSearchItemDto -> onPlayerClick(item.id)
                    else -> {} // Handle tournament click if needed
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = when (item) {
                is TeamSearchItemDto -> item.image
                is PlayerSearchItemDto -> item.image
                else -> null
            }
            
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                placeholder = painterResource(id = R.drawable.ic_ball),
                error = painterResource(id = R.drawable.ic_ball),
                fallback = painterResource(id = R.drawable.ic_ball)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = when (item) {
                        is TeamSearchItemDto -> item.name
                        is PlayerSearchItemDto -> item.name
                        is TournamentSearchItemDto -> item.name
                        is UnknownSearchItemDto -> "Unknown"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val subtitle = when (item) {
                    is TeamSearchItemDto -> "Team • ${item.countryName ?: "Unknown"}"
                    is PlayerSearchItemDto -> "Player • ${item.countryName ?: "Unknown"}"
                    is TournamentSearchItemDto -> "Tournament • ${item.countryName ?: "Unknown"}"
                    is UnknownSearchItemDto -> "Unknown"
                }
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
