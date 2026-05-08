package com.example.soccerworld.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

// Hot search suggestions shown before user types
private val hotSearches = listOf(
    "Arsenal" to Icons.Default.AccountBox,
    "Real Madrid" to Icons.Default.AccountBox,
    "Ronaldo" to Icons.Default.Person,
    "Mbappe" to Icons.Default.Person,
    "Champions League" to Icons.Default.Star,
    "Premier League" to Icons.Default.Star
)

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

    val primary = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Search bar ───────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            TextField(
                value = state.query,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = {
                    Text(
                        "Tìm kiếm đội bóng, cầu thủ, giải đấu...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = primary)
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        TextButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Text("Xóa", color = primary, fontSize = 13.sp)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // ── Body ─────────────────────────────────────────────────────
        when {
            // Loading
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Đang tìm kiếm...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Error
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Lỗi: ${state.error}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }

            // No results
            state.results.isEmpty() && state.query.length >= 2 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Không tìm thấy kết quả cho\n\"${state.query}\"",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Empty query – show suggestions
            state.query.isEmpty() -> {
                SearchSuggestions(
                    onSuggestionClick = { query -> viewModel.onSearchQueryChanged(query) }
                )
            }

            // Results
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

// ── Suggestions page ─────────────────────────────────────────────────────────

@Composable
private fun SearchSuggestions(onSuggestionClick: (String) -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Tìm kiếm phổ biến",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        items(hotSearches) { (label, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSuggestionClick(label) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Nhập tên đội bóng, cầu thủ hoặc giải đấu\nđể bắt đầu tìm kiếm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Result list with section grouping ────────────────────────────────────────

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
            item { GroupHeader(title = "Câu lạc bộ", icon = Icons.Default.AccountBox, count = teams.size) }
            items(teams) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick)
            }
        }

        // Players section
        if (players.isNotEmpty()) {
            item { GroupHeader(title = "Cầu thủ", icon = Icons.Default.Person, count = players.size) }
            items(players) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick)
            }
        }

        // Tournaments section
        if (tournaments.isNotEmpty()) {
            item { GroupHeader(title = "Giải đấu", icon = Icons.Default.Star, count = tournaments.size) }
            items(tournaments) { item ->
                SearchItemCard(item = item, onTeamClick = onTeamClick, onPlayerClick = onPlayerClick)
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String, icon: ImageVector, count: Int) {
    val primary = MaterialTheme.colorScheme.primary
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
            color = MaterialTheme.colorScheme.primaryContainer,
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

// ── Individual result card ────────────────────────────────────────────────────

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
        is TeamSearchItemDto -> "CLB • ${item.countryName ?: "—"}"
        is PlayerSearchItemDto -> "Cầu thủ • ${item.countryName ?: "—"}"
        is TournamentSearchItemDto -> "Giải đấu • ${item.countryName ?: "—"}"
        is UnknownSearchItemDto -> "Không rõ"
    }
    val typeIcon = when (item) {
        is TeamSearchItemDto -> Icons.Default.AccountBox
        is PlayerSearchItemDto -> Icons.Default.Person
        is TournamentSearchItemDto -> Icons.Default.Star
        else -> Icons.Default.Search
    }
    val typeColor = when (item) {
        is TeamSearchItemDto -> MaterialTheme.colorScheme.primary
        is PlayerSearchItemDto -> MaterialTheme.colorScheme.secondary
        is TournamentSearchItemDto -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                when (item) {
                    is TeamSearchItemDto -> onTeamClick(item.id)
                    is PlayerSearchItemDto -> onPlayerClick(item.id)
                    else -> {}
                }
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / crest
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

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Type badge
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
    }
}
