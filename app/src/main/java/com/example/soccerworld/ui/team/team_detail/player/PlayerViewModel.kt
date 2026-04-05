package com.example.soccerworld.ui.team.team_detail.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.model.player.Squad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Tạo hộp trạng thái an toàn
data class PlayerUiState(
    val isLoading: Boolean = true,
    // LƯU Ý 1: Đổi 'Any' thành class chứa thông tin cầu thủ (ví dụ: Player)
    val playerList: List<Squad?> = emptyList(),
    val error: String? = null
)

class PlayerViewModel(private val repository: FootballRepository) : ViewModel() {


    // 3. Đường ống StateFlow (Thay thế hoàn toàn LiveData)
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    fun getAllPlayersOfTeam(teamId: Int) {
        viewModelScope.launch {
            // Báo cho UI biết là bắt đầu quay vòng tải dữ liệu
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi API lấy thông tin đội bóng
                val response = repository.getAllPlayersOfTeam(teamId)

                // 🌟 LƯU Ý CỰC KỲ QUAN TRỌNG:
                // Đối với API Football-Data v4, danh sách cầu thủ của một đội
                // thường nằm trong mảng tên là 'squad' (đội hình), chứ không phải 'players'.
                // Bạn hãy gõ 'response.' và chọn '.squad' nhé.
                // Đồng thời, chốt chặn ?: emptyList() để chống null!
                val data = response.squad ?: emptyList() // <--- Sửa chữ squad nếu Model của bạn tên khác

                // Thành công: Gửi danh sách cầu thủ lên UI
                _uiState.update { it.copy(isLoading = false, playerList = data) }

            } catch (e: Exception) {
                // 🌟 Thất bại: Xử lý lỗi chuẩn mực, không dùng TODO()
                // Báo lỗi bằng chữ đàng hoàng lên màn hình thay vì làm văng app
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tải cầu thủ: ${e.message}") }
            }
        }
    }
}