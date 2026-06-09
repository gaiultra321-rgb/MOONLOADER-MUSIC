package com.moonloader.music.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.moonloader.music.R
import com.moonloader.music.data.extractor.YouTubeExtractor
import com.moonloader.music.data.model.Song
import com.moonloader.music.databinding.FragmentSearchBinding
import com.moonloader.music.download.DownloadWorker
import com.moonloader.music.ui.adapters.SongAdapter
import com.moonloader.music.ui.player.PlayerActivity
import kotlinx.coroutines.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(
            onPlay = { song -> playSong(song) },
            onDownload = { song -> downloadSong(song) }
        )
        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearchResults.adapter = adapter
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim() ?: return
                if (q.length >= 2) debounceSearch(q)
            }
        })
        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = v.text?.toString()?.trim() ?: return@setOnEditorActionListener false
                if (q.isNotEmpty()) search(q)
                true
            } else false
        }
    }

    private fun debounceSearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(400)
            search(query)
        }
    }

    private fun search(query: String) {
        binding.progressSearch.visibility = View.VISIBLE
        binding.tvSearchPlaceholder.visibility = View.GONE
        binding.recyclerSearchResults.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val results = YouTubeExtractor.search(query)
                binding.progressSearch.visibility = View.GONE
                if (results.isEmpty()) {
                    binding.tvSearchPlaceholder.text = getString(R.string.no_results)
                    binding.tvSearchPlaceholder.visibility = View.VISIBLE
                } else {
                    adapter.submitList(results)
                    binding.recyclerSearchResults.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progressSearch.visibility = View.GONE
                binding.tvSearchPlaceholder.text = getString(R.string.error_loading)
                binding.tvSearchPlaceholder.visibility = View.VISIBLE
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
            } catch (e: Exception) { e.printStackTrace() }
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
