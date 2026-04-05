    package com.example.soccerworld.ui.home.topscorer

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.soccerworld.data.FootballRepository
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.flow.update
    import kotlinx.coroutines.launch

    // 1. Gói trạng thái giao diện (Giống hệt bên Bảng xếp hạng)
    data class TopScorerUiState(
        val isLoading: Boolean = true,
        // LƯU Ý: Sửa Any thành TopScorerEntity hoặc Model tương ứng của bạn
        val topScorerList: List<Any> = emptyList(),
        val error: String? = null
    )

    // Kế thừa AndroidViewModel để lấy được context của Application (dùng cho Room và SharedPreferences)
    class TopScorerViewModel(private val repository: FootballRepository) : ViewModel() {


        // 3. Ống nước StateFlow
        private val _uiState = MutableStateFlow(TopScorerUiState())
        val uiState = _uiState.asStateFlow()

        // Lưu ý: Đổi leagueId từ Int thành String (VD: "PL") cho khớp API v4
        fun getTopScorers(leagueId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                try {
                    // VIỆC NHẸ LƯƠNG CAO: Chỉ cần gọi đúng 1 hàm, Repository tự lo mọi thứ bên trong!
                    val data = repository.getTopScorers(leagueId)

                    _uiState.update { it.copy(isLoading = false, topScorerList = data) }

                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Lỗi: ${e.message}") }
                }
            }
        }

    }