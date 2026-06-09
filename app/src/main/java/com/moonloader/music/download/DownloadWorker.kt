package com.moonloader.music.download

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.moonloader.music.MoonLoaderApp
import com.moonloader.music.data.extractor.YouTubeExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val songId = inputData.getString(KEY_SONG_ID) ?: return Result.failure()
        val songTitle = inputData.getString(KEY_SONG_TITLE) ?: "Unknown"
        val videoUrl = inputData.getString(KEY_VIDEO_URL) ?: return Result.failure()
        setForeground(createForegroundInfo(songTitle, 0))
        return try {
            val streamUrl = YouTubeExtractor.getStreamUrl(videoUrl) ?: return Result.failure()
            val outputDir = File(applicationContext.getExternalFilesDir(null), "MoonLoader")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "${songId.take(11)}.m4a")
            withContext(Dispatchers.IO) {
                val conn = URL(streamUrl).openConnection()
                conn.connect()
                val totalBytes = conn.contentLength.toLong()
                var downloaded = 0L
                conn.getInputStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buf = ByteArray(8192)
                        var bytes = input.read(buf)
                        while (bytes != -1) {
                            output.write(buf, 0, bytes)
                            downloaded += bytes
                            if (totalBytes > 0) {
                                val pct = (downloaded * 100 / totalBytes).toInt()
                                setProgress(workDataOf(KEY_PROGRESS to pct))
                                setForeground(createForegroundInfo(songTitle, pct))
                            }
                            bytes = input.read(buf)
                        }
                    }
                }
            }
            MoonLoaderApp.instance.database.songDao().updateDownloadStatus(songId, true, outputFile.absolutePath)
            Result.success(workDataOf(KEY_OUTPUT_PATH to outputFile.absolutePath))
        } catch (e: Exception) { e.printStackTrace(); Result.failure() }
    }

    private fun createForegroundInfo(title: String, progress: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, MoonLoaderApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true).build()
        return ForegroundInfo(1001, notification)
    }

    companion object {
        const val KEY_SONG_ID = "song_id"
        const val KEY_SONG_TITLE = "song_title"
        const val KEY_VIDEO_URL = "video_url"
        const val KEY_PROGRESS = "progress"
        const val KEY_OUTPUT_PATH = "output_path"

        fun buildRequest(songId: String, songTitle: String, videoUrl: String) =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf(KEY_SONG_ID to songId, KEY_SONG_TITLE to songTitle, KEY_VIDEO_URL to videoUrl))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
    }
}
