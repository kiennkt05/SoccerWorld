package com.example.soccerworld.ui.home.leaguetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.model.leaguetable.Table
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Khai báo cái hộp chứa trạng thái màn hình
data class LeagueTableUiState(
    val isLoading: Boolean = true,
    // LƯU Ý: Thay 'Any' bằng cái class Table (hoặc StandingItem) mà Plugin sinh ra cho bạn nhé
    val tableList: List<Table?>? = emptyList(),
    val error: String? = null
)

class LeagueTableViewModel(private val repository: FootballRepository) : ViewModel() {


    // 3. Biến StateFlow để Compose theo dõi
    private val _uiState = MutableStateFlow(LeagueTableUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Vừa vào app là gọi mạng luôn
        fetchStandings()
    }

    private fun fetchStandings() {
        viewModelScope.launch {
            try {
                // Đang tải...
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Gọi API lấy Ngoại hạng Anh ("PL")
                val response = repository.getLeagueTable("PL")

                // Bóc tách JSON: Lấy cái mảng table bên trong standings
                // LƯU Ý: Chỉnh lại tên biến cho khớp với Model của bạn
                val data = response.standings?.firstOrNull()?.table ?: emptyList()

                // Thành công: Ném data vào hộp, tắt loading
                _uiState.update { it.copy(isLoading = false, tableList = data) }

            } catch (e: Exception) {
                // Thất bại: Báo lỗi
                _uiState.update { it.copy(isLoading = false, error = "Lỗi mạng: ${e.message}") }
            }
        }
    }
}