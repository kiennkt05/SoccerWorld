package com.example.soccerworld.ui.home.leaguetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
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
            _uiState.update { it.copy(isLoading = true, error = null) }

            val leagueId = repository.getSelectedLeagueId()
            when (val result = repository.getLeagueTable(leagueId)) {
                is DataResult.Success -> {
                    val data = result.data.standings?.firstOrNull()?.table ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, tableList = data) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải bảng xếp hạng") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}