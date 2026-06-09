package com.moonloader.music.data.db

import android.content.Context
import androidx.room.*
import com.moonloader.music.data.model.*

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun getAllSongs(): kotlinx.coroutines.flow.Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY addedAt DESC")
    fun getDownloadedSongs(): kotlinx.coroutines.flow.Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): Song?

    @Query("UPDATE songs SET isDownloaded = :downloaded, localPath = :path WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, downloaded: Boolean, path: String?)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun getAllSubscriptions(): kotlinx.coroutines.flow.Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun subscribe(subscription: Subscription)

    @Delete
    suspend fun unsubscribe(subscription: Subscription)

    @Query("SELECT * FROM subscriptions WHERE channelId = :channelId")
    suspend fun getSubscription(channelId: String): Subscription?
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): kotlinx.coroutines.flow.Flow<List<Playlist>>

    @Insert
    suspend fun createPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
}

@Database(
    entities = [Song::class, Subscription::class, Playlist::class, PlaylistSong::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "moonloader_music.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
