package com.moonloader.music.ui.library

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moonloader.music.MoonLoaderApp
import com.moonloader.music.databinding.FragmentLibraryBinding
import com.moonloader.music.ui.adapters.SongAdapter
import com.moonloader.music.ui.player.PlayerActivity
import com.moonloader.music.data.extractor.YouTubeExtractor
import com.moonloader.music.data.model.Song
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeLibrary()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(onPlay = { song -> playSong(song) })
        binding.recyclerLibrary.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLibrary.adapter = adapter
    }

    private fun observeLibrary() {
        lifecycleScope.launch {
            MoonLoaderApp.instance.database.songDao().getAllSongs().collectLatest { songs ->
                adapter.submitList(songs)
                binding.tvEmptyLibrary.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerLibrary.visibility = if (songs.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun playSong(song: Song) {
        if (song.isDownloaded && song.localPath != null) {
            val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_STREAM_URL, song.localPath)
                putExtra(PlayerActivity.EXTRA_TITLE, song.title)
                putExtra(PlayerActivity.EXTRA_ARTIST, song.artist)
                putExtra(PlayerActivity.EXTRA_THUMBNAIL, song.thumbnailUrl)
            }
            startActivity(intent)
        } else {
            lifecycleScope.launch {
                try {
                    val streamUrl = YouTubeExtractor.getStreamUrl(song.id) ?: return@launch
                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                        putExtra(PlayerActivity.EXTRA_TITLE, song.title)
                        putExtra(PlayerActivity.EXTRA_ARTIST, song.artist)
                        putExtra(PlayerActivity.EXTRA_THUMBNAIL, song.thumbnailUrl)
                    }
                    startActivity(intent)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
