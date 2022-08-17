package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.CommentModel
import com.raveline.petinlove.databinding.FragmentCommentBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.SystemFunctions.hideKeyboard
import com.raveline.petinlove.presentation.adapters.CommentAdapter
import com.raveline.petinlove.presentation.viewmodels.CommentViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.CommentViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding: FragmentCommentBinding get() = _binding!!

    @Inject
    lateinit var commentsViewModelFactory: CommentViewModelFactory
    private val commentsViewModel: CommentViewModel by viewModels { commentsViewModelFactory }

    private val args by navArgs<CommentFragmentArgs>()

    private val commentAdapter: CommentAdapter by lazy {
        CommentAdapter()
    }

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.bnv_main_id)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_commentFragment_to_homeFragment)
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
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        initObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ibvCommentFragment.setOnClickListener {
            hideKeyboard()
            setComment()
        }

        binding.apply {
            toolbarCommentFragment.title = getString(R.string.comments_string_title)
            toolbarCommentFragment.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_commentFragment_to_homeFragment)
                navBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setComment() = lifecycleScope.launch {
        val user = args.user
        val post = args.post
        val commentId = UUID.randomUUID().toString().replace("-", "").trim()
        val commentary = binding.etCommentFragment.text?.trim()
        if (TextUtils.isEmpty(commentary)) {
            Snackbar.make(
                binding.root,
                getString(R.string.add_a_cute_comment_here),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            binding.etCommentFragment.text?.clear()
            binding.etCommentFragment.clearFocus()
            val comment = CommentModel(
                idUser = user.uid,
                idPost = post.postId,
                idComment = commentId,
                userProfileImage = user.userProfileImage,
                userName = user.userName,
                comment = commentary.toString(),
                dateCreation = Timestamp.now()
            )
            commentsViewModel.postComment(post.postId, commentId, comment)
        }
    }

    private fun initObservers() {

        lifecycleScope.launch {
            commentsViewModel.uiStateFlow.collect { uiState ->
                when (uiState) {
                    UiState.Initial -> {
                        commentsViewModel.getCommentsByPost(args.post.postId)
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
                        setupRecyclerView()
                    }
                }
            }
        }

        lifecycleScope.launch {
            commentsViewModel.commentStateFlow.collect { commentState ->
                when (commentState) {
                    UiState.Initial -> {
                    }
                    UiState.Loading -> {
                        CustomDialogLoading().startLoading(requireActivity())
                    }
                    UiState.Error -> {
                        CustomDialogLoading().dismissLoading()
                        Snackbar.make(
                            binding.root,
                            getString(R.string.comment_error_message),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    UiState.Success -> {
                        CustomDialogLoading().dismissLoading()
                        commentsViewModel.getCommentsByPost(args.post.postId)
                    }
                }
            }
        }

        lifecycleScope.launch {
            commentsViewModel.commentariesFlow.collect { comments ->
                if (comments.isNotEmpty()) {
                    commentAdapter.setData(comments)
                    setupRecyclerView()
                }
            }
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerViewCommentFragment.apply {
            setHasFixedSize(true)
            setHasTransientState(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = SlideInUpAnimator(OvershootInterpolator(2f))
            adapter = commentAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}