package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentSavedPostsBinding
import com.raveline.petinlove.presentation.adapters.PostItemAdapter
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.LikesViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.PostViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SavedPostsFragment : Fragment() {

    private var _binding: FragmentSavedPostsBinding? = null
    private val binding: FragmentSavedPostsBinding get() = _binding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    @Inject
    lateinit var postViewModelFactory: PostViewModelFactory
    private val postViewModel: PostViewModel by viewModels { postViewModelFactory }

    @Inject
    lateinit var likesViewModelFactory: LikesViewModelFactory
    private val likeViewModel: LikeViewModel by viewModels { likesViewModelFactory }

    private val postAdapter: PostItemAdapter by lazy {
        PostItemAdapter(likeViewModel, userViewModel, null, this)
    }

    private lateinit var user: UserModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().onBackPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedPostsBinding.inflate(inflater, container, false)

        initObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun initObservers() {
        user = userViewModel.getLoggedUserFromPref()!!
        lifecycleScope.launch {
            postViewModel.uiStateFlow.collect { uiState ->
                when (uiState) {
                    UiState.Initial -> {
                        likeViewModel.getSavedPosts(user.uid)
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
                        setupRecyclerView()
                    }

                }
            }
        }

        lifecycleScope.launch {
            likeViewModel.postsFlow.collectLatest { savedPosts ->
                if (savedPosts.isNotEmpty()) {
                    postAdapter.setData(savedPosts)
                    setupRecyclerView()
                }
            }
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerViewSavedPostFragment.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}