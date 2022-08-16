package com.raveline.petinlove.presentation.adapters

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.model.mapToPost
import com.raveline.petinlove.databinding.ItemHomeAdapterBinding
import com.raveline.petinlove.domain.utils.*
import com.raveline.petinlove.presentation.fragments.*
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.util.*

class PostItemAdapter(
    private val likeViewModel: LikeViewModel,
    private val userViewModel: UserViewModel,
    private val postViewModel: PostViewModel? = null,
    private val fragment: Fragment? = null
) : RecyclerView.Adapter<PostItemAdapter.MyViewHolder>() {

    private var postList = listOf<PostModel>()
    private lateinit var user: UserModel

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
        holder.setVisibilityForUserFragments()
    }

    override fun getItemCount(): Int = postList.size
    inner class MyViewHolder(private val binding: ItemHomeAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindPost(post: PostModel) {
            user = userViewModel.getLoggedUserFromPref()!!
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

                if (user.uid == post.authorId) {
                    imageViewAdapterHomeProfileImagePost.setOnClickListener {
                        goToMyProfile()
                    }
                } else {
                    if (fragment != null) {
                        imageViewAdapterHomeProfileImagePost.setOnClickListener {
                            goToProfileAuthor(post.authorId)
                        }
                    }
                }
            }

            binding.imageViewAdapterHomeMainImagePost.setOnClickListener {
                goToPostDetail(post)
            }

            binding.imageViewAdapterHomeCommentariesPost.setOnClickListener {
                goToComments(post)
            }

            binding.imageViewAdapterHomeSavePost.setOnClickListener {
                setSavedPost(post)
            }

        }

        fun setVisibilityForUserFragments() {
            when (fragment) {
                is ProfileUserFragment -> {
                    visibilityToolbarGone()
                }
                is ProfileFragment -> {
                    visibilityToolbarGone()
                }
            }
        }

        private fun visibilityToolbarGone() {
            binding.apply {
                toolbarAdapterHomeFragment.visibility = GONE
            }
        }

        private fun setSavedPost(post: PostModel) {

            val postMap: HashMap<String, Any> = hashMapOf(
                postFieldPostId to post.postId,
                postFieldUserAuthorName to user.userName,
                postFieldUserAuthorImage to user.userProfileImage,
                postFieldLikes to post.likes,
                postFieldAuthorId to user.uid,
                postFieldDescription to post.description,
                postFieldImagePath to post.imagePath,
                postFieldDatePosted to Timestamp(Date(System.currentTimeMillis()))
            )
            likeViewModel.setOrRemoveSavePost(post, user, postMap)


        }

        private fun goToComments(post: PostModel) {
            val navController = Navigation.findNavController(binding.root)

            when (fragment) {
                is ProfileUserFragment -> {
                    val directions =
                        ProfileUserFragmentDirections.actionProfileUserFragmentToCommentFragment(
                            user,
                            post
                        )
                    navController.navigate(directions)
                }
                is ProfileFragment -> {
                    val directions =
                        ProfileFragmentDirections.actionProfileFragmentToCommentFragment(user, post)
                    navController.navigate(directions)
                }
                is HomeFragment -> {
                    val directions =
                        HomeFragmentDirections.actionHomeFragmentToCommentFragment(user, post)
                    navController.navigate(directions)
                }

                is PostDetailFragment -> {
                    val directions =
                        PostDetailFragmentDirections.actionPostDetailFragmentToCommentFragment(
                            user,
                            post
                        )
                    navController.navigate(directions)
                }
            }
        }

        private fun goToPostDetail(post: PostModel) {

            val navController = Navigation.findNavController(binding.root)

            when (fragment) {
                is ProfileFragment -> {
                    val directions =
                        ProfileFragmentDirections.actionProfileFragmentToPostDetailFragment(
                            post,
                            user.uid
                        )
                    navController.navigate(directions)
                }

                is HomeFragment -> {
                    val directions =
                        HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(
                            post,
                            user.uid
                        )
                    navController.navigate(directions)
                }
            }


        }

        private fun goToProfileAuthor(profileId: String) {
            val navController = Navigation.findNavController(binding.root)
            val directions =
                HomeFragmentDirections.actionHomeFragmentToProfileUserFragment(profileId)
            navController.navigate(directions)
        }

        private fun goToMyProfile() {
            val navController = Navigation.findNavController(binding.root)
            navController.navigate(R.id.action_homeFragment_to_profileFragment)
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

            likeViewModel.viewModelScope.launch {
                likeViewModel.checkIsSaved(post, user.uid).addSnapshotListener { doc, error ->
                    if (doc != null) {
                        binding.apply {
                            if (doc.exists()) {
                                imageViewAdapterHomeSavePost.setBackgroundResource(R.drawable.ic_baseline_turned_in_24)
                            } else {
                                imageViewAdapterHomeSavePost.setBackgroundResource(R.drawable.ic_baseline_turned_in_not_24)
                            }
                        }
                    }

                    if (error != null) {
                        binding.imageViewAdapterHomeSavePost.setBackgroundResource(R.drawable.ic_baseline_turned_in_not_24)
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