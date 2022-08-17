package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentProfileBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.presentation.adapters.PostItemAdapter
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.LikesViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.PostViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    private lateinit var navBar: BottomNavigationView

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

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
        PostItemAdapter(likeViewModel, userViewModel, postViewModel,this)
    }


    private lateinit var user: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.bnv_main_id)
        navBar.visibility = View.VISIBLE
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().onBackPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        user = userViewModel.getLoggedUserFromPref()!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            textViewProfileFragmentSignOut.setOnClickListener {
                navBar.visibility = View.GONE
                userViewModel.logout().also {
                    findNavController().navigate(R.id.action_profileFragment_to_mainFragment)
                }
            }

            textViewProfileFragmentEdit.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }
        }
        displayUserInfo()
        initObservers()
    }

    private fun displayUserInfo() {
        binding.apply {
            textViewProfileFragmentFollowers.text = user.userFollowers.toString()
            textViewProfileFragmentFollowing.text = user.userFollowing.toString()
            textViewProfileFragmentDescription.text = user.userDescription
            textViewProfileFragmentPublications.text = user.userPublications.toString()
            Glide.with(requireContext()).load(user.userProfileImage)
                .into(imageViewProfileFragment)
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
                    }

                }
            }
        }

        lifecycleScope.launch {
            postViewModel.postsFlow.collect { posts ->
                if (posts.isNotEmpty()) {
                    homeAdapter.getPostsById(user.uid)
                    setupRecyclerView()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewProfileFragmentPublications.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = SlideInUpAnimator(OvershootInterpolator(2f))
            adapter = homeAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}