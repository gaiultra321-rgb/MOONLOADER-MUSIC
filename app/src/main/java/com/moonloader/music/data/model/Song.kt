package com.moonloader.music.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: Long,
    val videoId: String,
    val streamUrl: String?,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey val channelId: String,
    val channelName: String,
    val channelUrl: String,
    val avatarUrl: String,
    val subscribedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSong(
    val playlistId: Long,
    val songId: String,
    val position: Int
)
