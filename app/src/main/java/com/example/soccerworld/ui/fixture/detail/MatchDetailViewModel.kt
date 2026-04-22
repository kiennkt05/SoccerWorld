package com.example.soccerworld.ui.fixture.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchDetailUiState(
    val isLoading: Boolean = true,
    val data: MatchDetailAggregate? = null,
    val error: String? = null
)

class MatchDetailViewModel(
    private val repository: FootballRepository
) : ViewModel() {
    private val tag = "MatchDetailVM"
    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMatchDetail(fixtureId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d(tag, "Loading aggregate for fixtureId=$fixtureId")
            when (val result = repository.getMatchDetailAggregate(fixtureId)) {
                is DataResult.Success -> {
                    Log.d(
                        tag,
                        "Loaded aggregate fixtureId=$fixtureId h2h=${result.data.h2h.size} " +
                            "events=${result.data.enrichment?.events?.size ?: 0} " +
                            "stats=${result.data.enrichment?.stats?.size ?: 0} " +
                            "lineups=${result.data.enrichment?.lineups?.size ?: 0}"
                    )
                    _uiState.update { it.copy(isLoading = false, data = result.data) }
                }
                is DataResult.Error -> {
                    Log.e(tag, "Aggregate load failed fixtureId=$fixtureId type=${result.type} msg=${result.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to load match detail") }
                }
                DataResult.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }
}
