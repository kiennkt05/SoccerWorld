package com.example.soccerworld

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import kotlinx.coroutines.Dispatchers
import com.example.soccerworld.util.FcmTopicManager
import com.example.soccerworld.util.NotificationHelper
import com.example.soccerworld.work.MatchReminderScheduler

class SoccerWorldApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // 1. Tạo Notification Channels (yêu cầu Android 8+)
        NotificationHelper.createChannels(this)

        // 2. Subscribe FCM topics mặc định (lần đầu cài app)
        //    FirebaseMessagingService.onNewToken() sẽ re-subscribe khi token refresh
        FcmTopicManager.subscribeAll()

        // 3. Bắt đầu periodic worker nhắc trận yêu thích (Ngừng sử dụng vì đã có SingleMatchWorker)
        MatchReminderScheduler.stop(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("soccerworld_image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB
                    .build()
            }
            .crossfade(false)
            .dispatcher(Dispatchers.IO)
            .build()
    }
}
