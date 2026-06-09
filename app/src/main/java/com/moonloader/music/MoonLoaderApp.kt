package com.moonloader.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.moonloader.music.data.db.MusicDatabase
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.OkHttpClient
import okhttp3.JavaNetCookieJar
import java.net.CookieManager

class MoonLoaderApp : Application() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "moonloader_playback"
        const val DOWNLOAD_CHANNEL_ID = "moonloader_download"
        lateinit var instance: MoonLoaderApp
            private set
    }

    val database by lazy { MusicDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initNewPipe()
        createNotificationChannels()
    }

    private fun initNewPipe() {
        val cookieManager = CookieManager()
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
        NewPipe.init(object : Downloader() {
            override fun execute(request: Request): Response {
                val requestBuilder = okhttp3.Request.Builder().url(request.url())
                request.headers().forEach { (key, values) ->
                    values.forEach { value -> requestBuilder.addHeader(key, value) }
                }
                val okHttpRequest = if (request.httpMethod() == "POST") {
                    requestBuilder.post(okhttp3.RequestBody.create(null, request.dataToSend() ?: ByteArray(0))).build()
                } else { requestBuilder.get().build() }
                val resp = okHttpClient.newCall(okHttpRequest).execute()
                return Response(resp.code, resp.message, resp.headers.toMultimap(), resp.body?.string(), resp.request.url.toString())
            }
        })
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannels(listOf(
                NotificationChannel(PLAYBACK_CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Playback controls"; setShowBadge(false) },
                NotificationChannel(DOWNLOAD_CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Download progress" }
            ))
        }
    }
}
