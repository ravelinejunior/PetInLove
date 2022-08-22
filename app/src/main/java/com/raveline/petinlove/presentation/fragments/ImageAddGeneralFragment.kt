package com.raveline.petinlove.presentation.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentImageAddGeneralBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.SystemFunctions
import com.raveline.petinlove.domain.utils.storyStoreKeyImage
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.StoryViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ImageAddGeneralFragment : Fragment() {

    private var _binding: FragmentImageAddGeneralBinding? = null
    private val binding: FragmentImageAddGeneralBinding get() = _binding!!

    @Inject
    lateinit var storyViewModelFactory: StoryViewModelFactory
    private val storyViewModel: StoryViewModel by viewModels { storyViewModelFactory }

    private lateinit var user: UserModel

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navBar = requireActivity().findViewById(R.id.bnv_main_id)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_imageAddGeneralFragment_to_homeFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        navBar.visibility = View.GONE

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentImageAddGeneralBinding.inflate(inflater, container, false)
        initObservers()
        getImageFromPhone()
        return binding.root

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == storyStoreKeyImage && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val imageUri = data.data!!
                user = SystemFunctions.getLoggedUserFromPref(requireContext())!!
                lifecycleScope.launch {
                    val extension = "${System.currentTimeMillis()}.jpeg"
                    storyViewModel.setStoryOnServer(user, imageUri, extension)

                }
            } else {
                findNavController().navigate(R.id.action_imageAddGeneralFragment_to_homeFragment)
            }
        }
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            storyViewModel.uiStateFlow.collectLatest { uiState ->
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
                        ).show().also {
                            findNavController().navigate(R.id.action_imageAddGeneralFragment_to_homeFragment)
                        }
                    }
                    UiState.Success -> {
                        CustomDialogLoading().dismissLoading()
                        Snackbar.make(
                            binding.root,
                            getString(R.string.successfully_add_post_msg),
                            Snackbar.LENGTH_SHORT
                        ).show().run {
                            navBar.visibility = View.VISIBLE
                            findNavController().navigate(R.id.action_imageAddGeneralFragment_to_homeFragment)
                        }
                    }

                }
            }
        }
    }


    private fun getImageFromPhone() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes = arrayOf("image/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.select_image_msg)
            ), storyStoreKeyImage
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}