package com.example.soccerworld.ui.fixture

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.model.fixture.Matche
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 1. Tạo hộp chứa trạng thái (UiState)
data class FixtureUiState(
    val isLoading: Boolean = true,
    // LƯU Ý 1: Thay 'Any' bằng Data Class chứa 1 trận đấu của bạn (Ví dụ: Match hoặc Fixture)
    val fixtureList: List<Matche> = emptyList(),
    val selectedDate: String = currentDateIso(),
    val availableDates: List<String> = buildDateWindow(),
    val favoriteIds: Set<Int> = emptySet(),
    val hasLiveMatches: Boolean = false,
    val error: String? = null
)

class FixtureViewModel(private val repository: FootballRepository) : ViewModel() {


    // 3. Ống nước StateFlow thay cho LiveData
    private val _uiState = MutableStateFlow(FixtureUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeFavorites()
        getAllFixtureOfLeague()
    }

    // 🌟 LƯU Ý 2: Đổi leagueId từ Int thành String
    fun getAllFixtureOfLeague(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val leagueId = repository.getSelectedLeagueId()
            val selectedDate = _uiState.value.selectedDate

            when (val result = repository.getAllFixtureOfLeague(
                leagueId = leagueId,
                dateFrom = selectedDate,
                dateTo = selectedDate,
                forceRefresh = forceRefresh
            )) {
                is DataResult.Success -> {
                    val data = result.data.matches ?: emptyList()
                    val hasLive = data.any { it.status == "IN_PLAY" || it.status == "PAUSED" }
                    Log.d("FixtureViewModel", "Fixtures refreshed selectedDate=$selectedDate count=${data.size} liveCount=${data.count { it.status == "IN_PLAY" || it.status == "PAUSED" }}")
                    _uiState.update { it.copy(isLoading = false, fixtureList = data, hasLiveMatches = hasLive) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải lịch thi đấu") }
                }
                DataResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onDateSelected(date: String) {
        Log.d("FixtureViewModel", "Date selected: $date")
        _uiState.update { it.copy(selectedDate = date) }
        getAllFixtureOfLeague(forceRefresh = true)
    }

    fun toggleFavorite(match: Matche) {
        viewModelScope.launch {
            repository.toggleFavorite(match)
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                _uiState.update { it.copy(favoriteIds = favorites.map { fav -> fav.matchId }.toSet()) }
            }
        }
    }
}

private fun currentDateIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

private fun buildDateWindow(): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    return (-3..3).map { offset ->
        val temp = cal.clone() as Calendar
        temp.add(Calendar.DATE, offset)
        sdf.format(temp.time)
    }
}