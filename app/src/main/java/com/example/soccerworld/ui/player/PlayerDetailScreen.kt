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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.soccerworld.R
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory
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

    val tabs = listOf("Details", "Matches", "Career")
    var selectedTab by remember { mutableStateOf(0) }

    val positionColor = remember(playerInfo.position) {
        when (playerInfo.position) {
            "Goalkeeper" -> Color(0xFFFFB300)
            "Defender"   -> Color(0xFF1E88E5)
            "Midfielder" -> Color(0xFF43A047)
            "Forward"    -> Color(0xFFE53935)
            else         -> Color(0xFF546E7A)
        }
    }

    val positionAbbr = remember(playerInfo.position) {
        when (playerInfo.position) {
            "Goalkeeper" -> "GK"; "Defender" -> "CB"
            "Midfielder" -> "MF"; "Forward"  -> "FW"
            "Coach"      -> "HC"
            else -> playerInfo.position?.take(2)?.uppercase() ?: "?"
        }
    }

    LaunchedEffect(playerInfo.teamId) {
        playerInfo.teamId?.let { viewModel.loadTeamMatches(it) }
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
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FavoriteBorder, "Favorite", tint = Color.White)
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
                                Color(0xFF374DF5).copy(alpha = 0.8f) // Sofascore Blue (translucent bottom)
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
                            model = ImageRequest.Builder(context).data(playerInfo.imageUrl).crossfade(true).build(),
                            contentDescription = playerInfo.name,
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
                            text = playerInfo.name,
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
                            if ((playerInfo.jerseyNumber ?: 0) > 0) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.15f)) {
                                    Text(
                                        text = "#${playerInfo.jerseyNumber}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        if (!playerInfo.nationality.isNullOrBlank()) {
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
                    0 -> DetailsTab(playerInfo, positionColor)
                    1 -> MatchesTab(uiState)
                    2 -> CareerTab(playerInfo)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1 — DETAILS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DetailsTab(playerInfo: PlayerDetailInfo, positionColor: Color) {
    val attributes = remember(playerInfo.position) {
        when (playerInfo.position) {
            "Goalkeeper" -> linkedMapOf("ATT" to 28, "TEC" to 52, "DEF" to 88, "TAC" to 72, "CRE" to 42)
            "Defender"   -> linkedMapOf("ATT" to 38, "TEC" to 62, "DEF" to 82, "TAC" to 76, "CRE" to 44)
            "Midfielder" -> linkedMapOf("ATT" to 66, "TEC" to 76, "DEF" to 62, "TAC" to 74, "CRE" to 70)
            "Forward"    -> linkedMapOf("ATT" to 83, "TEC" to 72, "DEF" to 34, "TAC" to 54, "CRE" to 62)
            else         -> linkedMapOf("ATT" to 55, "TEC" to 60, "DEF" to 60, "TAC" to 60, "CRE" to 55)
        }
    }
    val strengths = remember(playerInfo.position) {
        when (playerInfo.position) {
            "Goalkeeper" -> listOf("Shot stopping", "Distribution", "Positioning")
            "Defender"   -> listOf("Tackling", "Aerial duels", "Positioning")
            "Midfielder" -> listOf("Passing", "Ball control", "Vision")
            "Forward"    -> listOf("Finishing", "Movement", "Hold-up play")
            else         -> listOf("Work rate", "Teamwork")
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Attribute Overview (radar + labels) ──────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    "Attribute Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                // Radar chart chiếm toàn bộ chiều rộng card, labels tự nằm ở các đỉnh
                AttributeRadarChart(
                    attributes = attributes,
                    color = positionColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }

        // ── Player Positions ─────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    "Player Positions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniFootballPitch(
                        position = playerInfo.position,
                        positionColor = positionColor,
                        modifier = Modifier.size(width = 108.dp, height = 158.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Strengths",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2EA64F)
                        )
                        Spacer(Modifier.height(6.dp))
                        strengths.forEach { s ->
                            Text(
                                "• $s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Weaknesses",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "No outstanding weaknesses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 2 — MATCHES
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MatchesTab(uiState: PlayerDetailUiState) {
    when {
        uiState.isLoadingMatches -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        uiState.matchesError != null && uiState.matches.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚽", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.matchesError ?: "No matches found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        uiState.matches.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("No matches available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else -> {
            var activeFilter by remember { mutableStateOf("All") }
            val filters = listOf("All", "Finished", "Scheduled")

            val filteredMatches = remember(uiState.matches, activeFilter) {
                when (activeFilter) {
                    "Finished"  -> uiState.matches.filter { it.status == "FINISHED" }
                    "Scheduled" -> uiState.matches.filter { it.status == "SCHEDULED" || it.status == "TIMED" }
                    else        -> uiState.matches
                }.sortedByDescending { it.utcDate.orEmpty() }
            }

            val grouped = remember(filteredMatches) {
                val map = linkedMapOf<String, MutableList<Matche>>()
                filteredMatches.forEach { m ->
                    val key = m.competition?.name ?: "Tournament"
                    map.getOrPut(key) { mutableListOf() }.add(m)
                }
                map
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filters.forEach { f ->
                        val selected = activeFilter == f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                                    else Color.Transparent
                                )
                                .clickable { activeFilter = f },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                f,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (grouped.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No $activeFilter matches", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                modifier = Modifier.size(20.dp).clip(CircleShape),
                                                placeholder = painterResource(R.drawable.ic_ball),
                                                error = painterResource(R.drawable.ic_ball),
                                                fallback = painterResource(R.drawable.ic_ball),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                compName,
                                                style = MaterialTheme.typography.titleSmall,
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
                                            PlayerMatchRow(match)
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
private fun PlayerMatchRow(match: Matche) {
    val dateStr = remember(match.utcDate) { formatMatchDate(match.utcDate) }
    val timeStr = remember(match.utcDate) { formatMatchTime(match.utcDate) }
    val isFinished = match.status == "FINISHED"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dateStr, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            Spacer(Modifier.height(2.dp))
            Text(
                if (isFinished) "FT" else timeStr,
                style = MaterialTheme.typography.labelSmall, fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFinished) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primary
            )
        }
        Box(modifier = Modifier.padding(horizontal = 8.dp).width(0.7.dp).height(34.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                AsyncImage(model = match.homeTeam?.crest ?: R.drawable.ic_ball, contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    placeholder = painterResource(R.drawable.ic_ball), error = painterResource(R.drawable.ic_ball),
                    fallback = painterResource(R.drawable.ic_ball))
                Spacer(Modifier.width(6.dp))
                Text(match.homeTeam?.name ?: "Home", style = MaterialTheme.typography.bodySmall, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                AsyncImage(model = match.awayTeam?.crest ?: R.drawable.ic_ball, contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    placeholder = painterResource(R.drawable.ic_ball), error = painterResource(R.drawable.ic_ball),
                    fallback = painterResource(R.drawable.ic_ball))
                Spacer(Modifier.width(6.dp))
                Text(match.awayTeam?.name ?: "Away", style = MaterialTheme.typography.bodySmall, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f))
            }
        }
        if (isFinished) {
            val homeScore = match.score?.fullTime?.home
            val awayScore = match.score?.fullTime?.away
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 6.dp)) {
                Text(homeScore?.toString() ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 2.dp))
                Text(awayScore?.toString() ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3 — CAREER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CareerTab(playerInfo: PlayerDetailInfo) {
    val age = remember(playerInfo.dateOfBirth) {
        playerInfo.dateOfBirth?.take(10)?.let { dob ->
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val birthDate = sdf.parse(dob)
                if (birthDate != null) {
                    val birthCal = Calendar.getInstance().apply { time = birthDate }
                    val todayCal = Calendar.getInstance()
                    var calculatedAge = todayCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
                    if (todayCal.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                        calculatedAge--
                    }
                    calculatedAge
                } else null
            } catch (e: Exception) { null }
        }
    }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                CareerInfoRow("👤", "Full Name", playerInfo.name)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                CareerInfoRow("⚽", "Position", playerInfo.position ?: "—")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                CareerInfoRow("🎂", "Date of Birth", playerInfo.dateOfBirth?.take(10) ?: "—")
                if (age != null) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    CareerInfoRow("🎯", "Age", "$age years old")
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                CareerInfoRow("🌍", "Nationality", playerInfo.nationality ?: "—")
                if ((playerInfo.jerseyNumber ?: 0) > 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    CareerInfoRow("👕", "Jersey Number", "#${playerInfo.jerseyNumber}")
                }
            }
        }

        if (!playerInfo.nationality.isNullOrBlank()) {
            Text("National Team", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ctx = LocalContext.current
                    val flagResId = remember(playerInfo.flagId) {
                        val fid = playerInfo.flagId
                        if (fid != null && fid > 0) {
                            val r = ctx.resources.getIdentifier("country_flag_$fid", "drawable", ctx.packageName)
                            if (r != 0) r else null
                        } else null
                    }
                    if (flagResId != null) {
                        Image(
                            painter = painterResource(flagResId),
                            contentDescription = playerInfo.nationality,
                            modifier = Modifier.size(width = 44.dp, height = 30.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(44.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("🌍", fontSize = 20.sp) }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(playerInfo.nationality, style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold)
                        Text("National team", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("DEBUT" to "—", "APPEARANCES" to "—", "GOALS" to "—").forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CareerInfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp, modifier = Modifier.width(32.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
        // Label orbit: 80% của bán kính nhỏ nhất
        val labelR = minOf(cx, cy) * 0.82f
        // Half-size của mỗi label (để căn giữa)
        val labelHalfW = 22f

        fun angle(i: Int): Double = 2.0 * PI * i / n - PI / 2

        // ── Canvas vẽ ngũ giác ─────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pcx = size.width / 2f
            val pcy = size.height / 2f
            // chart radius ở 50% để để chỗ cho label ngoài rìa
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

            // Polygon giá trị (tô màu + viền)
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

        // ── Overlay label tại từng đỉnh ──────────────────────────────────
        entries.forEachIndexed { i, (label, score) ->
            val ang = angle(i)
            val lx = cx + labelR * cos(ang).toFloat()
            val ly = cy + labelR * sin(ang).toFloat()

            val badgeColor = when {
                score >= 75 -> Color(0xFF2EA64F)   // xanh lá tốt
                score >= 55 -> Color(0xFFF4A300)   // cam trung bình
                else        -> Color(0xFFE53935)   // đỏ thấp
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
                // Score badge
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
                // Tên thuộc tính
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
    val dotY = when (position) {
        "Goalkeeper" -> 0.88f
        "Defender"   -> 0.72f
        "Midfielder" -> 0.50f
        "Forward"    -> 0.22f
        else         -> 0.50f
    }

    Canvas(modifier = modifier.background(pitchGreen, RoundedCornerShape(10.dp))) {
        val w = size.width; val h = size.height; val sw = 1.5f * density
        // Outer
        drawRect(lineColor, topLeft = Offset(4f * density, 4f * density),
            size = androidx.compose.ui.geometry.Size(w - 8f * density, h - 8f * density), style = Stroke(sw))
        // Center line
        drawLine(lineColor, Offset(4f * density, h / 2f), Offset(w - 4f * density, h / 2f), sw)
        // Center circle
        drawCircle(lineColor, radius = w * 0.16f, center = Offset(w / 2f, h / 2f), style = Stroke(sw))
        drawCircle(lineColor, radius = 2.5f * density, center = Offset(w / 2f, h / 2f))
        // Penalty boxes
        val pbW = w * 0.56f; val pbH = h * 0.18f
        drawRect(lineColor, topLeft = Offset((w - pbW) / 2f, 4f * density),
            size = androidx.compose.ui.geometry.Size(pbW, pbH), style = Stroke(sw))
        drawRect(lineColor, topLeft = Offset((w - pbW) / 2f, h - 4f * density - pbH),
            size = androidx.compose.ui.geometry.Size(pbW, pbH), style = Stroke(sw))
        // Player dot
        val px = w * dotX; val py = h * dotY
        drawCircle(positionColor, radius = 10f * density, center = Offset(px, py))
        drawCircle(Color.White, radius = 3.5f * density, center = Offset(px, py))
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────
private fun formatMatchDate(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
        val out = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        inp.parse(utcDate)?.let { out.format(it) } ?: ""
    } catch (e: Exception) { utcDate.take(10) }
}

private fun formatMatchTime(utcDate: String?): String {
    if (utcDate.isNullOrEmpty()) return ""
    return try {
        val inp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
        val out = SimpleDateFormat("HH:mm", Locale.getDefault())
        inp.parse(utcDate)?.let { out.format(it) } ?: ""
    } catch (e: Exception) { "" }
}

@Suppress("UNUSED_PARAMETER")
private fun Modifier.tabIndicatorOffset(tabPosition: TabPosition): Modifier =
    this.fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = tabPosition.left)
        .width(tabPosition.width)
