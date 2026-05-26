package com.example.soccerworld.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.data.local.entity.FavoriteTeamEntity
import com.example.soccerworld.data.local.entity.FavoritePlayerEntity
import com.example.soccerworld.ui.fixture.FixtureCard
import com.example.soccerworld.ui.player.PlayerDetailInfo
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.example.soccerworld.ui.theme.BrandNavy
import com.example.soccerworld.ui.theme.TextOnDark
import com.example.soccerworld.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onMatchClick: (String) -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    onPlayerClick: (PlayerDetailInfo) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Favorites",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSystemInDarkTheme()) TextOnDark else BrandNavy
                        )
                        if (!isLoggedIn) {
                            Text(
                                text = "Sign in to follow your favorites",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isLoggedIn) {
                LoginRequiredState(onNavigateToLogin = onNavigateToLogin)
            } else {
                FavoritesContent(
                    onMatchClick = onMatchClick,
                    onTeamClick = onTeamClick,
                    onPlayerClick = onPlayerClick
                )
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    onMatchClick: (String) -> Unit,
    onTeamClick: (String) -> Unit,
    onPlayerClick: (PlayerDetailInfo) -> Unit
) {
    val context = LocalContext.current
    val viewModel: FavoritesViewModel = viewModel(factory = ViewModelFactory(Injection.provideFootballRepository(context)))
    val state by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Matches", "Clubs", "Players")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                when (selectedTabIndex) {
                    0 -> MatchFavoritesTab(
                        matches = state.matches,
                        onMatchClick = onMatchClick,
                        onToggleFavorite = { viewModel.toggleFavoriteMatch(it) }
                    )
                    1 -> ClubFavoritesTab(
                        clubs = state.teams,
                        onTeamClick = onTeamClick,
                        onToggleFavorite = { viewModel.toggleFavoriteTeam(it.teamId, it.name, it.logoUrl) }
                    )
                    2 -> PlayerFavoritesTab(
                        players = state.players,
                        onPlayerClick = onPlayerClick,
                        onToggleFavorite = { viewModel.toggleFavoritePlayer(it.playerId, it.name, it.imageUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchFavoritesTab(
    matches: List<Matche>,
    onMatchClick: (String) -> Unit,
    onToggleFavorite: (Matche) -> Unit
) {
    if (matches.isEmpty()) {
        EmptyState(
            title = "No favorite matches",
            description = "Tap the favorite icon on any match\nto save it here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = matches,
                key = { it.id ?: it.hashCode() }
            ) { match ->
                FixtureCard(
                    match = match,
                    isFavorite = true,
                    onToggleFavorite = { onToggleFavorite(match) },
                    onClick = { onMatchClick(match.id ?: "") }
                )
            }
        }
    }
}

@Composable
private fun ClubFavoritesTab(
    clubs: List<FavoriteTeamEntity>,
    onTeamClick: (String) -> Unit,
    onToggleFavorite: (FavoriteTeamEntity) -> Unit
) {
    if (clubs.isEmpty()) {
        EmptyState(
            title = "No favorite clubs",
            description = "Tap the favorite icon on any club page\nto save it here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = clubs,
                key = { it.teamId }
            ) { team ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTeamClick(team.teamId) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = team.logoUrl,
                                contentDescription = team.name,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = team.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!team.country.isNullOrBlank()) {
                                Text(
                                    text = team.country,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onToggleFavorite(team) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Unfavorite",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerFavoritesTab(
    players: List<FavoritePlayerEntity>,
    onPlayerClick: (PlayerDetailInfo) -> Unit,
    onToggleFavorite: (FavoritePlayerEntity) -> Unit
) {
    if (players.isEmpty()) {
        EmptyState(
            title = "No favorite players",
            description = "Tap the favorite icon on any player page\nto save it here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = players,
                key = { it.playerId }
            ) { player ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val playerInfo = PlayerDetailInfo(
                                id = player.playerId,
                                name = player.name,
                                position = player.position,
                                dateOfBirth = null,
                                nationality = player.nationality,
                                jerseyNumber = null,
                                imageUrl = player.imageUrl,
                                flagId = null,
                                teamId = null
                            )
                            onPlayerClick(playerInfo)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = player.imageUrl,
                                contentDescription = player.name,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val details = listOfNotNull(
                                player.position?.replaceFirstChar { it.uppercase() },
                                player.nationality
                            ).joinToString(" • ")
                            if (details.isNotBlank()) {
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onToggleFavorite(player) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Unfavorite",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    description: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginRequiredState(onNavigateToLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Login required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You need to sign in to save and view\nyour favorites",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
