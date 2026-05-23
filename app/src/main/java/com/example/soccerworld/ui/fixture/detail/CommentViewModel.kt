package com.example.soccerworld.ui.fixture.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.model.comment.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CommentUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

class CommentViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun loadComments(fixtureId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        // Realtime listener
        db.collection("comments")
            .whereEqualTo("fixtureId", fixtureId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Cannot load comments: ${error.message}"
                    )
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    comments = comments,
                    isLoading = false,
                    error = null
                )
            }
    }

    fun postComment(fixtureId: String, text: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: run { onResult(false); return }
        if (text.isBlank()) { onResult(false); return }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            try {
                val displayName = user.displayName?.ifBlank { null }
                    ?: user.email?.substringBefore("@")
                    ?: "Anonymous"

                val comment = hashMapOf(
                    "fixtureId" to fixtureId,
                    "userId" to user.uid,
                    "userDisplayName" to displayName,
                    "text" to text.trim(),
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("comments").add(comment).await()
                _uiState.value = _uiState.value.copy(isSending = false)
                onResult(true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = "Failed to send: ${e.message}"
                )
                onResult(false)
            }
        }
    }

    fun deleteComment(commentId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("comments").document(commentId).get().await()
                if (doc.getString("userId") == userId) {
                    db.collection("comments").document(commentId).delete().await()
                }
            } catch (e: Exception) {
                // Ignore delete errors silently
            }
        }
    }
}
