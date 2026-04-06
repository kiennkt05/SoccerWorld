package com.example.soccerworld.ui.fixture.detail.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.soccerworld.model.statistic.StatisticsResponse

// 1. Tạo gói trạng thái UI (UiState Pattern)
data class StatisticUiState(
    val isLoading: Boolean = true,
    // LƯU Ý 1: Thay chữ 'Any' bằng Data Class Thống kê của bạn (ví dụ: Statistic, hoặc List<Statistic>)
    // Để null an toàn, lỡ trận đấu chưa đá thì không có thống kê
    val statistics: StatisticsResponse? = null,
    val error: String? = null
)

class StatisticViewModel(private val repository: FootballRepository) : ViewModel() {


    // 3. StateFlow thay thế cho LiveData
    private val _uiState = MutableStateFlow(StatisticUiState())
    val uiState = _uiState.asStateFlow()

    fun getFixtureStatistics(fixtureId: Int) {
        viewModelScope.launch {
            // Báo cho UI biết là đang tải dữ liệu
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi API
                val response = repository.getFixtureStatistics(fixtureId)

                // 🌟 LƯU Ý 2: BÓC TÁCH JSON Ở ĐÂY
                // Tùy thuộc vào việc API v4 trả về thống kê dạng mảng (List) hay một Object duy nhất.
                // Bạn hãy gõ chữ 'response.' và chọn đúng trường dữ liệu chứa thống kê nhé.
                // Ví dụ nếu nó là List: val data = response.statistics ?: emptyList()
                // Ví dụ nếu nó là Object: val data = response.statistics
                val data = response // <--- SỬA DÒNG NÀY

                // Thành công, đẩy dữ liệu lên UI
                _uiState.update { it.copy(isLoading = false, statistics = data) }

            } catch (e: Exception) {
                // Thất bại thì báo lỗi đỏ
                _uiState.update { it.copy(isLoading = false, error = "Lỗi mạng: ${e.message}") }
            }
        }
    }
}