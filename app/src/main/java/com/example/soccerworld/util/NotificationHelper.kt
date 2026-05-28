package com.example.soccerworld.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.soccerworld.R
import com.example.soccerworld.ui.MainActivity

object NotificationHelper {

    // ── Channel IDs ──────────────────────────────────────────────────────────
    const val CHANNEL_MATCH_REMINDER   = "channel_match_reminder"
    const val CHANNEL_LIVE_SCORE       = "channel_live_score"
    const val CHANNEL_MATCH_RESULT     = "channel_match_result"
    const val CHANNEL_MATCH_COMMENT    = "channel_match_comment"

    // ── Notification IDs base ────────────────────────────────────────────────
    private const val NOTIF_ID_REMINDER_BASE = 1000
    private const val NOTIF_ID_LIVE_BASE     = 2000
    private const val NOTIF_ID_RESULT_BASE   = 3000
    private const val NOTIF_ID_COMMENT_BASE  = 4000

    /**
     * Gọi một lần trong Application.onCreate() để đăng ký tất cả channels.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MATCH_REMINDER,
                "Match Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc nhở trước khi trận đấu yêu thích bắt đầu"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_LIVE_SCORE,
                "Live Score Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cập nhật tỷ số trực tiếp của các trận đấu đang diễn ra"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MATCH_RESULT,
                "Match Results",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Kết quả trận đấu sau khi kết thúc"
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MATCH_COMMENT,
                "Match Comments",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo khi có bình luận mới ở trận đấu yêu thích"
            }
        )
    }

    // ── 1. Match Reminder ────────────────────────────────────────────────────

    /**
     * Gửi thông báo nhắc trước khi trận bắt đầu.
     * @param matchId   ID trận (dùng làm notification ID)
     * @param homeTeam  Tên đội nhà
     * @param awayTeam  Tên đội khách
     * @param minutesLeft Số phút còn lại (ví dụ 15)
     */
    fun sendMatchReminderNotification(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        minutesLeft: Int
    ) {
        if (!areNotificationsEnabled(context)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val prefs = CustomSharedPreferences.invoke(context)
        if (!prefs.isMatchReminderEnabled()) return

        val intent = buildMainIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context, matchId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ Trận đấu sắp bắt đầu!")
            .setContentText("$homeTeam vs $awayTeam – còn $minutesLeft phút nữa")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$homeTeam vs $awayTeam sẽ bắt đầu sau $minutesLeft phút. Đừng bỏ lỡ!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notifId = NOTIF_ID_REMINDER_BASE + (matchId.hashCode() and 0x0FFF)
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    // ── 2. Live Score Update ─────────────────────────────────────────────────

    /**
     * Gửi thông báo khi có bàn thắng trong trận đang live.
     * @param matchId   ID trận
     * @param homeTeam  Tên đội nhà
     * @param awayTeam  Tên đội khách
     * @param homeScore Bàn thắng đội nhà
     * @param awayScore Bàn thắng đội khách
     */
    fun sendLiveScoreNotification(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int
    ) {
        if (!areNotificationsEnabled(context)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val prefs = CustomSharedPreferences.invoke(context)
        if (!prefs.isLiveScoreEnabled()) return

        val intent = buildMainIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context, matchId.hashCode() + 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_LIVE_SCORE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚽ Bàn thắng!")
            .setContentText("$homeTeam $homeScore - $awayScore $awayTeam")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$homeTeam $homeScore - $awayScore $awayTeam\nTỷ số mới được cập nhật!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notifId = NOTIF_ID_LIVE_BASE + (matchId.hashCode() and 0x0FFF)
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    // ── 3. Match Result ──────────────────────────────────────────────────────

    /**
     * Gửi thông báo kết quả sau khi trận kết thúc.
     * @param matchId   ID trận
     * @param homeTeam  Tên đội nhà
     * @param awayTeam  Tên đội khách
     * @param homeScore Bàn thắng đội nhà (FT)
     * @param awayScore Bàn thắng đội khách (FT)
     * @param winner    "HOME_TEAM" | "AWAY_TEAM" | "DRAW" | null
     */
    fun sendMatchResultNotification(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        winner: String?
    ) {
        if (!areNotificationsEnabled(context)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val prefs = CustomSharedPreferences.invoke(context)
        if (!prefs.isMatchResultEnabled()) return

        val resultText = when (winner) {
            "HOME_TEAM" -> "$homeTeam thắng! 🏆"
            "AWAY_TEAM" -> "$awayTeam thắng! 🏆"
            "DRAW"      -> "Hòa! 🤝"
            else        -> "Kết thúc"
        }

        val intent = buildMainIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context, matchId.hashCode() + 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH_RESULT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🏁 Kết quả trận đấu")
            .setContentText("$homeTeam $homeScore - $awayScore $awayTeam")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$homeTeam $homeScore - $awayScore $awayTeam\n$resultText")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notifId = NOTIF_ID_RESULT_BASE + (matchId.hashCode() and 0x0FFF)
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    // ── 4. Match Comment ─────────────────────────────────────────────────────

    /**
     * Gửi thông báo khi có bình luận mới ở trận đấu yêu thích.
     */
    fun sendCommentNotification(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        commenterName: String,
        commentText: String
    ) {
        if (!areNotificationsEnabled(context)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val prefs = CustomSharedPreferences.invoke(context)
        if (!prefs.isMatchCommentEnabled()) return

        val intent = buildMainIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context, matchId.hashCode() + 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH_COMMENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💬 Bình luận mới – $homeTeam vs $awayTeam")
            .setContentText("$commenterName: $commentText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$commenterName: $commentText\n\nXem chi tiết trận đấu.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notifId = NOTIF_ID_COMMENT_BASE + (matchId.hashCode() and 0x0FFF)
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildMainIntent(context: Context): Intent =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun areNotificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()
}
