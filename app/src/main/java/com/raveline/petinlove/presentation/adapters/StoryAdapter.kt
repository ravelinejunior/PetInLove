package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.StoryModel
import com.raveline.petinlove.data.model.hashMapToUserModel
import com.raveline.petinlove.databinding.ItemAddStoryAdapterBinding
import com.raveline.petinlove.databinding.ItemStoriesAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.domain.utils.firstRegisterUserImage
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryAdapter(
    private val storyViewModel: StoryViewModel,
    private val fragment: Fragment
) : RecyclerView.Adapter<StoryAdapter.MyViewHolder>() {

    private var idList = emptyList<String>()
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

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return 0

        return 1
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = stories[position]
        if (idList.isEmpty()) {
            holder.bindForAddStory(holder, fragment)
        } else {
            if (holder.bindingAdapterPosition == 0) {
                getUserDataDisplay(holder, position)
            } else {
                holder.bindForStories(holder, story, fragment)
            }
        }

    }

    override fun getItemCount(): Int {
        if (idList.isEmpty()) return 1
        return idList.size
    }

    private fun getUserDataDisplay(
        viewHolder: MyViewHolder,
        pos: Int
    ) {

        storyViewModel.viewModelScope.launch {
            storyViewModel.idsFlow.collectLatest { ids ->
                for (id in ids) {
                    storyViewModel.getUserById(id).addSnapshotListener { doc, error ->
                        if (doc != null) {
                            val user = hashMapToUserModel(doc)

                            if (pos != 0) {
                                //add function to verify if the story was saw
                                val binding =
                                    viewHolder.viewBinding as ItemStoriesAdapterBinding
                                Glide.with(binding.imageViewStoryAdapterNotSeen)
                                    .load(user.userProfileImage)
                                    .fitCenter()
                                    .into(binding.imageViewStoryAdapterNotSeen)
                                binding.textViewStoryAdapterUserName.text = user.userName
                            } else {
                                val binding =
                                    viewHolder.viewBinding as ItemAddStoryAdapterBinding
                                Glide.with(binding.imageViewAddStoryAdapter)
                                    .load(firstRegisterUserImage)
                                    .fitCenter()
                                    .into(binding.imageViewAddStoryAdapter)

                                binding.imageViewAddStoryAdapter.setOnClickListener {
                                    fragment.findNavController()
                                        .navigate(R.id.action_homeFragment_to_imageAddGeneralFragment)
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    class MyViewHolder(
        val viewBinding: ViewBinding,
    ) : RecyclerView.ViewHolder(
        viewBinding.root
    ) {

        fun bindForAddStory(viewHolder: MyViewHolder, fragment: Fragment) {
            val binding = viewHolder.viewBinding as ItemAddStoryAdapterBinding
            Glide.with(binding.imageViewAddStoryAdapter)
                .load(firstRegisterUserImage)
                .fitCenter()
                .into(binding.imageViewAddStoryAdapter)

            binding.imageViewAddStoryAdapter.setOnClickListener {
                fragment.findNavController()
                    .navigate(R.id.action_homeFragment_to_imageAddGeneralFragment)
            }
        }

        fun bindForStories(
            viewHolder: MyViewHolder,
            story: StoryModel,
            fragment: Fragment
        ) {

            val binding = viewHolder.viewBinding as ItemStoriesAdapterBinding
            Glide.with(binding.imageViewStoryAdapterNotSeen)
                .load(story.profileImage)
                .fitCenter()
                .into(binding.imageViewStoryAdapterNotSeen)
            binding.textViewStoryAdapterUserName.text = story.userName
        }


        private fun navigateToImagePick(fragment: Fragment) {
            fragment.findNavController()
                .navigate(R.id.action_homeFragment_to_imageAddGeneralFragment)
        }

        private fun alertImageSelect(fragment: Fragment) {

            fragment.findNavController()
                .navigate(R.id.action_homeFragment_to_imageAddGeneralFragment)
        }

    }

    fun setData(storiesList: List<StoryModel>) {
        val diffUtil = ListDiffUtil(stories, storiesList)
        val result = DiffUtil.calculateDiff(diffUtil)
        stories = storiesList
        result.dispatchUpdatesTo(this)
    }

    fun setIdData(ids: List<String>) {
        val diffUtil = ListDiffUtil(idList, ids)
        val result = DiffUtil.calculateDiff(diffUtil)
        idList = ids
        storyViewModel.setIds(idList)
        result.dispatchUpdatesTo(this)
    }

}