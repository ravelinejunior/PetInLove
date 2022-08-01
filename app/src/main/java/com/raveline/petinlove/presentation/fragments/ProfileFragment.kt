package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentProfileBinding
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
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

    private lateinit var user: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navBar = requireActivity().findViewById(R.id.bnv_main_id)
        navBar.visibility = View.VISIBLE
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
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
                userViewModel.logout().also {
                    findNavController().navigate(R.id.action_profileFragment_to_homeActivity)
                }
            }

            textViewProfileFragmentEdit.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }
        }
        displayUserInfo()
    }

    private fun displayUserInfo() {
        binding.apply {
            textViewProfileFragmentFollowers.text = user.userFollowers.toString()
            textViewProfileFragmentFollowing.text = user.userFollowing.toString()
            textViewProfileFragmentDescription.text = user.userDescription
            Glide.with(requireContext()).load(user.userProfileImage)
                .into(imageViewProfileFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}