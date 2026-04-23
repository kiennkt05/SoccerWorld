package com.example.soccerworld.ui.fixture.detail.h2h

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.soccerworld.model.h2h.Matche

// 1. Gói trạng thái giao diện (UiState Pattern)
data class H2HUiState(
    val isLoading: Boolean = true,
    // LƯU Ý: Thay 'Any' bằng class Model chứa thông tin 1 trận đấu (ví dụ: Match/Fixture)
    val h2hList: List<Matche> = emptyList(),
    val error: String? = null
)

class H2HViewModel(private val repository: FootballRepository) : ViewModel() {

    // 3. Khởi tạo StateFlow thay thế hoàn toàn cho MutableLiveData
    private val _uiState = MutableStateFlow(H2HUiState())
    val uiState = _uiState.asStateFlow()

    // 🌟 THAY ĐỔI LỚN: Nhận vào fixtureId thay vì homeTeamId/awayTeamId
    fun getHeadToHead(fixtureId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getAllH2hItems(fixtureId)) {
                is DataResult.Success -> {
                    val data = result.data.matches ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, h2hList = data) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải lịch sử đối đầu") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}