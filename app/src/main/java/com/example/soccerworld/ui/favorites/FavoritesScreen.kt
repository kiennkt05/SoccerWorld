package com.example.soccerworld.ui.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.soccerworld.ui.theme.DividerColor
import com.example.soccerworld.ui.theme.LightBackground
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
import com.example.soccerworld.ui.fixture.FixtureCard
import com.example.soccerworld.ui.theme.BrandGreenLight
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
    // Added isLoggedIn as a parameter with a default value that checks LocalInspectionMode
    // to prevent FirebaseAuth from crashing in the Android Studio Preview.
    isLoggedIn: Boolean = if (LocalInspectionMode.current) false else FirebaseAuth.getInstance().currentUser != null
) {
    val context = LocalContext.current
    val viewModel: FavoritesViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTeamSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandGreenMedium)
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
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                // Sub-tabs (Events, Teams, Players)
                TabRow(
                    selectedTabIndex = selectedTab,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BrandGreenMedium
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
                            selectedContentColor = BrandGreenMedium,
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
                // Keep the matches list locked if user is a guest, but let them interact with Teams locally
                LoginRequiredState(onNavigateToLogin = onNavigateToLogin)
            } else {
                when (selectedTab) {
                    0 -> EventsTabContent(
                        state = state,
                        onMatchClick = onMatchClick,
                        onToggleFavoriteMatch = { match -> viewModel.toggleFavoriteMatch(match) }
                    )
                    1 -> TeamsTabContent(
                        state = state,
                        onAddClick = { showAddTeamSheet = true },
                        onTeamClick = onTeamClick,
                        onToggleFavorite = { team ->
                            viewModel.toggleFavoriteTeam(
                                teamId = team.teamId,
                                name = team.name,
                                logoUrl = team.logoUrl,
                                countryName = team.countryName
                            )
                        }
                    )
                    2 -> PlayersTabContent(
                        state = state,
                        onToggleFavorite = { player ->
                            viewModel.toggleFavoritePlayer(
                                playerId = player.playerId,
                                name = player.name,
                                imageUrl = player.imageUrl,
                                nationality = player.nationality,
                                position = player.position
                            )
                        }
                    )
                }
            }

            // Sync prompt banner at top if user is a guest and looking at teams
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
            onToggleFavorite = { id, name, logo, country ->
                viewModel.toggleFavoriteTeam(id, name, logo, country)
            }
        )
    }
}

// ── Sync Banner for Guests ───────────────────────────────────────────────────
@Composable
private fun GuestSyncBanner(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGreenMedium),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandGreenMedium),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.fav_signin), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Events (Matches) Tab Content ─────────────────────────────────────────────
@Composable
private fun EventsTabContent(
    state: FavoritesUiState,
    onMatchClick: (String) -> Unit,
    onToggleFavoriteMatch: (com.example.soccerworld.model.fixture.Matche) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandGreenMedium)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

// ── Teams Tab Content ────────────────────────────────────────────────────────
@Composable
private fun TeamsTabContent(
    state: FavoritesUiState,
    onAddClick: () -> Unit,
    onTeamClick: (String) -> Unit,
    onToggleFavorite: (FavoriteTeamEntity) -> Unit
) {
    val trendingTeams = listOf(
        Triple("66", "Manchester United", "https://crests.football-data.org/66.png"),
        Triple("86", "Real Madrid", "https://crests.football-data.org/86.png"),
        Triple("81", "FC Barcelona", "https://crests.football-data.org/81.png"),
        Triple("64", "Liverpool", "https://crests.football-data.org/64.png"),
        Triple("57", "Arsenal", "https://crests.football-data.org/57.png"),
        Triple("vietnam_team", "Vietnam", "https://images.flashscore.info/image/r_4/vietnam-4V0l10a5.png")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Grid Header
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
                    color = Color.Black
                )
                Text(
                    text = stringResource(R.string.fav_teams_count, state.teams.size),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Teams Grid Area
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
                        // Add Button Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clickable { onAddClick() },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, DividerColor),
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
                                        .background(BrandGreenMedium.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = BrandGreenMedium)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(R.string.fav_add_team), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreenMedium)
                            }
                        }

                        // Populate the first row with favorited teams if available
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

                    // Remaining rows for teams
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

        // Trending Section
        item {
            Text(
                text = stringResource(R.string.fav_trending_teams),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
            )
        }

        items(trendingTeams) { (id, name, logo) ->
            val isFav = state.teams.any { it.teamId == id }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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
                    color = Color.Black,
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
            HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DividerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Delete close icon
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
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

// ── Players Tab Content ─────────────────────────────────────────────────────
@Composable
private fun PlayersTabContent(
    state: FavoritesUiState,
    onToggleFavorite: (FavoritePlayerEntity) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandGreenMedium)
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
                    border = BorderStroke(1.dp, DividerColor),
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
                                .background(DividerColor),
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

// ── Search & Add Team Bottom Sheet ──────────────────────────────────────────
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

    // Search query debounced observer
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
                    CircularProgressIndicator(color = BrandGreenMedium)
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

// ── Generic Empty State Composable ───────────────────────────────────────────
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
                    .background(DividerColor),
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

// ── Authentication Login Prompts ─────────────────────────────────────────────
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
                    .background(BrandGreenMedium.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandGreenMedium,
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
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenMedium)
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
        // In preview, isLoggedIn will default to false safely due to the parameter default value.
        FavoritesScreen()
    }
}
