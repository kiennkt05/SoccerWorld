package com.example.soccerworld.ui.auth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import java.security.MessageDigest
import java.util.Locale
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

    fun handleGoogleSignInResult(context: Context, data: Intent?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!, onResult)
            } catch (e: ApiException) {
                parseApiException(context, e)
                onResult(false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign in failed: ${e.localizedMessage}"
                )
                onResult(false)
            }
        }
    }

    fun handleGoogleSignInFailure(context: Context, resultCode: Int, data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                if (data != null) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    task.getResult(ApiException::class.java)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign in failed or cancelled (Result code: $resultCode)"
                )
            } catch (e: ApiException) {
                parseApiException(context, e)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign in failed: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun parseApiException(context: Context, e: ApiException) {
        val errorMsg = when (e.statusCode) {
            10 -> {
                val sha1 = getAppSignaturesSHA1(context)
                "Developer Error (10): SHA-1 is not registered in Firebase/Google Console.\n\nYour SHA-1:\n$sha1"
            }
            7 -> "Network Error (7): Cannot connect to the Internet. Please check your network connection."
            12500 -> "Google Sign-In configuration error (Error code 12500). Please update google-services.json."
            12501 -> "Sign-in was cancelled by the user (12501)."
            else -> "Google sign-in failed (Error code ${e.statusCode}): ${e.localizedMessage}"
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
                errorMessage = "Firebase authentication failed: ${e.message}"
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

fun getAppSignaturesSHA1(context: Context): String {
    try {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            packageInfo.signatures
        }

        if (signatures != null && signatures.isNotEmpty()) {
            val cert = signatures[0].toByteArray()
            val md = MessageDigest.getInstance("SHA-1")
            val publicKey = md.digest(cert)
            val hexString = StringBuilder()
            for (i in publicKey.indices) {
                val appendString = Integer.toHexString(0xFF and publicKey[i].toInt()).uppercase(Locale.US)
                if (appendString.length == 1) {
                    hexString.append("0")
                }
                hexString.append(appendString)
                if (i < publicKey.size - 1) {
                    hexString.append(":")
                }
            }
            return hexString.toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "Unknown SHA-1"
}
