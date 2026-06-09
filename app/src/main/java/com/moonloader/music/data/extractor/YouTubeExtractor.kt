package com.moonloader.music.data.extractor

import com.moonloader.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object YouTubeExtractor {

    private val youtubeService = NewPipe.getService(ServiceList.YouTube.serviceId)

    suspend fun search(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val extractor = youtubeService.getSearchExtractor(query, listOf("music_songs"), "")
            extractor.fetchPage()
            extractor.initialPage.items.mapNotNull { item ->
                if (item is StreamInfoItem) Song(
                    id = item.url, title = item.name, artist = item.uploaderName ?: "",
                    thumbnailUrl = item.thumbnailUrl ?: "", duration = item.duration,
                    videoId = extractVideoId(item.url), streamUrl = null
                ) else null
            }
        } catch (e: Exception) { e.printStackTrace(); emptyList() }
    }

    suspend fun getStreamUrl(videoUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val streamInfo = StreamInfo.getInfo(youtubeService, videoUrl)
            streamInfo.audioStreams.maxByOrNull { it.averageBitrate }?.content
                ?: streamInfo.videoStreams.firstOrNull()?.content
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    suspend fun getTrending(): List<Song> = withContext(Dispatchers.IO) {
        try {
            val extractor = youtubeService.kioskList.getExtractorById("Trending", null)
            extractor.fetchPage()
            extractor.initialPage.items.take(30).mapNotNull { item ->
                if (item is StreamInfoItem) Song(
                    id = item.url, title = item.name, artist = item.uploaderName ?: "",
                    thumbnailUrl = item.thumbnailUrl ?: "", duration = item.duration,
                    videoId = extractVideoId(item.url), streamUrl = null
                ) else null
            }
        } catch (e: Exception) { e.printStackTrace(); emptyList() }
    }

    private fun extractVideoId(url: String): String {
        val regex = Regex("""(?:v=|youtu\.be/|/embed/|/shorts/)([\ w-]{11})""")
        return regex.find(url)?.groupValues?.get(1) ?: url
    }
}
