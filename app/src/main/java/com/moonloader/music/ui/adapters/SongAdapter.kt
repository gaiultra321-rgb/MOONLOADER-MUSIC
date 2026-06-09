package com.moonloader.music.ui.adapters

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moonloader.music.R
import com.moonloader.music.data.model.Song

class SongAdapter(
    private val onPlay: (Song) -> Unit,
    private val onDownload: ((Song) -> Unit)? = null,
    private val onMore: ((Song) -> Unit)? = null
) : ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(a: Song, b: Song) = a.id == b.id
        override fun areContentsTheSame(a: Song, b: Song) = a == b
    }

    inner class SongViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val thumbnail: ImageView = view.findViewById(R.id.img_song_thumbnail)
        private val title: TextView = view.findViewById(R.id.tv_song_title)
        private val artist: TextView = view.findViewById(R.id.tv_song_artist)
        private val duration: TextView = view.findViewById(R.id.tv_song_duration)
        private val btnDownload: ImageButton = view.findViewById(R.id.btn_song_download)
        private val btnMore: ImageButton = view.findViewById(R.id.btn_song_more)

        fun bind(song: Song) {
            title.text = song.title
            artist.text = song.artist
            duration.text = formatDuration(song.duration)
            Glide.with(view).load(song.thumbnailUrl)
                .placeholder(R.drawable.ic_music_note)
                .centerCrop().into(thumbnail)
            view.setOnClickListener { onPlay(song) }
            btnDownload.visibility = if (song.isDownloaded) View.GONE else View.VISIBLE
            btnDownload.setOnClickListener { onDownload?.invoke(song) }
            btnMore.setOnClickListener { onMore?.invoke(song) }
        }

        private fun formatDuration(s: Long) = "%d:%02d".format(s / 60, s % 60)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
        SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false))

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) =
        holder.bind(getItem(position))
}
