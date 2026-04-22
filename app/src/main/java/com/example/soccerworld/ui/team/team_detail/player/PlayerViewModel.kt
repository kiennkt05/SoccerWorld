package com.example.soccerworld.ui.team.team_detail.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
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
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getAllPlayersOfTeam(teamId)) {
                is DataResult.Success -> {
                    val data = result.data.squad ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, playerList = data) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải cầu thủ") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}