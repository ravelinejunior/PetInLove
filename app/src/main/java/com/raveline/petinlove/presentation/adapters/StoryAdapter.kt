package com.raveline.petinlove.presentation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.StoryModel
import com.raveline.petinlove.data.model.hashMapToUserModel
import com.raveline.petinlove.data.model.mapToStory
import com.raveline.petinlove.databinding.ItemStoriesAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.domain.utils.SystemFunctions
import com.raveline.petinlove.domain.utils.firstRegisterUserImage
import com.raveline.petinlove.domain.utils.storyFirebaseDocumentReference
import com.raveline.petinlove.presentation.fragments.HomeFragmentDirections
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import kotlinx.coroutines.launch

class StoryAdapter(
    private val storyViewModel: StoryViewModel,
    private val fragment: Fragment
) : RecyclerView.Adapter<StoryAdapter.MyViewHolder>() {

    private var stories = emptyList<StoryModel>()
    val user = SystemFunctions.getLoggedUserFromPref(fragment.requireContext())!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val binding = ItemStoriesAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = stories[position]
        getUserDataDisplay(holder, story, position)

        if (position != 0) {
            seenStory(holder, story.userId, position)
        }

        if (position == 0) {

            myStory(
                holder.viewBinding.textViewStoryAdapterUserName,
                holder,
                false,
                position,
            )
        }

        holder.itemView.setOnClickListener {
            if (position == 0) {
                val binding = holder.viewBinding
                myStory(
                    binding.textViewStoryAdapterUserName,
                    holder,
                    true,
                    position
                )
            } else {
                val directions =
                    HomeFragmentDirections.actionHomeFragmentToViewStoryActivity(story.userId)
                fragment.findNavController().navigate(directions)
            }
        }

    }

    override fun getItemCount(): Int = stories.size

    private fun getUserDataDisplay(
        viewHolder: MyViewHolder,
        story: StoryModel,
        position: Int
    ) {
        val binding = viewHolder.viewBinding
        storyViewModel.viewModelScope.launch {

            storyViewModel.getUserById(story.userId).addSnapshotListener { doc, error ->
                if (doc != null) {
                    val mUser = hashMapToUserModel(doc)

                    if (story.userId == user.uid) {
                        Glide.with(binding.imageViewStoryAdapterSeen)
                            .load(mUser.userProfileImage)
                            .fitCenter()
                            .dontAnimate()
                            .dontTransform()
                            .into(binding.imageViewStoryAdapterSeen)
                    }


                    if (position != 0) {
                        Glide.with(binding.imageViewStoryAdapterNotSeen)
                            .load(mUser.userProfileImage)
                            .fitCenter()
                            .dontAnimate()
                            .dontTransform()
                            .into(binding.imageViewStoryAdapterNotSeen)
                        binding.textViewStoryAdapterUserName.text = mUser.userName
                    }

                }

            }
        }
    }

    private fun myStory(
        textView: TextView,
        viewHolder: MyViewHolder,
        click: Boolean,
        position: Int
    ) {

        storyViewModel.viewModelScope.launch {
            storyViewModel.getStoriesGen().child(user.uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    val currentTime = System.currentTimeMillis()
                    for (doc in snapshot.children) {
                        val storyMap = doc.value as Map<String, Any>
                        val story = mapToStory(storyMap)
                        if (currentTime > story.timeStart && currentTime < story.timeEnd) {
                            count++
                        }
                    }

                    if (click) {
                        //Show alert dialog
                        viewHolder.navigateToImagePick(fragment)
                    } else {
                        viewHolder.viewBinding.constraintLayoutStoryAdapter.visibility = VISIBLE
                        if (count > 0) {
                            "My Story".let { textView.text = it }
                            Glide.with(viewHolder.viewBinding.imageViewAddStoryAdapter)
                                .load(user.userProfileImage)
                                .fitCenter()
                                .dontAnimate()
                                .dontTransform()
                                .into(viewHolder.viewBinding.imageViewAddStoryAdapter)
                        } else {
                            "Add Story".let { textView.text = it }
                            Glide.with(viewHolder.viewBinding.imageViewAddStoryAdapter)
                                .load(firstRegisterUserImage)
                                .fitCenter()
                                .dontAnimate()
                                .dontTransform()
                                .into(viewHolder.viewBinding.imageViewAddStoryAdapter)


                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAGStoryAdapter", error.details)
                }
            })
        }
    }

    private fun seenStory(viewHolder: MyViewHolder, userId: String, position: Int) {
        val binding = viewHolder.viewBinding
        storyViewModel.viewModelScope.launch {
            storyViewModel.getStoriesGen().child(userId).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    val currentTime = System.currentTimeMillis()
                    if (snapshot.hasChildren()) {
                        for (doc in snapshot.children) {
                            val storyMap = doc.value as Map<String, Any>
                            val story = mapToStory(storyMap)
                            val timeCorrect =
                                currentTime > story.timeStart && currentTime < story.timeEnd

                            if (doc.child(storyFirebaseDocumentReference).child(user.uid)
                                    .exists() && !timeCorrect
                            ) {
                                count++
                            }

                            binding.constraintLayoutStoryAdapter.visibility = GONE
                            if (count > 0) {
                                binding.imageViewStoryAdapterSeen.visibility = VISIBLE
                                binding.imageViewStoryAdapterNotSeen.visibility = GONE

                            } else {
                                binding.imageViewStoryAdapterNotSeen.visibility = VISIBLE
                                binding.imageViewStoryAdapterSeen.visibility = GONE
                            }


                        }
                    } else {
                        binding.apply {
                            imageViewStoryAdapterNotSeen.visibility = GONE
                            imageViewStoryAdapterSeen.visibility = GONE
                            textViewStoryAdapterUserName.visibility = GONE
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAGStoryAdapter", error.details)
                }
            })
        }
    }

    class MyViewHolder(
        val viewBinding: ItemStoriesAdapterBinding,
    ) : RecyclerView.ViewHolder(
        viewBinding.root
    ) {


        fun navigateToImagePick(fragment: Fragment) {
            fragment.findNavController()
                .navigate(R.id.action_homeFragment_to_imageAddGeneralFragment)
        }

        fun alertImageSelect(fragment: Fragment) {

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

}