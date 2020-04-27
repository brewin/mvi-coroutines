package com.github.brewin.mvicoroutines.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.brewin.mvicoroutines.databinding.RepoItemBinding
import com.github.brewin.mvicoroutines.domain.entity.RepoEntity
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow

class RepoListAdapter : ListAdapter<RepoEntity, RepoListAdapter.ViewHolder>(ItemCallback) {

    private val itemClicks = BroadcastChannel<ItemClickEvent>(Channel.BUFFERED)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(RepoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun itemClicks() = itemClicks.asFlow()

    data class ItemClickEvent(val item: RepoEntity)

    inner class ViewHolder(
        private val binding: RepoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RepoEntity) {
            binding.root.setOnClickListener { itemClicks.offer(ItemClickEvent(item)) }
            binding.root.text = item.name
        }
    }

    private object ItemCallback : DiffUtil.ItemCallback<RepoEntity>() {

        override fun areItemsTheSame(oldItem: RepoEntity, newItem: RepoEntity): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: RepoEntity, newItem: RepoEntity): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: RepoEntity, newItem: RepoEntity): Any? =
            if (oldItem == newItem) null
            else Unit // Dummy value to prevent item change animation.
    }
}