package com.example.soccerworld.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Quản lý việc subscribe/unsubscribe FCM topics.
 * Mỗi topic tương ứng với 1 loại notification.
 */
object FcmTopicManager {

    private const val TAG = "FcmTopicManager"

    const val TOPIC_MATCH_REMINDER = "match_reminder"
    const val TOPIC_LIVE_SCORE     = "live_score"
    const val TOPIC_MATCH_RESULT   = "match_result"

    /** Subscribe tất cả topics mặc định (gọi khi app khởi động lần đầu) */
    fun subscribeAll() {
        subscribeToTopic(TOPIC_MATCH_REMINDER)
        subscribeToTopic(TOPIC_LIVE_SCORE)
        subscribeToTopic(TOPIC_MATCH_RESULT)
    }

    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener { Log.d(TAG, "Subscribed to: $topic") }
            .addOnFailureListener { Log.e(TAG, "Failed to subscribe: $topic", it) }
    }

    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener { Log.d(TAG, "Unsubscribed from: $topic") }
            .addOnFailureListener { Log.e(TAG, "Failed to unsubscribe: $topic", it) }
    }
}
