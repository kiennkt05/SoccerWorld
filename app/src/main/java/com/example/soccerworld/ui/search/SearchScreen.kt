package com.example.soccerworld.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsSoccer
import com.example.soccerworld.ui.theme.TextOnDark
import com.example.soccerworld.ui.theme.TextDark
import com.example.soccerworld.ui.theme.TextSecondary
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onPlayerClick: (String) -> Unit = {},
    onCompetitionClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()
    
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val recentSearches = remember { mutableStateListOf("Arsenal", "Real Madrid", "Manchester United") }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Purple Top Bar (Sofascore Styling) ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF5B3FC4))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { 
                    if (state.query.isNotEmpty()) {
                        viewModel.onSearchQueryChanged("") 
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                TextField(
                    value = state.query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(48.dp),
                    placeholder = {
                        Text(
                            "Search matches, competitions, team...",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            }
        }

        // ── Body Display ──────────────────────────────────────────────────────────
        when {
            // Loading State
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF5B3FC4))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Searching...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Error State
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }

            // No Results Found
            state.results.isEmpty() && state.query.length >= 2 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No results found for\n\"${state.query}\"",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Empty Query State (Show Suggested / Recent tabs)
            state.query.isEmpty() -> {
                // Tab layout
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF5B3FC4),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                            color = Color(0xFF5B3FC4)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = { Text("Suggested", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF5B3FC4),
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = { Text("Recent", fontWeight = FontWeight.Bold) },
                        selectedContentColor = Color(0xFF5B3FC4),
                        unselectedContentColor = Color.Gray
                    )
                }

                if (selectedSubTab == 0) {
                    SuggestedTabContent(
                        onTeamClick = onTeamClick,
                        onPlayerClick = onPlayerClick,
                        onCompetitionClick = onCompetitionClick
                    )
                } else {
                    RecentTabContent(
                        recentSearches = recentSearches,
                        onItemClick = { query -> viewModel.onSearchQueryChanged(query) },
                        onClearAll = { recentSearches.clear() },
                        onRemoveItem = { item -> recentSearches.remove(item) }
                    )
                }
            }

            // Active Search Results
            else -> {
                SearchResultList(
                    results = state.results,
                    onTeamClick = onTeamClick,
                    onPlayerClick = onPlayerClick
                )
            }
        }
    }
}

// ── Suggested Tab Content Composable ─────────────────────────────────────────
@Composable
private fun SuggestedTabContent(
    onTeamClick: (String) -> Unit,
    onPlayerClick: (String) -> Unit,
    onCompetitionClick: (String) -> Unit
) {
    val expandedStates = remember { mutableStateMapOf("Vietnam" to false, "World" to false, "Europe" to false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F9)),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        
        // 1. Top Teams
        item {
            SuggestedHeader(title = "Top teams")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val teams = listOf(
                    Triple("66", "Manchester United", "https://crests.football-data.org/66.png"),
                    Triple("vietnam_team", "Vietnam", "https://images.flashscore.info/image/r_4/vietnam-4V0l10a5.png"),
                    Triple("86", "Real Madrid", "https://crests.football-data.org/86.png"),
                    Triple("81", "FC Barcelona", "https://crests.football-data.org/81.png")
                )
                items(teams, key = { it.first }) { (id, name, logo) ->
                    SuggestedCard(name = name, logo = logo) {
                        onTeamClick(id)
                    }
                }
            }
        }

        // 2. Top Players
        item {
            SuggestedHeader(title = "Top players")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val players = listOf(
                    Triple("cr7", "C. Ronaldo", "https://www.flashscore.com/res/image/data/h24_placeholder.jpg"),
                    Triple("messi", "Lionel Messi", "https://www.flashscore.com/res/image/data/h24_placeholder.jpg"),
                    Triple("mbappe", "K. Mbappé", "https://www.flashscore.com/res/image/data/h24_placeholder.jpg"),
                    Triple("yamal", "Lamine Yamal", "https://www.flashscore.com/res/image/data/h24_placeholder.jpg")
                )
                items(players, key = { it.first }) { (id, name, photo) ->
                    SuggestedPlayerCard(name = name, photo = photo) {
                        onPlayerClick(id)
                    }
                }
            }
        }

        // 3. Rankings
        item {
            SuggestedHeader(title = "Rankings")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(2) { index ->
                    val name = if (index == 0) "FIFA Rankings" else "UEFA Rankings"
                    val icon = if (index == 0) Icons.Default.Public else Icons.Default.Star
                    SuggestedRankingCard(name = name, icon = icon) {}
                }
            }
        }

        // 4. Top Competitions
        item {
            SuggestedHeader(title = "Top competitions")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val comps = listOf(
                    Triple("CL", "UEFA Champions League", "https://crests.football-data.org/CL.png"),
                    Triple("CL", "UEFA Europa League", "https://crests.football-data.org/CL.png"),
                    Triple("PL", "Premier League", "https://crests.football-data.org/PL.png"),
                    Triple("PD", "LaLiga", "https://crests.football-data.org/PD.png")
                )
                items(comps) { (code, name, logo) ->
                    SuggestedCard(name = name, logo = logo) {
                        onCompetitionClick(code)
                    }
                }
            }
        }

        // 5. All Competitions Expandable Accordions
        item {
            SuggestedHeader(title = "All competitions")
        }

        // Vietnam expandable
        item {
            ExpandableCategoryRow(
                title = "Vietnam",
                icon = Icons.Default.SportsSoccer,
                badge = null,
                isExpanded = expandedStates["Vietnam"] == true,
                onToggle = { expandedStates["Vietnam"] = !(expandedStates["Vietnam"] == true) }
            )
            AnimatedVisibility(
                visible = expandedStates["Vietnam"] == true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.background(Color.White)) {
                    Text(
                        text = "V-League 1",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCompetitionClick("PL") } // Demo redirect
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }

        // World expandable
        item {
            ExpandableCategoryRow(
                title = "World",
                icon = Icons.Default.Public,
                badge = "1/7",
                badgeColor = Color(0xFF1DB954),
                isExpanded = expandedStates["World"] == true,
                onToggle = { expandedStates["World"] = !(expandedStates["World"] == true) }
            )
            AnimatedVisibility(
                visible = expandedStates["World"] == true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.background(Color.White)) {
                    val items = listOf("FIFA World Cup", "Club World Cup", "FIFA Confederations Cup")
                    items.forEach { name ->
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCompetitionClick("CL") } // Demo redirect
                                .padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E8EF))
                    }
                }
            }
        }

        // Europe expandable
        item {
            ExpandableCategoryRow(
                title = "Europe",
                icon = Icons.Default.Star,
                badge = "3",
                badgeColor = Color(0xFF5B3FC4),
                isExpanded = expandedStates["Europe"] == true,
                onToggle = { expandedStates["Europe"] = !(expandedStates["Europe"] == true) }
            )
            AnimatedVisibility(
                visible = expandedStates["Europe"] == true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.background(Color.White)) {
                    val items = listOf(
                        "UEFA Champions League" to "CL",
                        "UEFA Europa League" to "CL",
                        "UEFA Conference League" to "CL"
                    )
                    items.forEach { (name, code) ->
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCompetitionClick(code) }
                                .padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E8EF))
                    }
                }
            }
        }
    }
}

// ── Recent Tab Content Composable ───────────────────────────────────────────
@Composable
private fun RecentTabContent(
    recentSearches: List<String>,
    onItemClick: (String) -> Unit,
    onClearAll: () -> Unit,
    onRemoveItem: (String) -> Unit
) {
    if (recentSearches.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No recent searches",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F9))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                recentSearches.forEachIndexed { index, query ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(query) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = query,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onRemoveItem(query) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (index < recentSearches.size - 1) {
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E8EF))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onClearAll) {
                Text("Clear search history", color = Color(0xFF5B3FC4), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Search Layout Sub-components ─────────────────────────────────────────────

@Composable
private fun SuggestedHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF5B3FC4),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun SuggestedCard(name: String, logo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8E8EF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = logo,
                contentDescription = name,
                modifier = Modifier
                    .size(44.dp)
                    .padding(bottom = 6.dp),
                placeholder = painterResource(id = R.drawable.ic_ball),
                error = painterResource(id = R.drawable.ic_ball)
            )
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun SuggestedPlayerCard(name: String, photo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8E8EF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E8EF)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photo,
                    contentDescription = name,
                    modifier = Modifier.size(36.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun SuggestedRankingCard(name: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE8E8EF))
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
                    .background(Color(0xFF5B3FC4).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF5B3FC4),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpandableCategoryRow(
    title: String,
    icon: ImageVector,
    badge: String?,
    badgeColor: Color = Color.Gray,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE8E8EF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            if (badge != null) {
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// ── Search Results List ──────────────────────────────────────────────────────
@Composable
private fun SearchResultList(
    results: List<SearchItemDto>,
    onTeamClick: (String) -> Unit,
    onPlayerClick: (String) -> Unit
) {
    val teams = results.filterIsInstance<TeamSearchItemDto>()
    val players = results.filterIsInstance<PlayerSearchItemDto>()
    val tournaments = results.filterIsInstance<TournamentSearchItemDto>()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Teams section
        if (teams.isNotEmpty()) {
            item { GroupHeader(title = "Clubs", icon = Icons.Default.AccountBox, count = teams.size) }
            items(teams) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick)
            }
        }

        // Players section
        if (players.isNotEmpty()) {
            item { GroupHeader(title = "Players", icon = Icons.Default.Person, count = players.size) }
            items(players) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick)
            }
        }

        // Tournaments section
        if (tournaments.isNotEmpty()) {
            item { GroupHeader(title = "Tournaments", icon = Icons.Default.Star, count = tournaments.size) }
            items(tournaments) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick)
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String, icon: ImageVector, count: Int) {
    val primary = Color(0xFF5B3FC4)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = primary,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun SearchItemCard(
    item: SearchItemDto,
    onTeamClick: (String) -> Unit,
    onPlayerClick: (String) -> Unit
) {
    val imageUrl = when (item) {
        is TeamSearchItemDto -> item.image
        is PlayerSearchItemDto -> item.image
        else -> null
    }
    val name = when (item) {
        is TeamSearchItemDto -> item.name
        is PlayerSearchItemDto -> item.name
        is TournamentSearchItemDto -> item.name
        is UnknownSearchItemDto -> "Unknown"
    }
    val subtitle = when (item) {
        is TeamSearchItemDto -> "Club • ${item.countryName ?: "—"}"
        is PlayerSearchItemDto -> "Player • ${item.countryName ?: "—"}"
        is TournamentSearchItemDto -> "Tournament • ${item.countryName ?: "—"}"
        is UnknownSearchItemDto -> "Unknown"
    }
    val typeIcon = when (item) {
        is TeamSearchItemDto -> Icons.Default.AccountBox
        is PlayerSearchItemDto -> Icons.Default.Person
        is TournamentSearchItemDto -> Icons.Default.Star
        else -> Icons.Default.Search
    }
    val typeColor = when (item) {
        is TeamSearchItemDto -> Color(0xFF5B3FC4)
        is PlayerSearchItemDto -> Color(0xFFFF9800)
        is TournamentSearchItemDto -> Color(0xFF1DB954)
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item) {
                    is TeamSearchItemDto -> onTeamClick(item.id)
                    is PlayerSearchItemDto -> onPlayerClick(item.id)
                    else -> {}
                }
            }
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        placeholder = painterResource(id = R.drawable.ic_ball),
                        error = painterResource(id = R.drawable.ic_ball),
                        fallback = painterResource(id = R.drawable.ic_ball)
                    )
                } else {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = typeColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(6.dp)
                )
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
