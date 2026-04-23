package com.example.soccerworld.ui.fixture.detail.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
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

    fun getFixtureStatistics(fixtureId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getFixtureStatistics(fixtureId)) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, statistics = result.data) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải thống kê trận đấu") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}