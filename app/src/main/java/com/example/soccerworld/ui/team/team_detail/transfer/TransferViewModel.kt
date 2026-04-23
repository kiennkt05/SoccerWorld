package com.example.soccerworld.ui.team.team_detail.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.FootballRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Tạo hộp trạng thái an toàn tuyệt đối
data class TransferUiState(
    val isLoading: Boolean = true,
    // LƯU Ý: Thay chữ 'Any' bằng Data Class chứa thông tin chuyển nhượng của bạn (VD: Transfer)
    val transferList: List<Any> = emptyList(),
    val error: String? = null
)

class TransferViewModel(private val repository: FootballRepository) : ViewModel() {

    // 3. Đường ống StateFlow (Thay thế hoàn toàn LiveData)
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState = _uiState.asStateFlow()

    fun getAllTransfersOfTeam() {
        viewModelScope.launch {
            // Bật trạng thái loading
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Gọi API lấy thông tin chuyển nhượng
                // API này không có với endpoint hiện tại
//                val response = repository.getAllTransfersOfTeam(teamId)
//
//                // Bóc tách mảng dữ liệu
//                // LƯU Ý: Sửa '.transfers' cho khớp với tên biến trong Model TransferResponse của bạn
//                // Dùng ?: emptyList() để chống sập app nếu không có lịch sử chuyển nhượng
//                val data = response.transfers ?: emptyList()
//
//                // Thành công: Gửi danh sách lên UI, tắt loading
//                _uiState.update { it.copy(isLoading = false, transferList = data) }

            } catch (e: Exception) {
                // Thất bại: Xử lý lỗi đàng hoàng thay vì để trống
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tải dữ liệu chuyển nhượng: ${e.message}") }
            }
        }
    }
}