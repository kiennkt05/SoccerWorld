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
import androidx.compose.foundation.text.BasicTextField
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
import com.example.soccerworld.ui.theme.BrandGreenMedium
import com.example.soccerworld.ui.theme.AccentNeonOrange
import com.example.soccerworld.ui.theme.AccentNeonMint
import com.example.soccerworld.ui.theme.LightBackground
import com.example.soccerworld.ui.theme.DividerColor
import com.example.soccerworld.ui.player.PlayerDetailInfo
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.util.Constant
import com.example.soccerworld.util.FlashLiveLeague
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import com.example.soccerworld.R
import com.example.soccerworld.ui.theme.SoccerWorldTheme
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
    onPlayerClick: (PlayerDetailInfo) -> Unit = {},
    onCompetitionClick: (com.example.soccerworld.util.FlashLiveLeague) -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = remember { CustomSharedPreferences.invoke(context) }
    val viewModel: SearchViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideFootballRepository(context))
    )
    val state by viewModel.uiState.collectAsState()
    
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val recentSearches = remember {
        mutableStateListOf<String>().apply {
            addAll(sharedPrefs.getSearchHistory())
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top Bar ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandGreenMedium)
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

                BasicTextField(
                    value = state.query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(40.dp)
                        .background(Color.White, CircleShape),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    singleLine = true,
                    cursorBrush = SolidColor(BrandGreenMedium),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (state.query.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                            if (state.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChanged("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // ── Body Display ──────────────────────────────────────────────────────────
        when {
            // Loading State
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BrandGreenMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.search_searching), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            stringResource(R.string.search_no_results, state.query),
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
                    contentColor = BrandGreenMedium,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                            color = BrandGreenMedium
                        )
                    }
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        text = {
                            Text(
                                text = stringResource(R.string.search_tab_suggested),
                                fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = BrandGreenMedium,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        text = {
                            Text(
                                text = stringResource(R.string.search_tab_recent),
                                fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = BrandGreenMedium,
                        unselectedContentColor = Color.Gray
                    )
                }

                if (selectedSubTab == 0) {
                    SuggestedTabContent(
                        onTeamClick = { id, name ->
                            sharedPrefs.addSearchQuery(name)
                            recentSearches.clear()
                            recentSearches.addAll(sharedPrefs.getSearchHistory())
                            onTeamClick(id)
                        },
                        onPlayerClick = { playerInfo ->
                            sharedPrefs.addSearchQuery(playerInfo.name)
                            recentSearches.clear()
                            recentSearches.addAll(sharedPrefs.getSearchHistory())
                            onPlayerClick(playerInfo)
                        },
                        onCompetitionClick = { league ->
                            sharedPrefs.addSearchQuery(league.name)
                            recentSearches.clear()
                            recentSearches.addAll(sharedPrefs.getSearchHistory())
                            onCompetitionClick(league)
                        }
                    )
                } else {
                    RecentTabContent(
                        recentSearches = recentSearches,
                        onItemClick = { query ->
                            sharedPrefs.addSearchQuery(query)
                            recentSearches.clear()
                            recentSearches.addAll(sharedPrefs.getSearchHistory())
                            viewModel.onSearchQueryChanged(query)
                        },
                        onClearAll = {
                            sharedPrefs.clearSearchHistory()
                            recentSearches.clear()
                        },
                        onRemoveItem = { item ->
                            sharedPrefs.removeSearchQuery(item)
                            recentSearches.clear()
                            recentSearches.addAll(sharedPrefs.getSearchHistory())
                        }
                    )
                }
            }

            // Active Search Results
            else -> {
                SearchResultList(
                    results = state.results,
                    onTeamClick = { id, name ->
                        sharedPrefs.addSearchQuery(name)
                        recentSearches.clear()
                        recentSearches.addAll(sharedPrefs.getSearchHistory())
                        onTeamClick(id)
                    },
                    onPlayerClick = { playerInfo ->
                        sharedPrefs.addSearchQuery(playerInfo.name)
                        recentSearches.clear()
                        recentSearches.addAll(sharedPrefs.getSearchHistory())
                        onPlayerClick(playerInfo)
                    },
                    onCompetitionClick = { league ->
                        sharedPrefs.addSearchQuery(league.name)
                        recentSearches.clear()
                        recentSearches.addAll(sharedPrefs.getSearchHistory())
                        onCompetitionClick(league)
                    }
                )
            }
        }
    }
}

// ── Suggested Tab Content Composable ─────────────────────────────────────────
@Composable
private fun SuggestedTabContent(
    onTeamClick: (String, String) -> Unit,
    onPlayerClick: (PlayerDetailInfo) -> Unit,
    onCompetitionClick: (FlashLiveLeague) -> Unit
) {
    val expandedStates = remember { mutableStateMapOf("Vietnam" to false, "World" to false, "Europe" to false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        
        // 1. Top Teams
        item {
            SuggestedHeader(title = stringResource(R.string.search_header_top_teams))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val teams = listOf(
                    Triple("ppjDR086", "Manchester United", "https://crests.football-data.org/66.png"),
                    Triple("CjhkPw0k", "Paris Saint-Germain", "https://crests.football-data.org/524.png"),
                    Triple("W8mj7MDD", "Real Madrid", "https://crests.football-data.org/86.png"),
                    Triple("SKbpVP5K", "FC Barcelona", "https://crests.football-data.org/81.png")
                )
                items(teams, key = { it.first }) { (id, name, logo) ->
                    SuggestedCard(name = name, logo = logo) {
                        onTeamClick(id, name)
                    }
                }
            }
        }

        // 2. Top Players
        item {
            SuggestedHeader(title = stringResource(R.string.search_header_top_players))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val players = listOf(
                    Triple("WGOY4FSt", "C. Ronaldo", "https://www.flashscore.com/res/image/data/nsF9bZdM-bTK8dxEL.png"),
                    Triple("vgOOdZbd", "Lionel Messi", "https://www.flashscore.com/res/image/data/d8SZZtZg-S4hzKKkP.png"),
                    Triple("Wn6E2SED", "K. Mbappé", "https://www.flashscore.com/res/image/data/W6bb7hg5-fgmNpuz4.png"),
                    Triple("lAt3vEub", "Lamine Yamal", "https://www.flashscore.com/res/image/data/KKCp9peM-hEMFHRD2.png")
                )
                items(players, key = { it.first }) { (id, name, photo) ->
                    SuggestedPlayerCard(name = name, photo = photo) {
                        val playerInfo = PlayerDetailInfo(
                            id = id,
                            name = name,
                            position = "Forward",
                            dateOfBirth = when (id) {
                                "WGOY4FSt" -> "1985-02-05"
                                "vgOOdZbd" -> "1987-06-24"
                                "Wn6E2SED" -> "1998-12-20"
                                "lAt3vEub" -> "2007-07-13"
                                else -> null
                            },
                            nationality = when (id) {
                                "WGOY4FSt" -> "Portugal"
                                "vgOOdZbd" -> "Argentina"
                                "Wn6E2SED" -> "France"
                                "lAt3vEub" -> "Spain"
                                else -> null
                            },
                            jerseyNumber = when (id) {
                                "WGOY4FSt" -> 7
                                "vgOOdZbd" -> 10
                                "Wn6E2SED" -> 9
                                "lAt3vEub" -> 19
                                else -> null
                            },
                            imageUrl = photo,
                            flagId = null,
                            teamId = null
                        )
                        onPlayerClick(playerInfo)
                    }
                }
            }
        }

        // 3. Rankings
//        item {
//            SuggestedHeader(title = stringResource(R.string.search_header_rankings))
//            LazyRow(
//                contentPadding = PaddingValues(horizontal = 16.dp),
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                items(2) { index ->
//                    val name = if (index == 0) "FIFA Rankings" else "UEFA Rankings"
//                    val icon = if (index == 0) Icons.Default.Public else Icons.Default.Star
//                    SuggestedRankingCard(name = name, icon = icon) {}
//                }
//            }
//        }

        // 4. Top Competitions
        item {
            SuggestedHeader(title = stringResource(R.string.search_header_top_competitions))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val comps = listOf(
                    Constant.FLASHLIVE_LEAGUES["CL"]!! to "https://crests.football-data.org/CL.png",
                    Constant.FLASHLIVE_LEAGUES["EL"]!! to "https://crests.football-data.org/CL.png",
                    Constant.FLASHLIVE_LEAGUES["PL"]!! to "https://crests.football-data.org/PL.png",
                    Constant.FLASHLIVE_LEAGUES["PD"]!! to "https://crests.football-data.org/PD.png"
                )
                items(comps) { (league, logo) ->
                    SuggestedCard(name = league.name, logo = logo) {
                        onCompetitionClick(league)
                    }
                }
            }
        }

        // 5. All Competitions Expandable Accordions
        item {
            SuggestedHeader(title = stringResource(R.string.search_header_all_competitions))
        }

//        // Vietnam expandable
//        item {
//            ExpandableCategoryRow(
//                title = "Vietnam",
//                icon = Icons.Default.SportsSoccer,
//                badge = null,
//                isExpanded = expandedStates["Vietnam"] == true,
//                onToggle = { expandedStates["Vietnam"] = !(expandedStates["Vietnam"] == true) }
//            )
//            AnimatedVisibility(
//                visible = expandedStates["Vietnam"] == true,
//                enter = fadeIn() + expandVertically(),
//                exit = fadeOut() + shrinkVertically()
//            ) {
//                Column(modifier = Modifier.background(Color.White)) {
//                    Text(
//                        text = "V-League 1",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { onCompetitionClick("PL", "V-League 1") } // Demo redirect
//                            .padding(horizontal = 32.dp, vertical = 12.dp)
//                    )
//                }
//            }
//        }

        // World expandable
        item {
            ExpandableCategoryRow(
                title = "World",
                icon = Icons.Default.Public,
                badge = null,
                badgeColor = AccentNeonMint,
                isExpanded = expandedStates["World"] == true,
                onToggle = { expandedStates["World"] = !(expandedStates["World"] == true) }
            )
            AnimatedVisibility(
                visible = expandedStates["World"] == true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.background(Color.White)) {
                    val items = listOf(
                        Constant.FLASHLIVE_LEAGUES["WC"]!!,
                        Constant.FLASHLIVE_LEAGUES["CWC"]!!,
                        Constant.FLASHLIVE_LEAGUES["FCC"]!!
                    )
                    items.forEach { league ->
                        Text(
                            text = league.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCompetitionClick(league) }
                                .padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
                    }
                }
            }
        }

        // Europe expandable
        item {
            ExpandableCategoryRow(
                title = "Europe",
                icon = Icons.Default.Star,
                badge = null,
                badgeColor = BrandGreenMedium,
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
                        Constant.FLASHLIVE_LEAGUES["CL"]!!,
                        Constant.FLASHLIVE_LEAGUES["PL"]!!,
                        Constant.FLASHLIVE_LEAGUES["PD"]!!,
                        Constant.FLASHLIVE_LEAGUES["BL1"]!!,
                        Constant.FLASHLIVE_LEAGUES["SA"]!!,
                        Constant.FLASHLIVE_LEAGUES["FL1"]!!
                    )
                    items.forEach { league ->
                        Text(
                            text = league.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCompetitionClick(league) }
                                .padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
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
                text = stringResource(R.string.search_no_recent),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
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
                        HorizontalDivider(thickness = 0.5.dp, color = DividerColor)
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
                Text(stringResource(R.string.search_clear_history), color = BrandGreenMedium, fontWeight = FontWeight.Bold)
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
        color = BrandGreenMedium,
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
        border = BorderStroke(1.dp, DividerColor)
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
                lineHeight = 1.2.em,
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
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(DividerColor),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = photo,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
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
        border = BorderStroke(1.dp, DividerColor)
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandGreenMedium,
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
        border = BorderStroke(0.5.dp, DividerColor)
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
    onTeamClick: (String, String) -> Unit,
    onPlayerClick: (PlayerDetailInfo) -> Unit,
    onCompetitionClick: (com.example.soccerworld.util.FlashLiveLeague) -> Unit
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
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick, onCompetitionClick = onCompetitionClick)
            }
        }

        // Players section
        if (players.isNotEmpty()) {
            item { GroupHeader(title = "Players", icon = Icons.Default.Person, count = players.size) }
            items(players) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick, onCompetitionClick = onCompetitionClick)
            }
        }

        // Tournaments section
        if (tournaments.isNotEmpty()) {
            item { GroupHeader(title = "Tournaments", icon = Icons.Default.Star, count = tournaments.size) }
            items(tournaments) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick, onCompetitionClick = onCompetitionClick)
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String, icon: ImageVector, count: Int) {
    val primary = BrandGreenMedium
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
    onTeamClick: (String, String) -> Unit,
    onPlayerClick: (PlayerDetailInfo) -> Unit,
    onCompetitionClick: (com.example.soccerworld.util.FlashLiveLeague) -> Unit
) {
    val imageUrl = when (item) {
        is TeamSearchItemDto -> item.image
        is PlayerSearchItemDto -> item.image
        is TournamentSearchItemDto -> item.image
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
        is TeamSearchItemDto -> BrandGreenMedium
        is PlayerSearchItemDto -> AccentNeonOrange
        is TournamentSearchItemDto -> AccentNeonMint
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item) {
                    is TeamSearchItemDto -> onTeamClick(item.id, item.name)
                    is PlayerSearchItemDto -> {
                        val playerInfo = PlayerDetailInfo(
                            id = item.id,
                            name = item.name,
                            position = null,
                            dateOfBirth = null,
                            nationality = item.countryName,
                            jerseyNumber = null,
                            imageUrl = item.image,
                            flagId = null,
                            teamId = null
                        )
                        onPlayerClick(playerInfo)
                    }
                    is TournamentSearchItemDto -> {
                        val stageId = item.tournamentStageIds?.firstOrNull() ?: item.id
                        val seasonId = item.tournamentId
                        val additionalStageIds = item.tournamentStageIds?.drop(1) ?: emptyList()
                        val league = com.example.soccerworld.util.FlashLiveLeague(
                            stageId = stageId,
                            seasonId = seasonId,
                            name = item.name,
                            additionalStageIds = additionalStageIds,
                            countryName = item.countryName,
                            image = item.image
                        )
                        onCompetitionClick(league)
                    }
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

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SoccerWorldTheme {
        SearchScreen()
    }
}
