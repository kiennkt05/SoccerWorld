package com.example.soccerworld.service

import android.util.Log
import com.example.soccerworld.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Xử lý FCM messages khi app đang chạy (foreground) hoặc background.
 *
 * Với data-only message:
 *   - App foreground  → onMessageReceived() được gọi → hiển thị local notification
 *   - App background  → onMessageReceived() được gọi → hiển thị local notification
 *   - App bị kill     → onMessageReceived() được gọi khi app mở lại
 *
 * Với notification message (có "notification" field):
 *   - App foreground  → onMessageReceived() được gọi
 *   - App background/killed → FCM tự hiển thị system notification
 */
class SoccerWorldMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Gọi khi nhận được FCM message.
     * Chúng ta dùng data-only message để có toàn quyền kiểm soát hiển thị.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "FCM received from: ${message.from}")

        val data = message.data

        if (data.isEmpty()) {
            // Notification-only message (ít dùng) – log và bỏ qua
            message.notification?.let {
                Log.d(TAG, "Notification: ${it.title} – ${it.body}")
            }
            return
        }

        Log.d(TAG, "Data payload: $data")

        // Dispatch theo loại notification
        when (data["type"]) {
            "MATCH_REMINDER" -> handleMatchReminder(data)
            "LIVE_SCORE"     -> handleLiveScore(data)
            "MATCH_RESULT"   -> handleMatchResult(data)
            "NEW_COMMENT"    -> handleNewComment(data)
            else             -> Log.w(TAG, "Unknown notification type: ${data["type"]}")
        }
    }

    /**
     * Gọi khi FCM cấp token mới (thiết bị đăng ký lại).
     * Re-subscribe tất cả topics dựa theo cài đặt người dùng.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        // Re-subscribe topics theo preferences hiện tại
        val prefs = com.example.soccerworld.util.CustomSharedPreferences.invoke(applicationContext)
        if (prefs.isMatchReminderEnabled()) {
            com.example.soccerworld.util.FcmTopicManager
                .subscribeToTopic(com.example.soccerworld.util.FcmTopicManager.TOPIC_MATCH_REMINDER)
        }
        if (prefs.isLiveScoreEnabled()) {
            com.example.soccerworld.util.FcmTopicManager
                .subscribeToTopic(com.example.soccerworld.util.FcmTopicManager.TOPIC_LIVE_SCORE)
        }
        if (prefs.isMatchResultEnabled()) {
            com.example.soccerworld.util.FcmTopicManager
                .subscribeToTopic(com.example.soccerworld.util.FcmTopicManager.TOPIC_MATCH_RESULT)
        }

        // Re-subscribe to comment topics for all favorite matches
        val db = com.example.soccerworld.data.local.FootballDatabase.invoke(applicationContext)
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val favorites = db.footballDao().getAllFavorites().first()
                favorites.forEach { fav ->
                    com.example.soccerworld.util.FcmTopicManager.subscribeToTopic("comment_match_${fav.matchId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to re-subscribe to favorite matches comment topics on token change", e)
            }
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private fun handleMatchReminder(data: Map<String, String>) {
        NotificationHelper.sendMatchReminderNotification(
            context     = applicationContext,
            matchId     = data["matchId"] ?: return,
            homeTeam    = data["homeTeam"] ?: "Home",
            awayTeam    = data["awayTeam"] ?: "Away",
            minutesLeft = data["minutesLeft"]?.toIntOrNull() ?: 15
        )
    }

    private fun handleLiveScore(data: Map<String, String>) {
        NotificationHelper.sendLiveScoreNotification(
            context   = applicationContext,
            matchId   = data["matchId"] ?: return,
            homeTeam  = data["homeTeam"] ?: "Home",
            awayTeam  = data["awayTeam"] ?: "Away",
            homeScore = data["homeScore"]?.toIntOrNull() ?: 0,
            awayScore = data["awayScore"]?.toIntOrNull() ?: 0
        )
    }

    private fun handleMatchResult(data: Map<String, String>) {
        NotificationHelper.sendMatchResultNotification(
            context   = applicationContext,
            matchId   = data["matchId"] ?: return,
            homeTeam  = data["homeTeam"] ?: "Home",
            awayTeam  = data["awayTeam"] ?: "Away",
            homeScore = data["homeScore"]?.toIntOrNull() ?: 0,
            awayScore = data["awayScore"]?.toIntOrNull() ?: 0,
            winner    = data["winner"]?.ifEmpty { null }
        )
    }

    private fun handleNewComment(data: Map<String, String>) {
        NotificationHelper.sendCommentNotification(
            context       = applicationContext,
            matchId       = data["matchId"] ?: return,
            homeTeam      = data["homeTeam"] ?: "Home",
            awayTeam      = data["awayTeam"] ?: "Away",
            commenterName = data["commenterName"] ?: "Người dùng",
            commentText   = data["commentText"] ?: ""
        )
    }
}
