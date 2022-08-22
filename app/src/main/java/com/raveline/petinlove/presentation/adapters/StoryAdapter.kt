package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.raveline.petinlove.data.model.StoryModel
import com.raveline.petinlove.databinding.ItemAddStoryAdapterBinding
import com.raveline.petinlove.databinding.ItemStoriesAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.presentation.viewmodels.UserViewModel

class StoryAdapter(
    private val userViewModel: UserViewModel
) : RecyclerView.Adapter<StoryAdapter.MyViewHolder>() {

    private var stories = emptyList<StoryModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if (viewType == 0) {
            val binding = ItemAddStoryAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MyViewHolder(binding)
        } else {
            val binding = ItemStoriesAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MyViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = stories[position]
        holder.bind(story)
    }

    override fun getItemCount(): Int = stories.size

    class MyViewHolder(
        private val viewBinding: ViewBinding
    ) : RecyclerView.ViewHolder(
        viewBinding.root
    ) {

        fun bind(story: StoryModel) {
            when (viewBinding) {

                is ItemAddStoryAdapterBinding -> {
                    viewBinding.apply {
                        Glide.with(imageViewAddStoryAdapter)
                            .load(story.imagePath)
                            .into(imageViewAddStoryAdapter)
                    }
                }

                is ItemStoriesAdapterBinding -> {
                    setImageIfIsSeen(viewBinding, story)
                }

            }
        }

        private fun setImageIfIsSeen(
            binding: ItemStoriesAdapterBinding,
            story: StoryModel
        ) {
            binding.apply {
                if (story.isSeen) {
                    Glide.with(imageViewStoryAdapterSeen)
                        .load(story.imagePath)
                        .into(imageViewStoryAdapterSeen)
                } else {
                    Glide.with(imageViewStoryAdapterSeen)
                        .load(story.imagePath)
                        .into(imageViewStoryAdapterSeen)
                }
                textViewStoryAdapterUserName.text = story.userName
            }
        }
    }

    fun setData(storiesList: List<StoryModel>) {
        val diffUtil = ListDiffUtil(stories, storiesList)
        val result = DiffUtil.calculateDiff(diffUtil)
        stories = storiesList
        result.dispatchUpdatesTo(this)
    }


}