package com.moonloader.music.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.moonloader.music.data.extractor.YouTubeExtractor
import com.moonloader.music.data.model.Song
import com.moonloader.music.databinding.FragmentHomeBinding
import com.moonloader.music.download.DownloadWorker
import com.moonloader.music.ui.adapters.SongAdapter
import com.moonloader.music.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadTrending()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(
            onPlay = { song -> playSong(song) },
            onDownload = { song -> downloadSong(song) }
        )
        binding.recyclerHome.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHome.adapter = adapter
    }

    private fun loadTrending() {
        binding.progressHome.visibility = View.VISIBLE
        binding.tvEmptyHome.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val songs = YouTubeExtractor.getTrending()
                binding.progressHome.visibility = View.GONE
                if (songs.isEmpty()) {
                    binding.tvEmptyHome.visibility = View.VISIBLE
                } else {
                    adapter.submitList(songs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progressHome.visibility = View.GONE
                binding.tvEmptyHome.visibility = View.VISIBLE
                binding.tvEmptyHome.text = getString(com.moonloader.music.R.string.error_loading)
            }
        }
    }

    private fun playSong(song: Song) {
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadSong(song: Song) {
        val request = DownloadWorker.buildRequest(song.videoId, song.title, song.id)
        WorkManager.getInstance(requireContext()).enqueue(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
