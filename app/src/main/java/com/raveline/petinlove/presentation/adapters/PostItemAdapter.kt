package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.databinding.ItemHomeAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil

class PostItemAdapter : RecyclerView.Adapter<PostItemAdapter.MyViewHolder>() {

    private var postList = listOf<PostModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemHomeAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = postList[position]
        holder.bindPost(post)
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
                    .into(imageViewAdapterHomeMainImagePost)

                Glide.with(imageViewAdapterHomeProfileImagePost)
                    .load(post.userImage)
                    .placeholder(circular)
                    .into(imageViewAdapterHomeProfileImagePost)

                textViewAdapterHomeNameUserPost.text = post.userName
                textViewAdapterHomeDescriptionUserPost.text = post.description
                textViewAdapterHomeUserNamePost.text = post.userName

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