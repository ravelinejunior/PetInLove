package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.ItemSearchUserAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil

class UserItemAdapter : RecyclerView.Adapter<UserItemAdapter.UserViewHolder>() {

    private var userList = emptyList<UserModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            ItemSearchUserAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userBind(user)
    }

    override fun getItemCount(): Int = userList.size

    inner class UserViewHolder(private val userBinding: ItemSearchUserAdapterBinding) :
        RecyclerView.ViewHolder(userBinding.root) {

        fun userBind(user: UserModel) {

            userBinding.apply {
                val circular =
                    CircularProgressDrawable(ivSearchUserAdapter.context).apply {
                        strokeWidth = 10f
                        centerRadius = 50f
                        start()
                    }

                Glide.with(ivSearchUserAdapter)
                    .load(user.userProfileImage)
                    .placeholder(circular)
                    .into(ivSearchUserAdapter)

                tvSearchUserAdapterName.text = user.userName

            }
        }

    }

    fun setData(users: List<UserModel>) {
        val listUtil = ListDiffUtil(userList, users)
        val result = DiffUtil.calculateDiff(listUtil)
        userList = users
        result.dispatchUpdatesTo(this)
    }

}