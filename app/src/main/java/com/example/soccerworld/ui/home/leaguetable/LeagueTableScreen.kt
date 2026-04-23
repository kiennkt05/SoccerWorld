package com.example.soccerworld.ui.home.leaguetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soccerworld.R
import com.example.soccerworld.model.leaguetable.Table
import com.example.soccerworld.model.leaguetable.Team
import com.example.soccerworld.util.Injection
import com.example.soccerworld.util.ViewModelFactory

// ==========================================
// 1. HÀM STATEFUL (Dùng để chạy thật trên máy)
// ==========================================
@Composable
fun LeagueTableScreen() {
    val context = LocalContext.current

    val viewModel: LeagueTableViewModel = viewModel(factory = ViewModelFactory(
            Injection.provideFootballRepository(
                context
            )
        )
    )

    val state by viewModel.uiState.collectAsState()

    // Chỉ gọi hàm Stateless và truyền cục State vào
    LeagueTableContent(state = state)
}

// ==========================================
// 2. HÀM STATELESS (Dùng để vẽ giao diện và Preview)
// ==========================================
@Composable
fun LeagueTableContent(state: LeagueTableUiState) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error, color = Color.Red)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 🌟 AN TOÀN: Dùng ?: emptyList() để lỡ danh sách bị null thì gán thành mảng rỗng
                val safeList = state.tableList ?: emptyList()

                items(safeList.size) { index ->
                    val item = safeList[index]

                    // 🌟 AN TOÀN: Kiểm tra lỡ phần tử bị null thì bỏ qua không vẽ
                    if (item != null) {
                        TeamRow(item)
                    }
                }
            }
        }
    }
}

// ==========================================
// HÀM TEAM ROW (Đã khử sạch !! và thêm bảo vệ)
// ==========================================
@Composable
fun TeamRow(item: Table) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Thứ hạng
            Text(
                text = "${item.position}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )

            // 2. Logo đội bóng (Dùng ?.crest an toàn)
            AsyncImage(
                model = item.team?.crest,
                contentDescription = "Logo of ${item.team?.name ?: "Unknown"}",
                modifier = Modifier.size(36.dp),
                placeholder = painterResource(id = R.drawable.ic_ball),
                error = painterResource(id = R.drawable.ic_ball),
                fallback = painterResource(id = R.drawable.ic_ball)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Tên đội bóng (Dùng ?: "Unknown" thay vì let cho code phẳng và đẹp hơn)
            Text(
                text = item.team?.name ?: "Unknown",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            // 4. Số trận
            Text(
                text = "${item.playedGames}",
                modifier = Modifier.width(28.dp),
                color = Color.DarkGray
            )

            // 5. Hiệu số
            Text(
                text = "${item.goalDifference}",
                modifier = Modifier.width(32.dp),
                color = Color.Gray
            )

            // 6. Điểm số
            Text(
                text = "${item.points}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.width(32.dp)
            )
        }
    }
}

// ==========================================
// 3. KHU VỰC PREVIEW (Chỉ chạy trong Android Studio)
// ==========================================
@Preview(showBackground = true, name = "Thành công - Có dữ liệu")
@Composable
fun PreviewLeagueTableSuccess() {
    val fakeData = listOf(
        Table(
            position = 1, team = Team(
                id = "57",
                name = "Arsenal FC",
                crest = "https://crests.football-data.org/57.png"
            ), playedGames = 31, points = 70, goalDifference = 39
        ),
        Table(position = 2, team = Team(id = "65", name = "Manchester City", crest = "https://crests.football-data.org/65.png"), playedGames = 30, points = 61, goalDifference = 32),
        Table(
            position = 3,
            team = Team(id = "66", name = "Manchester United", crest = "https://crests.football-data.org/66.png"),
            playedGames = 31,
            points = 55,
            goalDifference = 13
        )
    )

    val fakeState = LeagueTableUiState(
        isLoading = false,
        tableList = fakeData,
        error = null
    )

    MaterialTheme {
        LeagueTableContent(state = fakeState)
    }
}

@Preview(showBackground = true, name = "Đang tải dữ liệu")
@Composable
fun PreviewLeagueTableLoading() {
    MaterialTheme {
        LeagueTableContent(state = LeagueTableUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "Lỗi mạng")
@Composable
fun PreviewLeagueTableError() {
    MaterialTheme {
        LeagueTableContent(state = LeagueTableUiState(isLoading = false, error = "Không có kết nối Internet!"))
    }
}