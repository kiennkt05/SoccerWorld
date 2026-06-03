package com.example.soccerworld.ui.favorites

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.soccerworld.ui.theme.AccentNeonOrange
import com.example.soccerworld.ui.theme.BrandGreenMedium
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.soccerworld.R
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.data.local.entity.FavoriteTeamEntity
import com.example.soccerworld.data.local.entity.FavoritePlayerEntity
import androidx.compose.ui.layout.ContentScale
import com.example.soccerworld.data.remote.flashlive.TeamSearchItemDto
import com.example.soccerworld.data.remote.flashlive.SearchItemDto
import com.example.soccerworld.model.fixture.AwayTeam
import com.example.soccerworld.model.fixture.FullTime
import com.example.soccerworld.model.fixture.HomeTeam
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.fixture.Score
import com.example.soccerworld.ui.components.MatchRow
import com.example.soccerworld.ui.fixture.FixtureCard
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onMatchClick: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    isLoggedIn: Boolean = if (LocalInspectionMode.current) false else FirebaseAuth.getInstance().currentUser != null
) {
    val context = LocalContext.current
    val viewModel: FavoritesViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()

    FavoritesScreenContent(
        state = state,
        isLoggedIn = isLoggedIn,
        onMatchClick = onMatchClick,
        onNavigateToLogin = onNavigateToLogin,
        onTeamClick = onTeamClick,
        onToggleFavoriteMatch = { match -> viewModel.toggleFavoriteMatch(match) },
        onToggleFavoriteTeam = { team ->
            viewModel.toggleFavoriteTeam(
                teamId = team.teamId,
                name = team.name,
                logoUrl = team.logoUrl,
                countryName = team.countryName
            )
        },
        onToggleFavoritePlayer = { player ->
            viewModel.toggleFavoritePlayer(
                playerId = player.playerId,
                name = player.name,
                imageUrl = player.imageUrl,
                nationality = player.nationality,
                position = player.position
            )
        },
        onSearchToggleFavoriteTeam = { id, name, logo, country ->
            viewModel.toggleFavoriteTeam(id, name, logo, country)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    state: FavoritesUiState,
    isLoggedIn: Boolean,
    onMatchClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onTeamClick: (String) -> Unit,
    onToggleFavoriteMatch: (Matche) -> Unit,
    onToggleFavoriteTeam: (FavoriteTeamEntity) -> Unit,
    onToggleFavoritePlayer: (FavoritePlayerEntity) -> Unit,
    onSearchToggleFavoriteTeam: (String, String, String?, String?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTeamSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Header Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.fav_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                }

                // Sub-tabs (Events, Teams, Players)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    val tabs = listOf(
                        stringResource(R.string.fav_tab_events),
                        stringResource(R.string.fav_tab_teams),
                        stringResource(R.string.fav_tab_players)
                    )
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isLoggedIn && selectedTab == 0) {
                LoginRequiredState(onNavigateToLogin = onNavigateToLogin)
            } else {
                when (selectedTab) {
                    0 -> EventsTabContent(
                        state = state,
                        onMatchClick = onMatchClick,
                        onToggleFavoriteMatch = onToggleFavoriteMatch
                    )
                    1 -> TeamsTabContent(
                        state = state,
                        onAddClick = { showAddTeamSheet = true },
                        onTeamClick = onTeamClick,
                        onToggleFavorite = onToggleFavoriteTeam
                    )
                    2 -> PlayersTabContent(
                        state = state,
                        onToggleFavorite = onToggleFavoritePlayer
                    )
                }
            }

            if (!isLoggedIn && selectedTab == 1) {
                GuestSyncBanner(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onLoginClick = onNavigateToLogin
                )
            }
        }
    }

    if (showAddTeamSheet) {
        AddTeamBottomSheet(
            onDismiss = { showAddTeamSheet = false },
            favoriteTeams = state.teams,
            onToggleFavorite = onSearchToggleFavoriteTeam
        )
    }
}

@Composable
private fun GuestSyncBanner(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.fav_sync_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.fav_sync_desc),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.fav_signin), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EventsTabContent(
    state: FavoritesUiState,
    onMatchClick: (String) -> Unit,
    onToggleFavoriteMatch: (Matche) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (state.matches.isEmpty()) {
        FavoritesEmptyState(
            message = stringResource(R.string.fav_empty_events),
            hint = stringResource(R.string.fav_empty_events_hint)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                items = state.matches,
                key = { it.id ?: it.hashCode() },
                contentType = { "favorite_match" }
            ) { match ->
                FixtureCard(
                    match = match,
                    isFavorite = true,
                    onToggleFavorite = { onToggleFavoriteMatch(match) },
                    onClick = { onMatchClick(match.id ?: "") },
                    showDateForFinished = true
                )
            }
        }
    }
}

@Composable
private fun TeamsTabContent(
    state: FavoritesUiState,
    onAddClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    onToggleFavorite: (FavoriteTeamEntity) -> Unit
) {
    val trendingTeams = listOf(
        Triple("ppjDR086", "Manchester United", "https://crests.football-data.org/66.png"),
        Triple("W8mj7MDD", "Real Madrid", "https://crests.football-data.org/86.png"),
        Triple("SKbpVP5K", "FC Barcelona", "https://crests.football-data.org/81.png"),
        Triple("CjhkPw0k", "Paris Saint-Germain", "https://crests.football-data.org/524.png"),
        Triple("hA1Zm19f", "Arsenal", "https://crests.football-data.org/57.png"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.fav_my_teams),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.fav_teams_count, state.teams.size),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable { onAddClick() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.fav_add_team), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        repeat(2) { index ->
                            if (index < state.teams.size) {
                                val team = state.teams[index]
                                Box(modifier = Modifier.weight(1f)) {
                                    FavoritedTeamGridItem(team = team, onTeamClick = onTeamClick, onToggle = onToggleFavorite)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (state.teams.size > 2) {
                        val remainingTeams = state.teams.drop(2)
                        val chunks = remainingTeams.chunked(3)
                        chunks.forEach { chunk ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                repeat(3) { index ->
                                    if (index < chunk.size) {
                                        val team = chunk[index]
                                        Box(modifier = Modifier.weight(1f)) {
                                            FavoritedTeamGridItem(team = team, onTeamClick = onTeamClick, onToggle = onToggleFavorite)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.fav_trending_teams),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
            )
        }

        items(trendingTeams) { (id, name, logo) ->
            val isFav = state.teams.any { it.teamId == id }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onTeamClick(id) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = logo,
                    contentDescription = name,
                    modifier = Modifier.size(32.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onToggleFavorite(
                            FavoriteTeamEntity(
                                teamId = id,
                                name = name,
                                logoUrl = logo,
                                countryName = null,
                                savedAt = System.currentTimeMillis()
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) AccentNeonOrange else Color.LightGray
                    )
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun FavoritedTeamGridItem(
    team: FavoriteTeamEntity,
    onTeamClick: (String) -> Unit,
    onToggle: (FavoriteTeamEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onTeamClick(team.teamId) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { onToggle(team) },
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(14.dp))
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = team.logoUrl,
                    contentDescription = team.name,
                    modifier = Modifier.size(40.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = team.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayersTabContent(
    state: FavoritesUiState,
    onToggleFavorite: (FavoritePlayerEntity) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (state.players.isEmpty()) {
        FavoritesEmptyState(
            message = stringResource(R.string.fav_empty_players),
            hint = stringResource(R.string.fav_empty_players_hint)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.players,
                key = { it.playerId },
                contentType = { "favorite_player" }
            ) { player ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = player.imageUrl,
                                contentDescription = player.name,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_ball),
                                error = painterResource(id = R.drawable.ic_ball)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = player.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val details = listOfNotNull(player.position, player.nationality).joinToString(" • ")
                            if (details.isNotBlank()) {
                                Text(
                                    text = details,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        IconButton(
                            onClick = { onToggleFavorite(player) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Unfavorite",
                                tint = AccentNeonOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeamBottomSheet(
    onDismiss: () -> Unit,
    favoriteTeams: List<FavoriteTeamEntity>,
    onToggleFavorite: (String, String, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { Injection.provideFootballRepository(context) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchItemDto>>(emptyList()) }
    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            results = emptyList()
            return@LaunchedEffect
        }
        isSearching = true
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300L)
            val res = repository.multiSearch(query)
            if (res is com.example.soccerworld.data.model.DataResult.Success) {
                results = res.data.filterIsInstance<TeamSearchItemDto>()
            }
            isSearching = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.fav_add_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.fav_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (results.isEmpty() && query.trim().length >= 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.fav_search_no_results, query), color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { item ->
                        if (item is TeamSearchItemDto) {
                            val isFav = favoriteTeams.any { it.teamId == item.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = item.name,
                                    modifier = Modifier.size(36.dp),
                                    placeholder = painterResource(id = R.drawable.ic_ball),
                                    error = painterResource(id = R.drawable.ic_ball)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(item.countryName ?: "Club", fontSize = 11.sp, color = Color.Gray)
                                }
                                IconButton(
                                    onClick = {
                                        onToggleFavorite(item.id, item.name, item.image, item.countryName)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Star",
                                        tint = if (isFav) AccentNeonOrange else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesEmptyState(message: String, hint: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hint,
                fontSize = 12.sp,
                color = Color.Gray,
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
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.fav_login_required),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.fav_login_required_desc),
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.fav_signin), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    SoccerWorldTheme {
        FavoritesScreen()
    }
}

@Preview(showBackground = true, name = "Favorites - Light Mode")
@Composable
fun FavoritesScreenLightPreview() {
    SoccerWorldTheme(darkTheme = false) {
        FavoritesScreenContent(
            state = getMockFavoritesState(),
            isLoggedIn = true,
            onMatchClick = {},
            onNavigateToLogin = {},
            onTeamClick = {},
            onToggleFavoriteMatch = {},
            onToggleFavoriteTeam = {},
            onToggleFavoritePlayer = {},
            onSearchToggleFavoriteTeam = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Favorites - Dark Mode")
@Composable
fun FavoritesScreenDarkPreview() {
    SoccerWorldTheme(darkTheme = true) {
        FavoritesScreenContent(
            state = getMockFavoritesState(),
            isLoggedIn = true,
            onMatchClick = {},
            onNavigateToLogin = {},
            onTeamClick = {},
            onToggleFavoriteMatch = {},
            onToggleFavoriteTeam = {},
            onToggleFavoritePlayer = {},
            onSearchToggleFavoriteTeam = { _, _, _, _ -> }
        )
    }
}

private fun getMockFavoritesState() = FavoritesUiState(
    isLoading = false,
    matches = listOf(
        Matche(
            id = "1",
            utcDate = "2024-05-19T15:00:00Z",
            status = "FINISHED",
            homeTeam = HomeTeam(id = "57", name = "Arsenal", crest = "https://crests.football-data.org/57.png"),
            awayTeam = AwayTeam(id = "61", name = "Chelsea", crest = "https://crests.football-data.org/61.png"),
            score = Score(fullTime = FullTime(home = 3, away = 1))
        ),
        Matche(
            id = "2",
            utcDate = "2024-05-20T19:00:00Z",
            status = "TIMED",
            homeTeam = HomeTeam(id = "66", name = "Man United", crest = "https://crests.football-data.org/66.png"),
            awayTeam = AwayTeam(id = "64", name = "Liverpool", crest = "https://crests.football-data.org/64.png"),
            score = Score(fullTime = FullTime(home = null, away = null))
        ),
        Matche(
            id = "3",
            utcDate = "2023-10-27T20:00:00Z",
            status = "IN_PLAY",
            homeTeam = HomeTeam(id = "3", name = "Liverpool FC", crest = "https://crests.football-data.org/64.png"),
            awayTeam = AwayTeam(id = "4", name = "Manchester City FC", crest = "https://crests.football-data.org/65.png"),
            score = Score(fullTime = FullTime(home = 1, away = 1))
        )
    ),
    teams = listOf(
        FavoriteTeamEntity("57", "Arsenal", "https://crests.football-data.org/57.png", "England", 0),
        FavoriteTeamEntity("66", "Man United", "https://crests.football-data.org/66.png", "England", 0)
    ),
    players = listOf(
        FavoritePlayerEntity("1", "Lionel Messi", "https://www.flashscore.com/res/image/data/d8SZZtZg-S4hzKKkP.png", "Argentina", "Forward", 0),
        FavoritePlayerEntity("2", "C. Ronaldo", "https://www.flashscore.com/res/image/data/nsF9bZdM-bTK8dxEL.png", "Portugal", "Forward", 0)
    )
)
