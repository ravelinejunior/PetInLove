package com.raveline.petinlove.presentation.adapters

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
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
import java.io.File
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

            binding.imageViewAdapterHomeShareDownloadPost.setOnClickListener {
                shareImage(post.imagePath)
            }

            binding.imageViewAdapterHomeMainImagePost.setOnLongClickListener {

                checkGalleryImageAndDownloadImage(post)
                true
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

        private fun checkGalleryImageAndDownloadImage(post: PostModel) {
            Dexter.withContext(fragment?.context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        downloadImageNew(System.currentTimeMillis().toString(), post.imagePath)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                    }
                }).check()
        }

        private fun downloadImageNew(filename: String, downloadUrlOfImage: String) {
            try {
                val dm = fragment?.requireActivity()
                    ?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                val downloadUri: Uri = Uri.parse(downloadUrlOfImage)
                val request = DownloadManager.Request(downloadUri)
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(true)
                    .setTitle(filename)
                    .setMimeType("image/*") // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_PICTURES,
                        File.separator.toString() + filename + ".jpg"
                    )
                dm!!.enqueue(request)
                Toast.makeText(fragment?.context, "Image download started.", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(fragment?.context, "Image download failed.", Toast.LENGTH_SHORT)
                    .show()

                Log.e("TAGImage", e.message.toString())
            }
        }

        private fun shareImage(imagePath: String) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val message = "I'm sharing this awesome pic with you. So cute. \n\n\t"
                shareIntent.putExtra(Intent.EXTRA_TEXT, message + imagePath)
                fragment?.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            } catch (e: Exception) {
                e.toString();
            }
        }


        private fun setSavedPost(post: PostModel) {

            val postMap: HashMap<String, Any> = hashMapOf(
                postFieldPostId to post.postId,
                postFieldUserAuthorName to post.userAuthorName,
                postFieldUserAuthorImage to post.userAuthorImage,
                postFieldLikes to post.likes,
                postFieldAuthorId to post.authorId,
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

                is SavedPostsFragment -> {
                    val directions =
                        SavedPostsFragmentDirections.actionSavedPostsFragmentToCommentFragment(
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

                is ProfileUserFragment -> {
                    val directions =
                        ProfileUserFragmentDirections.actionProfileUserFragmentToPostDetailFragment(
                            post,
                            user.uid
                        )
                    navController.navigate(directions)
                }

                is SavedPostsFragment -> {
                    val directions =
                        SavedPostsFragmentDirections.actionSavedPostsFragmentToPostDetailFragment(
                            post,
                            user.uid
                        )
                    navController.navigate(directions)
                }
            }


        }

        private fun goToProfileAuthor(profileId: String) {
            val navController = Navigation.findNavController(binding.root)

            when (fragment) {
                is SavedPostsFragment -> {
                    val directions =
                        SavedPostsFragmentDirections.actionSavedPostsFragmentToProfileUserFragment(
                            profileId
                        )
                    navController.navigate(directions)
                }

                is HomeFragment -> {
                    val directions =
                        HomeFragmentDirections.actionHomeFragmentToProfileUserFragment(profileId)
                    navController.navigate(directions)
                }

                is SearchPersonFragment -> {
                    val directions =
                        SearchPersonFragmentDirections.actionSearchPersonFragmentToProfileUserFragment(
                            profileId
                        )
                    navController.navigate(directions)
                }

                is CommentFragment -> {
                    val directions =
                        CommentFragmentDirections.actionCommentFragmentToProfileUserFragment(
                            profileId
                        )
                    navController.navigate(directions)
                }

            }

        }

        private fun goToMyProfile() {
            val navBar =
                fragment?.requireActivity()?.findViewById<BottomNavigationView>(R.id.bnv_main_id)
            val menu = navBar?.menu
            fragment?.parentFragmentManager?.beginTransaction()
                ?.remove(fragment)
                ?.replace(fragment.id, ProfileFragment())?.commit()

            val item = menu?.getItem(ProfileFragment().id)
            item?.isChecked = true

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
            postViewModel.getPostsById().addSnapshotListener { docs, _ ->
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