package com.raveline.petinlove.presentation.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentAddPostBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.SystemFunctions.hideKeyboard
import com.raveline.petinlove.domain.utils.postStoreKeyProfileImage
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.PostViewModelFactory
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AddPostFragment : Fragment() {
    private var _binding: FragmentAddPostBinding? = null
    private val binding: FragmentAddPostBinding get() = _binding!!

    private lateinit var navBar: BottomNavigationView

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var postViewModelFactory: PostViewModelFactory
    private val postViewModel: PostViewModel by viewModels { postViewModelFactory }

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    private var imageUri: Uri? = null

    private lateinit var user: UserModel

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
        user = userViewModel.getLoggedUserFromPref()!!
        navBar.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)

        initObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivAddPostFragment.setOnClickListener {
                pickFromGallery()
            }
            tvAddPostFragmentImage.setOnClickListener {
                pickFromGallery()
            }

            toolbarAddPostFragment.setNavigationOnClickListener {
                findNavController().popBackStack()
                navBar.visibility = View.VISIBLE
            }

            imageButtonAddPostFragmentSave.setOnClickListener {
                saveNewPost()
            }
        }
    }

    private fun saveNewPost() {
        val description = binding.textInputAddPostFragmentDescription.text.toString()
        hideKeyboard()

        if (imageUri != null) {
            lifecycleScope.launch {
                val extension = "${System.currentTimeMillis()}.jpeg"
                postViewModel.savePostOnServer(user, imageUri, extension, description)
            }
        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.image_not_found_msg),
                Snackbar.LENGTH_SHORT
            ).show()
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
                        Snackbar.make(
                            binding.root,
                            getString(R.string.successfully_add_post_msg),
                            Snackbar.LENGTH_SHORT
                        ).show().run {
                            navBar.visibility = View.VISIBLE
                            findNavController().navigate(R.id.action_addPostFragment_to_homeFragment)
                        }
                    }

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == postStoreKeyProfileImage) {
            if (data != null) {
                imageUri = data.data!!

                Glide.with(requireContext()).load(imageUri)
                    .into(binding.ivAddPostFragment)
            }
        }
    }


    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.select_image_msg)
            ), postStoreKeyProfileImage
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}