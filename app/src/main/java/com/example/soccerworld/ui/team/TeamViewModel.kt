package com.example.soccerworld.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.model.team.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Gói trạng thái giao diện (UiState Pattern)
data class TeamUiState(
    val isLoading: Boolean = true,
    // LƯU Ý 1: Thay chữ 'Any' bằng Data Class chứa thông tin đội bóng của bạn (Ví dụ: Team)
    val teamsList: List<Team?> = emptyList(),
    val error: String? = null
)

class TeamViewModel(private val repository: FootballRepository) : ViewModel() {

    // 3. Đường ống StateFlow (Thay thế hoàn toàn LiveData)
    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState = _uiState.asStateFlow()

    // 🌟 LƯU Ý 2: Đổi leagueId từ Int thành String
    fun getAllTeamsOfLeague(leagueId: String) {
        viewModelScope.launch {
            // Báo cho UI biết là đang tải dữ liệu
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi API lấy danh sách đội bóng trong giải
                val response = repository.getAllTeamsOfLeague(leagueId)

                // Bóc tách mảng dữ liệu đội bóng
                // LƯU Ý 3: Chấm đúng vào mảng danh sách đội bóng (thường là '.teams')
                // Chốt chặn ?: emptyList() để chống null an toàn
                val data = response.teams ?: emptyList()

                // Thành công: Gửi danh sách lên UI, tắt loading
                _uiState.update { it.copy(isLoading = false, teamsList = data) }

            } catch (e: Exception) {
                // Thất bại: Ghi nhận lỗi thay vì bỏ trống
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tải danh sách đội bóng: ${e.message}") }
            }
        }
    }
}