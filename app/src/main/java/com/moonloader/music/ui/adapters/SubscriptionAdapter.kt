package com.moonloader.music.ui.adapters

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moonloader.music.R
import com.moonloader.music.data.model.Subscription

class SubscriptionAdapter(
    private val onOpen: (Subscription) -> Unit,
    private val onUnsubscribe: (Subscription) -> Unit
) : ListAdapter<Subscription, SubscriptionAdapter.SubViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Subscription>() {
        override fun areItemsTheSame(a: Subscription, b: Subscription) = a.channelId == b.channelId
        override fun areContentsTheSame(a: Subscription, b: Subscription) = a == b
    }

    inner class SubViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: ImageView = view.findViewById(R.id.img_channel_avatar)
        private val name: TextView = view.findViewById(R.id.tv_channel_name)
        private val btnUnsub: ImageButton = view.findViewById(R.id.btn_unsubscribe)

        fun bind(sub: Subscription) {
            name.text = sub.channelName
            Glide.with(view).load(sub.avatarUrl)
                .placeholder(R.drawable.ic_subscriptions)
                .circleCrop().into(avatar)
            view.setOnClickListener { onOpen(sub) }
            btnUnsub.setOnClickListener { onUnsubscribe(sub) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubViewHolder =
        SubViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false))

    override fun onBindViewHolder(holder: SubViewHolder, position: Int) =
        holder.bind(getItem(position))
}
