package com.example.soccerworld.ui.fixture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Tạo hộp chứa trạng thái (UiState)
data class FixtureUiState(
    val isLoading: Boolean = true,
    // LƯU Ý 1: Thay 'Any' bằng Data Class chứa 1 trận đấu của bạn (Ví dụ: Match hoặc Fixture)
    val fixtureList: List<Any> = emptyList(),
    val error: String? = null
)

class FixtureViewModel(private val repository: FootballRepository) : ViewModel() {


    // 3. Ống nước StateFlow thay cho LiveData
    private val _uiState = MutableStateFlow(FixtureUiState())
    val uiState = _uiState.asStateFlow()

    // 🌟 LƯU Ý 2: Đổi leagueId từ Int thành String
    fun getAllFixtureOfLeague(leagueId: String) {
        viewModelScope.launch {
            // Bật cờ loading
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi API lấy lịch thi đấu
                val response = repository.getAllFixtureOfLeague(leagueId)

                // 🌟 LƯU Ý 3: Bóc tách mảng dữ liệu.
                // Ở API v4, danh sách trận đấu thường nằm trong mảng 'matches'.
                // Bạn hãy gõ 'response.' và chọn đúng tên mảng nhé, kèm theo ?: emptyList() để an toàn.
                val data = response.matches ?: emptyList()

                // Thành công: tắt loading, cập nhật list
                _uiState.update { it.copy(isLoading = false, fixtureList = data) }

            } catch (e: Exception) {
                // Thất bại: báo lỗi
                _uiState.update { it.copy(isLoading = false, error = "Lỗi mạng: ${e.message}") }
            }
        }
    }
}