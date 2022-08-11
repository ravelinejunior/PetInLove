package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.CommentModel
import com.raveline.petinlove.databinding.ItemCommentsAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.domain.utils.SystemFunctions

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.MyViewHolder>() {

    private var commentsList = emptyList<CommentModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemCommentsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       val comment = commentsList[position]
        holder.bindComment(comment)
    }

    override fun getItemCount(): Int = commentsList.size

    inner class MyViewHolder(private val binding: ItemCommentsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindComment(comment: CommentModel) {
            binding.apply {
                tvCommentsAdapterUserName.text = comment.userName
                tvCommentsAdapterComment.text = "''${comment.comment}''"
                tvCommentsAdapterDateCreated.text = SystemFunctions.convertTimeStampToString(comment.dateCreation.toDate())
                Glide.with(ivCommentsAdapterProfileImage.context).load(comment.userProfileImage)
                    .error(R.drawable.camera_choose)
                    .into(ivCommentsAdapterProfileImage)
            }
        }
    }

    fun setData(comments: List<CommentModel>) {
        val diffUtil = ListDiffUtil(commentsList, comments)
        val result = DiffUtil.calculateDiff(diffUtil)
        commentsList = comments
        result.dispatchUpdatesTo(this)
    }
}