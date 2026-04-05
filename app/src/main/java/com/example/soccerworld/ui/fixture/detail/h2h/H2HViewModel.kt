package com.example.soccerworld.ui.fixture.detail.h2h

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Gói trạng thái giao diện (UiState Pattern)
data class H2HUiState(
    val isLoading: Boolean = true,
    // LƯU Ý: Thay 'Any' bằng class Model chứa thông tin 1 trận đấu (ví dụ: Match/Fixture)
    val h2hList: List<Any> = emptyList(),
    val error: String? = null
)

class H2HViewModel(private val repository: FootballRepository) : ViewModel() {

    // 3. Khởi tạo StateFlow thay thế hoàn toàn cho MutableLiveData
    private val _uiState = MutableStateFlow(H2HUiState())
    val uiState = _uiState.asStateFlow()

    // 🌟 THAY ĐỔI LỚN: Nhận vào fixtureId thay vì homeTeamId/awayTeamId
    fun getHeadToHead(fixtureId: Int) {
        viewModelScope.launch {
            // Bật trạng thái loading
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi API lấy lịch sử đối đầu
                val response = repository.getAllH2hItems(fixtureId)

                // Bóc tách JSON: Lấy mảng các trận đấu (matches)
                // LƯU Ý: Chỉnh chữ '.matches' cho khớp với tên biến trong Model H2HResponse của bạn
                // Dùng ?: emptyList() để an toàn tuyệt đối nếu API không trả về mảng nào
                val data = response.matches ?: emptyList()

                // Thành công: Cập nhật dữ liệu lên UI
                _uiState.update { it.copy(isLoading = false, h2hList = data) }

            } catch (e: Exception) {
                // Thất bại: Báo lỗi
                _uiState.update { it.copy(isLoading = false, error = "Lỗi mạng: ${e.message}") }
            }
        }
    }
}