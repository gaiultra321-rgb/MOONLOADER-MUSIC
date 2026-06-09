package com.moonloader.music.ui.downloads

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moonloader.music.MoonLoaderApp
import com.moonloader.music.databinding.FragmentDownloadsBinding
import com.moonloader.music.ui.adapters.SongAdapter
import com.moonloader.music.ui.player.PlayerActivity
import com.moonloader.music.data.model.Song
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeDownloads()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(onPlay = { song -> playSong(song) })
        binding.recyclerDownloads.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDownloads.adapter = adapter
    }

    private fun observeDownloads() {
        lifecycleScope.launch {
            MoonLoaderApp.instance.database.songDao().getDownloadedSongs().collectLatest { songs ->
                adapter.submitList(songs)
                binding.tvEmptyDownloads.visibility = if (songs.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerDownloads.visibility = if (songs.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun playSong(song: Song) {
        val path = song.localPath ?: return
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_URL, path)
            putExtra(PlayerActivity.EXTRA_TITLE, song.title)
            putExtra(PlayerActivity.EXTRA_ARTIST, song.artist)
            putExtra(PlayerActivity.EXTRA_THUMBNAIL, song.thumbnailUrl)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
