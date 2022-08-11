package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentPostDetailBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.LikesViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding: FragmentPostDetailBinding get() = _binding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    @Inject
    lateinit var likesViewModelFactory: LikesViewModelFactory
    private val likeViewModel: LikeViewModel by viewModels { likesViewModelFactory }

    private lateinit var user: UserModel
    private lateinit var post: PostModel

    private lateinit var navBar: BottomNavigationView

    private val args by navArgs<PostDetailFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.bnv_main_id)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_postDetailFragment_to_homeFragment)
                    navBar.visibility = View.VISIBLE
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        navBar.visibility = View.GONE
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        post = args.selectedPost
        initObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewPostDetailLikePost.setOnClickListener {
            lifecycleScope.launch {
                changeLikeStatus(post)
            }
        }

        binding.toolbarPostDetailTopFragment.setNavigationOnClickListener {
            findNavController().popBackStack()
            navBar.visibility = View.VISIBLE
        }

        binding.imageViewPostDetailProfileImagePost.setOnClickListener {
            navigateToProfile()
        }


    }

    private fun navigateToProfile() {
        if (post.authorId != user.uid) {
            val direction =
                PostDetailFragmentDirections.actionPostDetailFragmentToProfileUserFragment(post.authorId)
            findNavController().navigate(direction)
        } else {
            val direction =
                PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment()
            findNavController().navigate(direction)
            navBar.visibility = View.VISIBLE
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            userViewModel.uiUserProfileStateFlow.collect { userState ->
                when (userState) {
                    UiState.Initial -> {
                        userViewModel.getUserById(args.userId)
                    }
                    UiState.Loading -> {
                        CustomDialogLoading().startLoading(requireActivity())
                    }
                    UiState.Error -> {
                        CustomDialogLoading().dismissLoading()
                        Snackbar.make(
                            binding.root,
                            getString(R.string.something_wrong_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    UiState.Success -> {
                        CustomDialogLoading().dismissLoading()
                        user = userViewModel.userProfileFlow.value!!
                        displayData()
                    }

                }
            }
        }

        lifecycleScope.launch {
            verifyColor()
        }


    }

    private fun displayData() {
        binding.apply {
            val circular =
                CircularProgressDrawable(imageViewPostDetailMainImagePost.context).apply {
                    strokeWidth = 10f
                    centerRadius = 50f
                    start()
                }
            Glide.with(imageViewPostDetailMainImagePost)
                .load(post.imagePath)
                .placeholder(circular)
                .centerCrop()
                .into(imageViewPostDetailMainImagePost)

            Glide.with(imageViewPostDetailProfileImagePost)
                .load(post.userAuthorImage)
                .placeholder(circular)
                .into(imageViewPostDetailProfileImagePost)

            textViewPostDetailNameUserPost.text = post.userAuthorName
            textViewPostDetailDescriptionUserPost.text = post.description
            textViewPostDetailUserNamePost.text = post.userAuthorName

            imageViewPostDetailCommentariesPost.setOnClickListener {
                val directions =
                    PostDetailFragmentDirections.actionPostDetailFragmentToCommentFragment(
                        user,
                        post
                    )

                findNavController().navigate(directions)
            }

            toolbarPostDetailTopFragment.title = user.userName

        }
    }

    private suspend fun verifyColor() {
        likeViewModel.checkIsLiked(post).addSnapshotListener { value, error ->
            if (value != null) {
                binding.apply {
                    if (value.contains(args.userId)) {
                        imageViewPostDetailLikePost.setBackgroundResource(R.drawable.ic_baseline_pets_filled)
                    } else {
                        imageViewPostDetailLikePost.setBackgroundResource(R.drawable.ic_baseline_pets_transparent)
                    }
                }
            } else {
                error?.printStackTrace()
            }
        }
    }

    private suspend fun changeLikeStatus(post: PostModel) {
        likeViewModel.getNumberLikes(post)
        verifyColor()
    }
}