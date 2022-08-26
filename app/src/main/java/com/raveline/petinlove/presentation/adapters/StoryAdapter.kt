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
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.StoryModel
import com.raveline.petinlove.data.model.hashMapToUserModel
import com.raveline.petinlove.data.model.mapToStory
import com.raveline.petinlove.databinding.ItemAddStoryAdapterBinding
import com.raveline.petinlove.databinding.ItemStoriesAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.domain.utils.SystemFunctions
import com.raveline.petinlove.domain.utils.firstRegisterUserImage
import com.raveline.petinlove.domain.utils.storyFirebaseDocumentReference
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class StoryAdapter(
    private val storyViewModel: StoryViewModel,
    private val fragment: Fragment
) : RecyclerView.Adapter<StoryAdapter.MyViewHolder>() {

    private var stories = emptyList<StoryModel>()
    val user = SystemFunctions.getLoggedUserFromPref(fragment.requireContext())!!

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
        return if (position == 0) 0
        else 1
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val story = stories[position]
        getUserDataDisplay(holder, story)

        if (holder.bindingAdapterPosition != 0) {
            seenStory(holder, story.userId)
        }

        if (holder.bindingAdapterPosition == 0) {
            val binding = holder.viewBinding as ItemAddStoryAdapterBinding
            myStory(binding.textViewAddStoryAdapterTitle, binding.imageViewAddStoryAdapter, false)
        }

        holder.itemView.setOnClickListener {
            if (holder.bindingAdapterPosition == 0) {
                val binding = holder.viewBinding as ItemAddStoryAdapterBinding
                myStory(
                    binding.textViewAddStoryAdapterTitle,
                    binding.imageViewAddStoryAdapter,
                    true
                )
            } else {
                //TODO: go to story selected
            }
        }

    }

    override fun getItemCount(): Int = stories.size

    private fun getUserDataDisplay(
        viewHolder: MyViewHolder,
        story: StoryModel,
    ) {

        storyViewModel.viewModelScope.launch {
            storyViewModel.getUserById(story.userId).addSnapshotListener { doc, error ->
                if (doc != null) {
                    val mUser = hashMapToUserModel(doc)
                    val currentTime = System.currentTimeMillis()
                    val timeCorrect =
                        currentTime > story.timeStart && currentTime < story.timeEnd
                    if (viewHolder.itemViewType == 0) {
                        val binding = viewHolder.viewBinding as ItemAddStoryAdapterBinding
                        "Add Story".let { binding.textViewAddStoryAdapterTitle.text = it }
                        if (timeCorrect) {
                            Glide.with(binding.imageViewAddStoryAdapter)
                                .load(mUser.userProfileImage)
                                .fitCenter()
                                .into(binding.imageViewAddStoryAdapter)

                        } else {
                            Glide.with(binding.imageViewAddStoryAdapter)
                                .load(firstRegisterUserImage)
                                .fitCenter()
                                .into(binding.imageViewAddStoryAdapter)
                        }
                    } else {
                        val binding = viewHolder.viewBinding as ItemStoriesAdapterBinding
                        Glide.with(binding.imageViewStoryAdapterNotSeen)
                            .load(mUser.userProfileImage)
                            .fitCenter()
                            .into(binding.imageViewStoryAdapterNotSeen)
                        binding.textViewStoryAdapterUserName.text = user.userName
                    }
                }
            }
        }
    }

    private fun myStory(textView: TextView, circleImageView: CircleImageView, click: Boolean) {

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
                    } else {
                        if (count > 0) {
                            "My Story".let { textView.text = it }
                            circleImageView.visibility = VISIBLE
                        } else {
                            "Add Story".let { textView.text = it }
                            circleImageView.visibility = VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAGStoryAdapter", error.details)
                }
            })
        }
    }

    private fun seenStory(viewHolder: MyViewHolder, userId: String) {
        val binding = viewHolder.viewBinding as ItemStoriesAdapterBinding
        storyViewModel.viewModelScope.launch {
            storyViewModel.getStoriesGen().child(userId).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    val currentTime = System.currentTimeMillis()
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

                        if (count > 0) {
                            binding.imageViewStoryAdapterSeen.visibility = VISIBLE
                            binding.imageViewStoryAdapterNotSeen.visibility = GONE
                        } else {
                            binding.imageViewStoryAdapterNotSeen.visibility = VISIBLE
                            binding.imageViewStoryAdapterSeen.visibility = GONE
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
        val viewBinding: ViewBinding,
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