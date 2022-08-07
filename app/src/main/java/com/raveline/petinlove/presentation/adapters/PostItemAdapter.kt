package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.model.mapToPost
import com.raveline.petinlove.databinding.ItemHomeAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.domain.utils.postFieldAuthorId
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class PostItemAdapter(
    private val likeViewModel: LikeViewModel,
    private val userViewModel: UserViewModel,
    private val postViewModel: PostViewModel? = null,
) : RecyclerView.Adapter<PostItemAdapter.MyViewHolder>() {

    private var postList = listOf<PostModel>()
    private val user: UserModel = userViewModel.getLoggedUserFromPref()!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemHomeAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = postList[position]
        holder.bindPost(post)
        holder.changeLikeStatus(post)
        holder.changeDisplay(post)
    }

    override fun getItemCount(): Int = postList.size
    inner class MyViewHolder(private val binding: ItemHomeAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindPost(post: PostModel) {
            binding.apply {
                val circular =
                    CircularProgressDrawable(imageViewAdapterHomeMainImagePost.context).apply {
                        strokeWidth = 10f
                        centerRadius = 50f
                        start()
                    }
                Glide.with(imageViewAdapterHomeMainImagePost)
                    .load(post.imagePath)
                    .placeholder(circular)
                    .centerCrop()
                    .into(imageViewAdapterHomeMainImagePost)

                Glide.with(imageViewAdapterHomeProfileImagePost)
                    .load(post.userAuthorImage)
                    .placeholder(circular)
                    .into(imageViewAdapterHomeProfileImagePost)

                textViewAdapterHomeNameUserPost.text = post.userAuthorName
                textViewAdapterHomeDescriptionUserPost.text = post.description
                textViewAdapterHomeUserNamePost.text = post.userAuthorName

            }
        }

        fun changeLikeStatus(post: PostModel) {
            binding.apply {
                imageViewAdapterHomeLikePost.setOnClickListener {
                    likeViewModel.getNumberLikes(post)
                }
            }
        }

        fun changeDisplay(post: PostModel) {
            likeViewModel.viewModelScope.launch {
                likeViewModel.checkIsLiked(post).addSnapshotListener { value, error ->
                    if (value != null) {
                        binding.apply {
                            if (value.contains(user.uid)) {
                                binding.apply {
                                    imageViewAdapterHomeLikePost.setBackgroundResource(R.drawable.ic_baseline_pets_filled)
                                }
                            } else {
                                imageViewAdapterHomeLikePost.setBackgroundResource(R.drawable.ic_baseline_pets_transparent)
                            }
                        }
                    } else {
                        error?.printStackTrace()
                    }
                }
            }

        }

    }

    fun getPostsById(userId: String) {
        postViewModel?.viewModelScope?.launch {
            postViewModel.getPostsById().addSnapshotListener { docs, error ->
                if (docs != null) {
                    val arrayPost: ArrayList<PostModel> = arrayListOf()
                    for (doc in docs.documents) {
                        if (doc.data!![postFieldAuthorId] == userId)
                            arrayPost.add(mapToPost(doc))
                    }
                    setData(arrayPost)
                } else {
                    setData(emptyList())
                }

            }
        }
    }

    fun setData(posts: List<PostModel>) {
        val diffUtil = ListDiffUtil(postList, posts)
        val result = DiffUtil.calculateDiff(diffUtil)
        postList = posts
        result.dispatchUpdatesTo(this)
    }

}