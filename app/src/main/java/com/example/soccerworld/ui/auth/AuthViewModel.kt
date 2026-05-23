package com.example.soccerworld.ui.auth

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Luôn đọc trực tiếp từ FirebaseAuth để tránh vấn đề ViewModel instance khác nhau
    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val webClientId = context.getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun handleGoogleSignInResult(data: Intent?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!, onResult)
            } catch (e: ApiException) {
                parseApiException(e)
                onResult(false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Đăng nhập thất bại: ${e.localizedMessage}"
                )
                onResult(false)
            }
        }
    }

    fun handleGoogleSignInFailure(resultCode: Int, data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                if (data != null) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    task.getResult(ApiException::class.java)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Đăng nhập thất bại hoặc bị hủy (Mã kết quả: $resultCode)"
                )
            } catch (e: ApiException) {
                parseApiException(e)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Đăng nhập thất bại: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun parseApiException(e: ApiException) {
        val errorMsg = when (e.statusCode) {
            10 -> "Lỗi Developer (10): SHA-1 chưa được đăng ký trong Firebase/Google Console.\n\nSHA-1 của máy bạn:\nF6:68:32:30:D5:60:EB:A1:75:2B:8D:C6:3B:4C:96:24:DB:0C:1A:35"
            7 -> "Lỗi Mạng (7): Không thể kết nối Internet. Vui lòng kiểm tra lại kết nối mạng."
            12500 -> "Lỗi cấu hình Google Sign-In (Mã lỗi 12500). Vui lòng cập nhật google-services.json."
            12501 -> "Đăng nhập đã bị hủy bởi người dùng (12501)."
            else -> "Đăng nhập Google thất bại (Mã lỗi ${e.statusCode}): ${e.localizedMessage}"
        }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = errorMsg
        )
    }


    private suspend fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentUser = auth.currentUser
            )
            onResult(true)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Xác thực Firebase thất bại: ${e.message}"
            )
            onResult(false)
        }
    }

    fun logout(context: Context) {
        auth.signOut()
        getGoogleSignInClient(context).signOut()
        _uiState.value = AuthUiState(currentUser = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
