package com.example.soccerworld.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
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

    init {
        getAllTeamsOfLeague()
    }

    // 🌟 LƯU Ý 2: Đổi leagueId từ Int thành String
    fun getAllTeamsOfLeague() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val leagueId = repository.getSelectedLeagueId()

            when (val result = repository.getAllTeamsOfLeague(leagueId)) {
                is DataResult.Success -> {
                    val data = result.data.teams ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, teamsList = data) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải danh sách đội bóng") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}