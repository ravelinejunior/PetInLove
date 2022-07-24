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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.databinding.FragmentHomeBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.presentation.adapters.PostItemAdapter
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.PostViewModelFactory
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
    lateinit var postViewModelFactory: PostViewModelFactory
    private val postViewModel: PostViewModel by viewModels { postViewModelFactory }

    private val homeAdapter: PostItemAdapter by lazy {
        PostItemAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

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
        initObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
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
                        firebaseAuth.signOut().also {
                            findNavController().navigate(R.id.action_homeFragment_to_homeActivity)
                        }
                    }
                }
                true
            }
        }
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            postViewModel.uiStateFlow.collectLatest { uiState ->
                when (uiState) {
                    UiState.Initial -> {}
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
            postViewModel.postsFlow.collectLatest { posts ->
                if (posts.isNotEmpty()) {
                    homeAdapter.setData(posts)
                    setupRecyclerView()
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


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}