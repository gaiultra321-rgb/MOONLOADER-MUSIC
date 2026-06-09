package com.moonloader.music.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.moonloader.music.MoonLoaderApp

/**
 * DownloadService — foreground service wrapper for WorkManager-based downloads.
 * WorkManager's DownloadWorker is the actual download engine; this service
 * satisfies the <service> declaration in AndroidManifest that WorkManager
 * requires for foreground expedited jobs on older Android versions.
 */
class DownloadService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIFICATION_ID,
            buildNotification("Preparing download…")
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(message: String): Notification {
        return NotificationCompat.Builder(this, MoonLoaderApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("MoonLoader Downloads")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP = "com.moonloader.music.STOP_DOWNLOAD_SERVICE"
    }
}
