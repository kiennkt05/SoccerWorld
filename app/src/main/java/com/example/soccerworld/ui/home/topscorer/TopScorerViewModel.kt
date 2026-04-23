    package com.example.soccerworld.ui.home.topscorer

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.soccerworld.data.FootballRepository
    import com.example.soccerworld.data.model.DataResult
    import com.example.soccerworld.model.topscorer.TopScorerEntity
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.update
    import kotlinx.coroutines.launch

    // 1. Gói trạng thái giao diện (Giống hệt bên Bảng xếp hạng)
    data class TopScorerUiState(
        val isLoading: Boolean = true,
        // LƯU Ý: Sửa Any thành TopScorerEntity hoặc Model tương ứng của bạn
        val topScorerList: List<TopScorerEntity> = emptyList(),
        val playerImageUrls: Map<String, String?> = emptyMap(),
        val error: String? = null
    )

    // Kế thừa AndroidViewModel để lấy được context của Application (dùng cho Room và SharedPreferences)
    class TopScorerViewModel(private val repository: FootballRepository) : ViewModel() {


        // 3. Ống nước StateFlow
        private val _uiState = MutableStateFlow(TopScorerUiState())
        val uiState = _uiState.asStateFlow()

        init {
            getTopScorers()
        }

        fun getTopScorers() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val leagueId = repository.getSelectedLeagueId()

                when (val result = repository.getTopScorers(leagueId)) {
                    is DataResult.Success -> {
                        val imageUrls = repository.preloadPlayerMediaInParallel(result.data)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                topScorerList = result.data,
                                playerImageUrls = imageUrls
                            )
                        }
                    }
                    is DataResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message ?: "Lỗi tải vua phá lưới") }
                    }
                    DataResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }

    }