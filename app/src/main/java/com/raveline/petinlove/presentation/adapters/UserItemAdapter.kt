package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.ItemSearchUserAdapterBinding
import com.raveline.petinlove.domain.utils.ListDiffUtil
import com.raveline.petinlove.presentation.fragments.ProfileFragment
import com.raveline.petinlove.presentation.fragments.SearchPersonFragmentDirections

class UserItemAdapter(
    private val fragment: Fragment? = null,
    private val firebaseAuth: FirebaseAuth? = null
) : RecyclerView.Adapter<UserItemAdapter.UserViewHolder>() {

    private var userList = emptyList<UserModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            ItemSearchUserAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userBind(user)
        holder.goToProfile(user)
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

        fun goToProfile(user: UserModel) {
            userBinding.root.setOnClickListener {
                setProfile(user)
            }
        }

        private fun setProfile(user: UserModel) {
            val navController = Navigation.findNavController(userBinding.root)

            if (user.uid == firebaseAuth?.uid) {
                setTransaction()
            } else {
                val directions =
                    SearchPersonFragmentDirections.actionSearchPersonFragmentToProfileUserFragment(
                        user.uid
                    )
                navController.navigate(directions)

            }

        }

        private fun setTransaction() {
            val navBar =
                fragment?.requireActivity()
                    ?.findViewById<BottomNavigationView>(R.id.bnv_main_id)
            val menu = navBar?.menu
            fragment?.parentFragmentManager?.beginTransaction()
                ?.replace(fragment.id, ProfileFragment())
                ?.remove(fragment)
                ?.commit()

            val item = menu?.getItem(ProfileFragment().id)
            item?.isChecked = true
        }


    }

    fun setData(users: List<UserModel>) {
        val listUtil = ListDiffUtil(userList, users)
        val result = DiffUtil.calculateDiff(listUtil)
        userList = users
        result.dispatchUpdatesTo(this)
    }

}