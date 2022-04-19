package com.daaniikusnanta.storyapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daaniikusnanta.storyapp.R
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.misc.getElapsedTimeString

class ListStoryAdapter(private val listStory: List<ListStoryItem>, private val context: Context) : RecyclerView.Adapter<ListStoryAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val story = listStory[position]
        with(story) {
            Glide.with(holder.itemView.context)
                .load(photoUrl)
                .into(holder.imgStory)
            holder.tvUsername.text = name
            holder.tvTime.text = createdAt?.let { getElapsedTimeString(it, context) }
        }

        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(listStory[holder.adapterPosition], holder) }
    }

    override fun getItemCount(): Int = listStory.size

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgStory: ImageView = itemView.findViewById(R.id.img_story)
        var tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryItem, holder: ListViewHolder)
    }
}