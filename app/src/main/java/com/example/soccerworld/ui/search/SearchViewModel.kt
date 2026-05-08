package com.example.soccerworld.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.remote.flashlive.SearchItemDto
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val results: List<SearchItemDto> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: FootballRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .distinctUntilChanged()
                .filter { it.length >= 2 || it.isEmpty() }
                .collectLatest { query ->
                    if (query.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, results = emptyList(), error = null) }
                        return@collectLatest
                    }
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    when (val result = repository.multiSearch(query)) {
                        is DataResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    results = result.data.distinctBy { item -> "${item.type}_${item.id}" },
                                    error = null
                                )
                            }
                        }
                        is DataResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = result.message) }
                        }
                        DataResult.Loading -> {}
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        _searchQuery.value = query
    }
}
