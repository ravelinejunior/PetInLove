package com.raveline.petinlove.presentation.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentEditProfileBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.SystemFunctions.hideKeyboard
import com.raveline.petinlove.domain.utils.mediaStoreKeyProfileImage
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding: FragmentEditProfileBinding get() = _binding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        navBar = requireActivity().findViewById(R.id.bnv_main_id)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                    navBar.visibility = VISIBLE
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        navBar.visibility = GONE
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        setObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }


    private fun initListeners() {
        binding.apply {
            buttonEditProfileFragmentSave.setOnClickListener {
                lifecycleScope.launch {
                    getFieldsAndEditUser()
                }
            }

            imageViewEditProfileFragment.setOnClickListener {
                getImageFromPhone()
            }

            textViewEditProfileFragmentImage.setOnClickListener {
                getImageFromPhone()
            }

            toolbarEditProfileFragment.setNavigationOnClickListener {
                navBar.visibility = VISIBLE
                //  findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                findNavController().popBackStack()
                findNavController().popBackStack()
            }
        }
    }

    private fun getImageFromPhone() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, mediaStoreKeyProfileImage)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mediaStoreKeyProfileImage && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                userViewModel.imagePath = data.data.toString()
                Glide.with(requireContext()).load(userViewModel.imagePath)
                    .into(binding.imageViewEditProfileFragment)
            }

        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            userViewModel.userModel.collectLatest { user ->
                if (user != null) {
                    displayData(user)
                } else {
                    displayData(userViewModel.getLoggedUserFromPref()!!)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            userViewModel.uiStateFlow.collectLatest { uiState ->
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
                            userViewModel.result,
                            Snackbar.LENGTH_SHORT
                        ).show().also {
                            navBar.visibility = VISIBLE
                            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                        }
                    }
                    UiState.NoConnection -> {
                        CustomDialogLoading().dismissLoading()
                        Snackbar.make(
                            binding.root,
                            userViewModel.result,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun displayData(user: UserModel) {
        binding.apply {
            textInputEditProfileName.setText(user.userName)
            textInputEditProfilePhoneNumber.setText(user.userPhoneNumber)
            textInputEditProfileFragmentDescription.setText(user.userDescription)
            if (user.userProfileImage.isNotEmpty()) {
                Glide.with(requireContext()).load(user.userProfileImage)
                    .into(imageViewEditProfileFragment)
            }

        }
    }

    private fun getFieldsAndEditUser() {
        hideKeyboard()
        binding.apply {
            val name = textInputEditProfileName.text?.toString()?.trim()
            val phone = textInputEditProfilePhoneNumber.text?.toString()?.trim()
            val description = textInputEditProfileFragmentDescription.text?.toString()?.trim()

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(context, getString(R.string.name_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(context, getString(R.string.phone_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            if (TextUtils.isEmpty(description)) {
                Toast.makeText(
                    context,
                    getString(R.string.description_empty_msg),
                    Toast.LENGTH_SHORT
                )
                    .show()

                return
            }

            userViewModel.editUserData(name.toString(), phone.toString(), description.toString())

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}