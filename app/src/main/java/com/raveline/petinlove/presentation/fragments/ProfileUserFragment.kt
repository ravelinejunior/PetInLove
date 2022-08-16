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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentProfileUserBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.presentation.adapters.PostItemAdapter
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.LikesViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.PostViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileUserFragment : Fragment() {

    private var _binding: FragmentProfileUserBinding? = null
    private val binding: FragmentProfileUserBinding get() = _binding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    @Inject
    lateinit var postViewModelFactory: PostViewModelFactory
    private val postViewModel: PostViewModel by viewModels { postViewModelFactory }

    @Inject
    lateinit var likesViewModelFactory: LikesViewModelFactory
    private val likeViewModel: LikeViewModel by viewModels { likesViewModelFactory }

    private val homeAdapter: PostItemAdapter by lazy {
        PostItemAdapter(likeViewModel, userViewModel, postViewModel, this)
    }

    private lateinit var user: UserModel

    private lateinit var navBar: BottomNavigationView

    private val userId by navArgs<ProfileUserFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        navBar = requireActivity().findViewById(R.id.bnv_main_id)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
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
        _binding = FragmentProfileUserBinding.inflate(inflater, container, false)

        binding.toolbarEditProfileFragment.setNavigationOnClickListener {
            navBar.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_profileUserFragment_to_homeFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }


    private fun displayUserInfo() {
        binding.apply {
            textViewProfileUserFragmentFollowers.text = user.userFollowers.toString()
            textViewProfileUserFragmentFollowing.text = user.userFollowing.toString()
            textViewProfileUserFragmentDescription.text = user.userDescription
            textViewProfileUserFragmentPublications.text = user.userPublications.toString()
            Glide.with(requireContext()).load(user.userProfileImage)
                .into(imageViewProfileUserFragment)

            toolbarEditProfileFragment.title = user.userName
        }
    }

    private fun initObservers() {

        lifecycleScope.launch {
            postViewModel.uiStateFlow.collect { uiState ->
                when (uiState) {
                    UiState.Initial -> {
                        postViewModel.getPostsFromServer()
                    }
                    UiState.Loading -> {
                    }
                    UiState.Error -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.something_wrong_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    UiState.Success -> {
                    }

                }
            }
        }

        lifecycleScope.launch {
            userViewModel.uiUserProfileStateFlow.collect { userState ->
                when (userState) {
                    UiState.Initial -> {
                        userViewModel.getUserById(userId.userId)
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

                        lifecycleScope.launch {
                            postViewModel.postsFlow.collect { posts ->
                                if (posts.isNotEmpty()) {
                                    homeAdapter.getPostsById(user.uid)
                                    setupRecyclerView()
                                }

                            }

                        }
                        displayUserInfo()

                    }

                }
            }
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerViewProfileUserFragmentPublications.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}