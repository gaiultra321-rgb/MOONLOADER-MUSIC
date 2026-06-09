package com.moonloader.music.ui.subscriptions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.moonloader.music.MoonLoaderApp
import com.moonloader.music.data.model.Subscription
import com.moonloader.music.databinding.FragmentSubscriptionsBinding
import com.moonloader.music.ui.adapters.SubscriptionAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SubscriptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeSubscriptions()
    }

    private fun setupRecyclerView() {
        adapter = SubscriptionAdapter(
            onOpen = { sub -> openChannel(sub) },
            onUnsubscribe = { sub -> unsubscribe(sub.channelId) }
        )
        binding.recyclerSubscriptions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSubscriptions.adapter = adapter
    }

    private fun observeSubscriptions() {
        lifecycleScope.launch {
            MoonLoaderApp.instance.database.subscriptionDao().getAllSubscriptions().collectLatest { subs ->
                adapter.submitList(subs)
                binding.tvEmptySubscriptions.visibility = if (subs.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerSubscriptions.visibility = if (subs.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun openChannel(sub: Subscription) {
        // Navigate to channel page — future feature
    }

    fun subscribe(id: String, name: String, url: String, avatar: String) {
        lifecycleScope.launch {
            MoonLoaderApp.instance.database.subscriptionDao()
                .subscribe(Subscription(id, name, url, avatar))
        }
    }

    fun unsubscribe(channelId: String) {
        lifecycleScope.launch {
            MoonLoaderApp.instance.database.subscriptionDao().getSubscription(channelId)?.let {
                MoonLoaderApp.instance.database.subscriptionDao().unsubscribe(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
