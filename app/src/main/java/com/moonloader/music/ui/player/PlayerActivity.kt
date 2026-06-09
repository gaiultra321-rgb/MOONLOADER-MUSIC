package com.moonloader.music.ui.player

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.moonloader.music.databinding.ActivityPlayerBinding
import com.moonloader.music.service.MusicPlaybackService

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())
    private var seekbarTracking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupPlayerControls()
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            setupController()
        }, MoreExecutors.directExecutor())
    }

    private fun setupController() {
        val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val artist = intent.getStringExtra(EXTRA_ARTIST) ?: ""
        val thumbnail = intent.getStringExtra(EXTRA_THUMBNAIL) ?: ""

        binding.tvTitle.text = title
        binding.tvArtist.text = artist
        Glide.with(this).load(thumbnail).placeholder(com.moonloader.music.R.drawable.ic_music_note)
            .centerCrop().into(binding.imgAlbumArt)

        controller?.let { ctrl ->
            val mediaItem = MediaItem.Builder()
                .setUri(streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setArtworkUri(android.net.Uri.parse(thumbnail))
                        .build()
                )
                .build()
            ctrl.setMediaItem(mediaItem)
            ctrl.prepare()
            ctrl.play()

            ctrl.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    binding.btnPlayPause.setImageResource(
                        if (isPlaying) android.R.drawable.ic_media_pause
                        else android.R.drawable.ic_media_play
                    )
                    if (isPlaying) startSeekbarUpdate() else handler.removeCallbacksAndMessages(null)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        binding.seekBar.max = (ctrl.duration / 1000).toInt()
                    }
                }
            })
        }
    }

    private fun startSeekbarUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                if (!seekbarTracking) {
                    val pos = ((controller?.currentPosition ?: 0) / 1000).toInt()
                    binding.seekBar.progress = pos
                    binding.tvCurrentTime.text = formatTime(pos.toLong())
                    binding.tvTotalTime.text = formatTime((controller?.duration ?: 0) / 1000)
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun setupPlayerControls() {
        binding.btnPlayPause.setOnClickListener {
            controller?.let { if (it.isPlaying) it.pause() else it.play() }
        }
        binding.btnNext.setOnClickListener { controller?.seekToNextMediaItem() }
        binding.btnPrev.setOnClickListener { controller?.seekToPreviousMediaItem() }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvCurrentTime.text = formatTime(progress.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) { seekbarTracking = true }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                seekbarTracking = false
                controller?.seekTo((sb?.progress?.toLong() ?: 0) * 1000)
            }
        })
    }

    private fun formatTime(seconds: Long) = "%d:%02d".format(seconds / 60, seconds % 60)

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_THUMBNAIL = "extra_thumbnail"
    }
}
