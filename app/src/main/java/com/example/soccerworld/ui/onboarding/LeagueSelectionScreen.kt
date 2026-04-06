package com.example.soccerworld.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.soccerworld.util.CustomSharedPreferences

data class LeagueUI(val id: String, val name: String, val logoUrl: String)

val popularLeagues = listOf(
    LeagueUI("2021", "Premier League", "https://crests.football-data.org/PL.png"),
    LeagueUI("2014", "La Liga", "https://crests.football-data.org/PD.png"),
    LeagueUI("2019", "Serie A", "https://crests.football-data.org/SA.png"),
    LeagueUI("2002", "Bundesliga", "https://crests.football-data.org/BL1.png"),
    LeagueUI("2015", "Ligue 1", "https://crests.football-data.org/FL1.png"),
    LeagueUI("2001", "Champions League", "https://crests.football-data.org/CL.png")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueSelectionScreen(
    onLeagueSelected: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = CustomSharedPreferences.invoke(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn Giải Đấu", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vui lòng chọn giải đấu bạn muốn theo dõi:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(popularLeagues) { league ->
                    LeagueCard(league = league) {
                        // Lưu ID vào SharedPreferences
                        sharedPreferences.saveLeagueId(league.id)
                        // Điều hướng sang màn hình chính
                        onLeagueSelected()
                    }
                }
            }
        }
    }
}

@Composable
fun LeagueCard(league: LeagueUI, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = league.logoUrl,
                contentDescription = league.name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = league.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
