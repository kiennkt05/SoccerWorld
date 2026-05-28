package com.example.soccerworld.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.soccerworld.R
import com.example.soccerworld.data.remote.flashlive.CareerRow
import com.example.soccerworld.data.remote.flashlive.CareerTabBlock
import com.example.soccerworld.data.remote.flashlive.PlayerData
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
import androidx.compose.ui.tooling.preview.Preview
import com.example.soccerworld.ui.theme.FavoriteGold
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Data class ───────────────────────────────────────────────────────────────
data class PlayerDetailInfo(
    val id: String,
    val name: String,
    val position: String?,
    val dateOfBirth: String?,
    val nationality: String?,
    val jerseyNumber: Int?,
    val imageUrl: String?,
    val flagId: Int?,
    val teamId: String? = null
) : java.io.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    playerInfo: PlayerDetailInfo,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val factory = ViewModelFactory(Injection.provideFootballRepository(context))
    val viewModel: PlayerDetailViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val playerData = uiState.playerData

    val tabs = listOf(
        stringResource(R.string.player_tab_details),
        stringResource(R.string.player_tab_matches),
        stringResource(R.string.player_tab_career)
    )
    var selectedTab by remember { mutableStateOf(0) }

    val activePosition = remember(playerData, playerInfo) {
        playerData?.typeName ?: playerInfo.position ?: "Midfielder"
    }

    val positionColor = remember(activePosition) {
        when {
            activePosition.contains("goalkeeper", ignoreCase = true) -> Color(0xFFFFB300)
            activePosition.contains("defender", ignoreCase = true) -> Color(0xFF1E88E5)
            activePosition.contains("midfielder", ignoreCase = true) -> Color(0xFF43A047)
            activePosition.contains("forward", ignoreCase = true) -> Color(0xFFE53935)
            else -> Color(0xFF546E7A)
        }
    }

    val positionAbbr = remember(activePosition) {
        when {
            activePosition.contains("goalkeeper", ignoreCase = true) -> "GK"
            activePosition.contains("defender", ignoreCase = true) -> "CB"
            activePosition.contains("midfielder", ignoreCase = true) -> "MF"
            activePosition.contains("forward", ignoreCase = true) -> "FW"
            activePosition.contains("coach", ignoreCase = true) -> "HC"
            else -> activePosition.take(2).uppercase()
        }
    }

    // Trigger details & career load in parallel
    LaunchedEffect(playerInfo.id) {
        viewModel.loadPlayerDataAndCareer(playerInfo.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleFavorite(
                            playerId = playerInfo.id,
                            fallbackName = playerInfo.name,
                            fallbackImageUrl = playerInfo.imageUrl,
                            fallbackNationality = playerInfo.nationality
                        )
                    }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) FavoriteGold else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ── Hero Header ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B2C66),  // Deep Navy Blue
                                Color(0xFF283A7E),  // Dark Royal Blue
                                Color(0xFF374DF5).copy(alpha = 0.8f) // Sofascore Blue
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 55.dp, y = (-15).dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player photo
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(playerData?.imagePath ?: playerInfo.imageUrl)
                                .crossfade(true).build(),
                            contentDescription = playerData?.name ?: playerInfo.name,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_players),
                            error = painterResource(R.drawable.ic_players),
                            fallback = painterResource(R.drawable.ic_players)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playerData?.name ?: playerInfo.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.25f)) {
                                Text(
                                    text = positionAbbr,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                            val jersey = playerInfo.jerseyNumber ?: 0
                            if (jersey > 0) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.15f)) {
                                    Text(
                                        text = "#$jersey",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        val teamName = playerData?.parentName ?: playerData?.teamName ?: ""
                        if (teamName.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!playerData?.teamImage.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = playerData?.teamImage,
                                        contentDescription = teamName,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text("⚽", fontSize = 14.sp)
                                }
                                Text(
                                    text = teamName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (!playerInfo.nationality.isNullOrBlank()) {
                            Text(
                                text = "🌍  ${playerInfo.nationality}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Tab Row ──────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = positionColor,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = positionColor
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = positionColor,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Tab Content ──────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> DetailsTab(playerInfo, playerData, uiState.isLoadingDetails, positionColor)
                    1 -> PlayerMatchesTab(uiState, playerData)
                    2 -> PlayerCareerTab(uiState)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1 — DETAILS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailsTab(
    playerInfo: PlayerDetailInfo,
    playerData: PlayerData?,
    isLoading: Boolean,
    positionColor: Color
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val birthdayFormatted = remember(playerData?.birthdayTime) {
        val time = playerData?.birthdayTime?.toLongOrNull()
        if (time != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(time * 1000L))
        } else null
    }

    val calculatedAge = remember(playerData?.birthdayTime) {
        val time = playerData?.birthdayTime?.toLongOrNull()
        if (time != null) {
            val birthCal = Calendar.getInstance().apply { timeInMillis = time * 1000L }
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } else null
    }

    val countryFlagResId = remember(playerData?.countryId) {
        val cid = playerData?.countryId
        if (cid != null && cid > 0) {
            context.resources.getIdentifier("country_flag_$cid", "drawable", context.packageName)
        } else 0
    }

    val displayPosition = remember(playerData?.typeName, playerInfo.position) {
        val type = playerData?.typeName ?: playerInfo.position ?: "Midfielder"
        type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = positionColor)
                }
            }
        } else {
            // Card 1: Club & Position Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        stringResource(R.string.player_sec_club_pos),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            ClubInfoItem(stringResource(R.string.player_lbl_current_club), playerData?.parentName ?: playerData?.teamName ?: "—", playerData?.teamImage)
                            // Spacer(Modifier.height(12.dp))
                            // InfoItem("Preferred Layout", playerData?.layout ?: "—")
                        }
                        Column(Modifier.weight(1f)) {
                            InfoItem(stringResource(R.string.player_lbl_role_pos), displayPosition)
                            // Spacer(Modifier.height(12.dp))
                            // val sport = if (playerData?.sportId == 1) "Football" else "—"
                            // InfoItem("Sport", sport)
                        }
                    }
                }
            }

            // Card 2: Personal Profile & Demographics
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        stringResource(R.string.player_sec_personal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            InfoItem(stringResource(R.string.player_lbl_full_name), playerData?.name ?: playerInfo.name)
                            Spacer(Modifier.height(12.dp))
                            InfoItem(stringResource(R.string.player_lbl_dob), birthdayFormatted ?: "—")
                        }
                        Column(Modifier.weight(1f)) {
                            FlagInfoItem(stringResource(R.string.player_lbl_nationality), playerData?.countryName ?: playerInfo.nationality ?: "—", countryFlagResId)
                            Spacer(Modifier.height(7.5.dp))
                            val ageText = if (calculatedAge != null) stringResource(R.string.player_age_value, calculatedAge) else "—"
                            InfoItem(stringResource(R.string.player_lbl_age), ageText)
                        }
                    }
                }
            }

            // Card 3: Valuation & Contract Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        stringResource(R.string.player_sec_valuation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            InfoItem(stringResource(R.string.player_lbl_valuation), playerData?.pmv ?: "—")
                        }
                        Column(Modifier.weight(1f)) {
                            val pceFormatted = remember(playerData?.pce) {
                                val time = playerData?.pce?.toLongOrNull()
                                if (time != null) {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    sdf.format(Date(time * 1000L))
                                } else "—"
                            }
                            InfoItem(stringResource(R.string.player_lbl_contract_ends), pceFormatted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FlagInfoItem(label: String, value: String, flagResId: Int) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            if (flagResId != 0) {
                Image(
                    painter = painterResource(flagResId),
                    contentDescription = value,
                    modifier = Modifier
                        .size(width = 20.dp, height = 14.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ClubInfoItem(label: String, value: String, logoUrl: String?) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            if (!logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = value,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text("⚽", fontSize = 14.sp)
            }
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 2 — MATCHES (Ported exact layout from TeamTabs.kt)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PlayerMatchesTab(uiState: PlayerDetailUiState, playerData: PlayerData?) {
    when {
        uiState.isLoadingMatches -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        uiState.matchesError != null && uiState.matches.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚽", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.player_no_matches),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        uiState.matches.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(stringResource(R.string.player_no_matches), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> {
            var activeFilter by remember { mutableStateOf("Finished") }
            val filters = listOf("Finished", "Scheduled")

            val finishedMatches = remember(uiState.matches) {
                uiState.matches.filter {
                    it.status == "FINISHED" || it.stage == "FINISHED"
                }.sortedByDescending { it.utcDate.orEmpty() }
            }

            val scheduledMatches = remember(uiState.matches) {
                uiState.matches.filter {
                    it.status == "SCHEDULED" || it.stage == "SCHEDULED" || it.status == "TIMED"
                }.sortedBy { it.utcDate.orEmpty() }
            }

            val activeMatches = if (activeFilter == "Finished") finishedMatches else scheduledMatches

            val grouped = remember(activeMatches) {
                val map = linkedMapOf<String, MutableList<Matche>>()
                activeMatches.forEach { m ->
                    val key = m.competition?.name ?: "Tournament"
                    map.getOrPut(key) { mutableListOf() }.add(m)
                }
                map
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Segmented Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filters.forEach { f ->
                        val selected = activeFilter == f
                        val labelText = if (f == "Finished") stringResource(R.string.player_finished) else stringResource(R.string.player_scheduled)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    else Color.Transparent
                                )
                                .clickable { activeFilter = f },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                labelText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (grouped.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        val filterText = if (activeFilter == "Finished") stringResource(R.string.player_finished).lowercase(Locale.getDefault()) else stringResource(R.string.player_scheduled).lowercase(Locale.getDefault())
                        Text(stringResource(R.string.player_no_status_matches, filterText), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        grouped.forEach { (compName, matchList) ->
                            item(key = compName) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val emblem = matchList.firstOrNull()?.competition?.emblem
                                            AsyncImage(
                                                model = emblem ?: R.drawable.ic_ball,
                                                contentDescription = compName,
                                                modifier = Modifier.size(22.dp).clip(CircleShape),
                                                placeholder = painterResource(R.drawable.ic_ball),
                                                error = painterResource(R.drawable.ic_ball),
                                                fallback = painterResource(R.drawable.ic_ball),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                compName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        matchList.forEachIndexed { idx, match ->
                                            if (idx > 0) HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 14.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                            PlayerMatchRow(match, activeFilter, playerData)
                                        }
                                    }
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
private fun PlayerMatchRow(match: Matche, activeFilter: String, playerData: PlayerData?) {
    val sofaDate = remember(match.utcDate) { formatSofaDate(match.utcDate) }
    val matchTime = remember(match.utcDate) { formatMatchTime(match.utcDate) }
    val isFinished = activeFilter == "Finished"

    val currentTeamId = playerData?.teamId ?: ""
    val currentTeamName = playerData?.parentName ?: playerData?.teamName ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date & Status
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = sofaDate,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            val statusText = if (isFinished) "FT" else matchTime
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFinished) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) 
                        else MaterialTheme.colorScheme.primary
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(0.7.dp)
                .height(38.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // Teams Stack
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            // Home Team
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                AsyncImage(
                    model = match.homeTeam?.crest ?: R.drawable.ic_ball,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val isHomeCurrent = remember(match.homeTeam, currentTeamId, currentTeamName) {
                    isCurrentTeam(match.homeTeam?.id, match.homeTeam?.name, currentTeamId, currentTeamName)
                }
                Text(
                    text = (match.homeTeam?.name ?: "TBD").replace("*", "").trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isHomeCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHomeCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Away Team
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                AsyncImage(
                    model = match.awayTeam?.crest ?: R.drawable.ic_ball,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    placeholder = painterResource(id = R.drawable.ic_ball),
                    error = painterResource(id = R.drawable.ic_ball)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val isAwayCurrent = remember(match.awayTeam, currentTeamId, currentTeamName) {
                    isCurrentTeam(match.awayTeam?.id, match.awayTeam?.name, currentTeamId, currentTeamName)
                }
                Text(
                    text = (match.awayTeam?.name ?: "TBD").replace("*", "").trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (isAwayCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAwayCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scores Column (if finished)
        if (isFinished) {
            val homeScore = match.score?.fullTime?.home
            val awayScore = match.score?.fullTime?.away
            Column(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = homeScore?.toString() ?: "0",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = awayScore?.toString() ?: "0",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Outcome badge
            val homeScoreVal = homeScore ?: 0
            val awayScoreVal = awayScore ?: 0
            val homeIsCurrent = remember(match.homeTeam, currentTeamId, currentTeamName) {
                isCurrentTeam(match.homeTeam?.id, match.homeTeam?.name, currentTeamId, currentTeamName)
            }
            val awayIsCurrent = remember(match.awayTeam, currentTeamId, currentTeamName) {
                isCurrentTeam(match.awayTeam?.id, match.awayTeam?.name, currentTeamId, currentTeamName)
            }
            val outcome = remember(homeScoreVal, awayScoreVal, homeIsCurrent, awayIsCurrent) {
                when {
                    homeScoreVal == awayScoreVal -> "D"
                    homeScoreVal > awayScoreVal -> if (homeIsCurrent) "W" else if (awayIsCurrent) "L" else "D"
                    else -> if (awayIsCurrent) "W" else if (homeIsCurrent) "L" else "D"
                }
            }
            val badgeColor = when (outcome) {
                "W" -> Color(0xFF2EA64F)
                "L" -> Color(0xFFE53935)
                else -> Color(0xFF9E9E9E)
            }

            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = outcome,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun isCurrentTeam(id: String?, name: String?, currentId: String, currentName: String): Boolean {
    if (id != null && currentId.isNotEmpty() && id == currentId) return true
    val cleanName = (name ?: "").replace("*", "").trim().lowercase()
    val cleanCurrent = currentName.replace("*", "").trim().lowercase()
    if (cleanCurrent.isNotEmpty() && cleanName.contains(cleanCurrent)) return true
    return false
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3 — CAREER (Overhauled with premium league & season dropdown filters)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerCareerTab(uiState: PlayerDetailUiState) {
    if (uiState.isLoadingCareer) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (uiState.careerError != null && uiState.careerTabs.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(uiState.careerError, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val careerTabs = uiState.careerTabs
    if (careerTabs.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No career history available") }
        return
    }

    // Get the first item from career data to use as default values
    val firstRow = remember(careerTabs) {
        careerTabs.firstOrNull()?.rows?.firstOrNull()
    }

    // Global Filter states
    var selectedTournament by remember(careerTabs) { 
        mutableStateOf(firstRow?.tournamentName ?: "All Tournaments") 
    }
    var selectedSeason by remember(careerTabs) { 
        mutableStateOf(firstRow?.seasonLabel ?: "All Seasons") 
    }

    // Dropdown open states
    var tournamentExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }

    // Extract unique tournaments and seasons for dropdown options
    val allUniqueTournaments = remember(careerTabs) {
        val list = mutableListOf<String>()
        careerTabs.forEach { tab ->
            tab.rows.orEmpty().forEach { row ->
                row.tournamentName?.let { list.add(it) }
            }
        }
        list.distinct().sorted()
    }

    val allUniqueSeasons = remember(careerTabs) {
        val list = mutableListOf<String>()
        careerTabs.forEach { tab ->
            tab.rows.orEmpty().forEach { row ->
                row.seasonLabel?.let { list.add(it) }
            }
        }
        list.distinct().sortedDescending()
    }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Dropdowns row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tournament Selector dropdown
            Box(modifier = Modifier.weight(1.5f)) {
                // Sleek outline container matching pasted image design
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { tournamentExpanded = !tournamentExpanded }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Find matching row for emblem rendering
                        val matchingRow = remember(selectedTournament, careerTabs) {
                            careerTabs.flatMap { it.rows.orEmpty() }
                                .firstOrNull { it.tournamentName == selectedTournament }
                        }

                        if (selectedTournament == "All Tournaments") {
                            Text("🏆", fontSize = 16.sp)
                        } else {
                            // Render flag emblem or team crest if a specific tournament is selected
                            val flagId = matchingRow?.tournamentFlagId
                            val flagResId = remember(flagId) {
                                if (flagId != null && flagId > 0) {
                                    context.resources.getIdentifier("country_flag_$flagId", "drawable", context.packageName)
                                } else 0
                            }
                            if (flagResId != 0) {
                                Image(
                                    painter = painterResource(flagResId),
                                    contentDescription = selectedTournament,
                                    modifier = Modifier
                                        .size(width = 20.dp, height = 14.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            if (!matchingRow?.teamImageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = matchingRow?.teamImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedTournament,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (tournamentExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = tournamentExpanded,
                    onDismissRequest = { tournamentExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.55f).background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Tournaments", fontWeight = FontWeight.Medium) },
                        onClick = {
                            selectedTournament = "All Tournaments"
                            tournamentExpanded = false
                        }
                    )
                    allUniqueTournaments.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name, fontSize = 13.sp) },
                            onClick = {
                                selectedTournament = name
                                tournamentExpanded = false
                            }
                        )
                    }
                }
            }

            // Season Selector dropdown
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { seasonExpanded = !seasonExpanded }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selectedSeason,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (seasonExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = seasonExpanded,
                    onDismissRequest = { seasonExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.35f).background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Seasons", fontWeight = FontWeight.Medium) },
                        onClick = {
                            selectedSeason = "All Seasons"
                            seasonExpanded = false
                        }
                    )
                    allUniqueSeasons.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 13.sp) },
                            onClick = {
                                selectedSeason = label
                                seasonExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Stats blocks
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            careerTabs.forEach { tabBlock ->
                val filteredRows = remember(tabBlock.rows, selectedTournament, selectedSeason) {
                    tabBlock.rows.orEmpty().filter { row ->
                        val matchesTournament = selectedTournament == "All Tournaments" || row.tournamentName == selectedTournament
                        val matchesSeason = selectedSeason == "All Seasons" || row.seasonLabel == selectedSeason
                        matchesTournament && matchesSeason
                    }
                }

                if (filteredRows.isNotEmpty()) {
                    Text(
                        text = tabBlock.tabLabel ?: "Competition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            // Column Headers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Season/Team", modifier = Modifier.weight(2.2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Apps", modifier = Modifier.weight(0.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Text("Goals", modifier = Modifier.weight(0.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Text("Asts", modifier = Modifier.weight(0.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Text("Cards", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Text("Rating", modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            filteredRows.forEachIndexed { idx, row ->
                                if (idx > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }
                                CareerStatRow(row, context)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerStatRow(row: CareerRow, context: android.content.Context) {
    val stats = row.stats.orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Season & Team Info
        Column(modifier = Modifier.weight(2.2f)) {
            Text(
                text = row.seasonLabel ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!row.teamImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = row.teamImageUrl,
                        contentDescription = row.teamName,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = row.teamName ?: "Unknown",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val flagResId = remember(row.tournamentFlagId) {
                    if (row.tournamentFlagId != null && row.tournamentFlagId > 0) {
                        context.resources.getIdentifier("country_flag_${row.tournamentFlagId}", "drawable", context.packageName)
                    } else 0
                }
                if (flagResId != 0) {
                    Image(
                        painter = painterResource(flagResId),
                        contentDescription = row.tournamentName,
                        modifier = Modifier
                            .size(width = 14.dp, height = 10.dp)
                            .clip(RoundedCornerShape(1.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = row.tournamentName ?: "Unknown League",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Apps
        Text(
            text = stats["4"] ?: "0",
            modifier = Modifier.weight(0.7f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Goals
        Text(
            text = stats["1"] ?: "0",
            modifier = Modifier.weight(0.7f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Assists
        Text(
            text = stats["8"] ?: "0",
            modifier = Modifier.weight(0.7f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Yellow / Red Cards row
        val yellow = stats["2"] ?: "0"
        val red = stats["3"] ?: "0"
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = 13.dp)
                    .background(Color(0xFFFFEA00), RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(2.dp))
            Text(yellow, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = 13.dp)
                    .background(Color(0xFFFF1744), RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(2.dp))
            Text(red, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        // Rating
        val ratingStr = stats["13"] ?: "—"
        val ratingVal = ratingStr.toDoubleOrNull()
        val ratingColor = remember(ratingVal) {
            when {
                ratingVal == null -> Color.Gray
                ratingVal >= 7.5 -> Color(0xFF2EA64F)
                ratingVal >= 6.8 -> Color(0xFFF4A300)
                else -> Color(0xFFE53935)
            }
        }
        Box(
            modifier = Modifier
                .weight(0.9f)
                .wrapContentWidth(Alignment.End)
                .background(ratingColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                ratingStr,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RADAR CHART — Labels tại 5 đỉnh ngũ giác
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AttributeRadarChart(
    attributes: Map<String, Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val entries = attributes.entries.toList()
    val n = entries.size
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    BoxWithConstraints(modifier = modifier) {
        val w = this.maxWidth.value    // dp float
        val h = this.maxHeight.value   // dp float
        val cx = w / 2f
        val cy = h / 2f
        val labelR = minOf(cx, cy) * 0.82f
        val labelHalfW = 22f

        fun angle(i: Int): Double = 2.0 * PI * i / n - PI / 2

        Canvas(modifier = Modifier.fillMaxSize()) {
            val pcx = size.width / 2f
            val pcy = size.height / 2f
            val r = minOf(pcx, pcy) * 0.50f

            fun a(i: Int) = (2.0 * PI * i / n - PI / 2).toFloat()

            // Vòng nền (4 vòng)
            for (ring in 1..4) {
                val rr = r * ring / 4f
                val path = Path().apply {
                    for (i in 0 until n) {
                        val x = pcx + rr * cos(a(i))
                        val y = pcy + rr * sin(a(i))
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(
                    path,
                    Color.Gray.copy(alpha = if (ring == 4) 0.22f else 0.10f),
                    style = Stroke(1f * density)
                )
            }

            // Trục từ tâm ra đỉnh
            for (i in 0 until n) {
                drawLine(
                    Color.Gray.copy(alpha = 0.25f),
                    start = Offset(pcx, pcy),
                    end = Offset(pcx + r * cos(a(i)), pcy + r * sin(a(i))),
                    strokeWidth = 1f * density
                )
            }

            // Polygon giá trị
            val values = entries.map { it.value / 100f }
            val vPath = Path().apply {
                for (i in 0 until n) {
                    val vr = r * values[i]
                    val x = pcx + vr * cos(a(i))
                    val y = pcy + vr * sin(a(i))
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(vPath, color.copy(alpha = 0.22f))
            drawPath(vPath, color, style = Stroke(2.8f * density, cap = StrokeCap.Round))

            // Chấm tại giá trị mỗi đỉnh
            for (i in 0 until n) {
                val vr = r * values[i]
                val px = pcx + vr * cos(a(i))
                val py = pcy + vr * sin(a(i))
                drawCircle(Color.White, 5.5f * density, Offset(px, py))
                drawCircle(color, 4f * density, Offset(px, py))
                drawCircle(Color.White, 2f * density, Offset(px, py))
            }
        }

        // Overlay label tại từng đỉnh
        entries.forEachIndexed { i, (label, score) ->
            val ang = angle(i)
            val lx = cx + labelR * cos(ang).toFloat()
            val ly = cy + labelR * sin(ang).toFloat()

            val badgeColor = when {
                score >= 75 -> Color(0xFF2EA64F)
                score >= 55 -> Color(0xFFF4A300)
                else        -> Color(0xFFE53935)
            }

            Column(
                modifier = Modifier
                    .absoluteOffset(
                        x = (lx - labelHalfW).dp,
                        y = (ly - labelHalfW).dp
                    )
                    .width((labelHalfW * 2).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$score",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 11.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MINI FOOTBALL PITCH
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MiniFootballPitch(
    position: String?,
    positionColor: Color,
    modifier: Modifier = Modifier
) {
    val pitchGreen = Color(0xFF1B5E20)
    val lineColor = Color.White.copy(alpha = 0.75f)
    val dotX = 0.5f
    val dotY = when {
        position == null -> 0.50f
        position.contains("goalkeeper", ignoreCase = true) -> 0.88f
        position.contains("defender", ignoreCase = true)   -> 0.72f
        position.contains("midfielder", ignoreCase = true) -> 0.50f
        position.contains("forward", ignoreCase = true)    -> 0.22f
        else         -> 0.50f
    }

    Canvas(modifier = modifier.background(pitchGreen, RoundedCornerShape(10.dp))) {
        val w = size.width; val h = size.height; val sw = 1.5f * density
        drawRect(lineColor, topLeft = Offset(4f * density, 4f * density),
            size = androidx.compose.ui.geometry.Size(w - 8f * density, h - 8f * density), style = Stroke(sw))
        drawLine(lineColor, Offset(4f * density, h / 2f), Offset(w - 4f * density, h / 2f), sw)
        drawCircle(lineColor, radius = w * 0.16f, center = Offset(w / 2f, h / 2f), style = Stroke(sw))
        drawCircle(lineColor, radius = 2.5f * density, center = Offset(w / 2f, h / 2f))
        val pbW = w * 0.56f; val pbH = h * 0.18f
        drawRect(lineColor, topLeft = Offset((w - pbW) / 2f, 4f * density),
            size = androidx.compose.ui.geometry.Size(pbW, pbH), style = Stroke(sw))
        drawRect(lineColor, topLeft = Offset((w - pbW) / 2f, h - 4f * density - pbH),
            size = androidx.compose.ui.geometry.Size(pbW, pbH), style = Stroke(sw))
        val px = w * dotX; val py = h * dotY
        drawCircle(positionColor, radius = 10f * density, center = Offset(px, py))
        drawCircle(Color.White, radius = 3.5f * density, center = Offset(px, py))
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────
private fun formatSofaDate(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDate)
        val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: utcDate
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(utcDate)
            val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: utcDate
        } catch (ex: Exception) {
            utcDate.take(10)
        }
    }
}

private fun formatMatchTime(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(utcDate)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: utcDate
    } catch (e: Exception) {
        ""
    }
}

@Suppress("UNUSED_PARAMETER")
private fun Modifier.tabIndicatorOffset(tabPosition: TabPosition): Modifier =
    this.fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = tabPosition.left)
        .width(tabPosition.width)

// ── Composable Previews ────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun DetailsTabPreview() {
    val sampleInfo = PlayerDetailInfo(
        id = "WGOY4FSt",
        name = "Cristiano Ronaldo",
        position = "Forward",
        dateOfBirth = "1985-02-05",
        nationality = "Portugal",
        jerseyNumber = 7,
        imageUrl = null,
        flagId = 155
    )
    val sampleData = PlayerData(
        id = "WGOY4FSt",
        name = "Cristiano Ronaldo",
        parentName = "Al Nassr",
        pmv = "€11.9m",
        pce = "1814313600",
        countryName = "Portugal",
        genderId = 1,
        typeName = "forward",
        teamId = "h4I89ZuE",
        teamName = "Al Nassr"
    )
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DetailsTab(
                playerInfo = sampleInfo,
                playerData = sampleData,
                isLoading = false,
                positionColor = Color(0xFFE53935)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerMatchesTabPreview() {
    val sampleData = PlayerData(
        id = "WGOY4FSt",
        name = "Cristiano Ronaldo",
        teamId = "ppjDR086",
        teamName = "Manchester Utd"
    )
    val sampleMatches = listOf(
        Matche(
            id = "match1",
            utcDate = "2026-05-24T15:00:00Z",
            status = "FINISHED",
            competition = com.example.soccerworld.model.fixture.Competition(name = "Premier League"),
            homeTeam = com.example.soccerworld.model.fixture.HomeTeam(id = "ppjDR086", name = "Manchester Utd"),
            awayTeam = com.example.soccerworld.model.fixture.AwayTeam(id = "liv", name = "Liverpool"),
            score = com.example.soccerworld.model.fixture.Score(
                fullTime = com.example.soccerworld.model.fixture.FullTime(home = 3, away = 1)
            )
        ),
        Matche(
            id = "match2",
            utcDate = "2026-05-28T19:00:00Z",
            status = "SCHEDULED",
            competition = com.example.soccerworld.model.fixture.Competition(name = "Premier League"),
            homeTeam = com.example.soccerworld.model.fixture.HomeTeam(id = "ars", name = "Arsenal"),
            awayTeam = com.example.soccerworld.model.fixture.AwayTeam(id = "ppjDR086", name = "Manchester Utd"),
            score = null
        )
    )
    val sampleState = PlayerDetailUiState(
        isLoadingMatches = false,
        matches = sampleMatches
    )
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayerMatchesTab(
                uiState = sampleState,
                playerData = sampleData
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerCareerTabPreview() {
    val sampleRows = listOf(
        CareerRow(
            seasonLabel = "25/26",
            teamId = "ppjDR086",
            teamName = "Manchester Utd",
            tournamentName = "Premier League",
            tournamentFlagId = 198,
            stats = mapOf(
                "4" to "35", // Apps
                "1" to "9",  // Goals
                "8" to "21", // Assists
                "2" to "5",  // Yellow
                "3" to "0",  // Red
                "13" to "8.0" // Rating
            )
        ),
        CareerRow(
            seasonLabel = "24/25",
            teamId = "ppjDR086",
            teamName = "Manchester Utd",
            tournamentName = "Premier League",
            tournamentFlagId = 198,
            stats = mapOf(
                "4" to "36",
                "1" to "8",
                "8" to "10",
                "2" to "5",
                "3" to "2",
                "13" to "7.3"
            )
        )
    )
    val sampleTabs = listOf(
        CareerTabBlock(
            tabId = 1,
            tabLabel = "League",
            tabType = 1,
            rows = sampleRows
        )
    )
    val sampleState = PlayerDetailUiState(
        isLoadingCareer = false,
        careerTabs = sampleTabs
    )
    SoccerWorldTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayerCareerTab(uiState = sampleState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerDetailScreenPreview() {
    val samplePlayerInfo = PlayerDetailInfo(
        id = "WGOY4FSt",
        name = "Cristiano Ronaldo",
        position = "Forward",
        dateOfBirth = "1985-02-05",
        nationality = "Portugal",
        jerseyNumber = 7,
        imageUrl = "https://www.flashscore.com/res/image/data/nsF9bZdM-bTK8dxEL.png",
        flagId = null
    )
    SoccerWorldTheme {
        PlayerDetailScreen(
            playerInfo = samplePlayerInfo,
            onBack = {}
        )
    }
}
