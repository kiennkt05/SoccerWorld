package com.example.soccerworld.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.content.Context
import com.example.soccerworld.ui.navigation.AppNavigation
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.soccerworld.ui.theme.SoccerWorldTheme
import com.example.soccerworld.ui.theme.ThemeConfig
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.util.LocaleHelper
import com.example.soccerworld.work.LivePollingScheduler

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = CustomSharedPreferences.invoke(newBase)
        val lang = prefs.getLanguage()
        val context = LocaleHelper.wrapContext(newBase, lang)
        super.attachBaseContext(context)
    }

    // Launcher xin quyền POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Kết quả được xử lý trong NotificationSettingsScreen nếu user vào Settings */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load initial theme setting
        val prefs = CustomSharedPreferences.invoke(this)
        ThemeConfig.appThemeState.value = prefs.getTheme()


        // Xin quyền thông báo tự động cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Khởi động scheduler check favorite matches
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = com.example.soccerworld.data.local.FootballDatabase.invoke(this@MainActivity)
                val favorites = db.footballDao().getAllFavorites().first()
                for (fav in favorites) {
                    if (fav.status == "IN_PLAY" || fav.status == "PAUSED") {
                        com.example.soccerworld.work.LivePollingScheduler.startForMatchId(this@MainActivity, fav.matchId, 0L)
                    } else if (fav.status == "SCHEDULED" || fav.status == "TIMED") {
                        val delayMs = calculateDelayMs(fav.utcDate)
                        val fifteenMinsMs = 15 * 60 * 1000L
                        val wakeUpIn = if (delayMs > fifteenMinsMs) delayMs - fifteenMinsMs else delayMs
                        
                        if (wakeUpIn > 0) {
                            com.example.soccerworld.work.LivePollingScheduler.startForMatchId(this@MainActivity, fav.matchId, wakeUpIn)
                        } else {
                            // Time passed, check immediately to get actual status
                            com.example.soccerworld.work.LivePollingScheduler.startForMatchId(this@MainActivity, fav.matchId, 0L)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        setContent {
            SoccerWorldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun calculateDelayMs(utcDateString: String?): Long {
        if (utcDateString == null) return 60_000L // Default 1 min if null
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = format.parse(utcDateString)
            if (date != null) {
                date.time - System.currentTimeMillis()
            } else {
                60_000L
            }
        } catch (e: Exception) {
            60_000L
        }
    }
}
