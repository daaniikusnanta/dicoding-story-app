package com.daaniikusnanta.storyapp.adapter

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.databinding.ItemStoryBinding
import com.daaniikusnanta.storyapp.misc.getElapsedTimeString

class ListStoryAdapter : PagingDataAdapter<ListStoryItem, ListStoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story, onItemClickCallback)
        }
    }

    class ListViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem, onItemClickCallback: OnItemClickCallback) {
            with(story) {
                Glide.with(binding.root.context)
                    .load(photoUrl)
                    .into(binding.imgStory)
                binding.tvUsername.text = name
                binding.tvTime.text = getElapsedTimeString(createdAt, binding.root.context)
                binding.tvLocation.text = lat?.let {
                    try {
                        Geocoder.isPresent()
                            .let {
                                if (it) {
                                    Geocoder(binding.root.context).getFromLocation(
                                        lat.toDouble(),
                                        lon!!.toDouble(),
                                        1
                                    )[0].locality
                                } else {
                                    "$lat, $lon"
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        "$lat, $lon"
                    }
                }

                binding.storyCard.setOnClickListener { onItemClickCallback.onItemClicked(story, binding) }
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem?, binding: ItemStoryBinding)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}