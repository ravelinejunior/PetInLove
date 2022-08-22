package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.StoryModel
import com.raveline.petinlove.databinding.FragmentHomeBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.firstRegisterUserImage
import com.raveline.petinlove.presentation.adapters.PostItemAdapter
import com.raveline.petinlove.presentation.adapters.StoryAdapter
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.LikesViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.PostViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.StoryViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = _binding!!

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

    @Inject
    lateinit var storyViewModelFactory: StoryViewModelFactory
    private val storyViewModel: StoryViewModel by viewModels { storyViewModelFactory }

    private val homeAdapter: PostItemAdapter by lazy {
        PostItemAdapter(likeViewModel, userViewModel, fragment = this)
    }

    private val storiesAdapter: StoryAdapter by lazy {
        StoryAdapter(storyViewModel, this)
    }

    private var navBar: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    lifecycleScope.launch {
                        postViewModel.getPostsFromServer()
                        findNavController().navigate(R.id.action_homeFragment_self)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupStoriesRecyclerView()
        initObservers()

        navBar = activity?.findViewById(R.id.bnv_main_id)!!
        navBar?.visibility = View.VISIBLE
    }

    private fun setupToolbar() {
        binding.toolbarHomeFragment.apply {
            inflateMenu(R.menu.menu_home_fragment)
            title = "AuAuCome"
            setOnMenuItemClickListener { item ->

                when (item.itemId) {
                    R.id.chatMenuHomeId -> {

                    }
                    R.id.logoutMenuHomeId -> {
                        userViewModel.logout().also {
                            findNavController().navigate(R.id.action_homeFragment_to_mainFragment)
                        }
                    }
                }
                true
            }
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
                    homeAdapter.setData(posts)
                    setupRecyclerView()
                }
            }
        }

        lifecycleScope.launch {
            storyViewModel.storyFlow.collectLatest { stories ->
                if (stories.isEmpty()) {
                    val story = listOf(
                        setUnitStory()
                    )
                    storiesAdapter.setData(story)
                    Log.i("TAGStories", stories.toString())
                } else {
                    val mStories = arrayListOf<StoryModel>()
                    mStories.addAll(stories)
                    mStories.add(0, setUnitStory())
                    storiesAdapter.setData(mStories)
                    setupStoriesRecyclerView()
                    Log.i("MTAGStories", mStories.toString())
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewHomeFragment.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = homeAdapter
        }
    }

    private fun setupStoriesRecyclerView() {
        binding.recyclerVieHomeFragmentStories.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = storiesAdapter
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setUnitStory(): StoryModel = StoryModel(
        false,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        firstRegisterUserImage,
        userViewModel.userModel.value?.userName.toString(),
        userViewModel.userModel.value?.uid.toString(),
        "",
        0
    )

}