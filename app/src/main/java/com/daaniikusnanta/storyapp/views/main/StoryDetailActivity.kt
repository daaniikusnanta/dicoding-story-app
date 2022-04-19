package com.daaniikusnanta.storyapp.views.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.daaniikusnanta.storyapp.api.ListStoryItem
import com.daaniikusnanta.storyapp.databinding.ActivityStoryDetailBinding
import com.daaniikusnanta.storyapp.misc.getElapsedTimeString

class StoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupData()
    }

    private fun setupData() {
        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY) as ListStoryItem
        with(story) {
            Glide.with(applicationContext)
                .load(photoUrl)
                .into(binding.imgPhoto)
            binding.tvUsername.text = name
            binding.tvDescription.text = description
            binding.tvTime.text = createdAt?.let { getElapsedTimeString(it, this@StoryDetailActivity) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}