package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.raveline.petinlove.R
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.databinding.FragmentEditProfileBinding
import com.raveline.petinlove.domain.utils.SystemFunctions.hideKeyboard
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.getUserData()


        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
    }

    private fun setObservers() {
        lifecycleScope.launch {
            userViewModel.userModel.collectLatest { user ->
                if (user != null) {
                    displayData(user)
                }
            }
        }
    }

    private fun displayData(user: UserModel) {
        binding.apply {
            textInputEditProfileName.setText(user.userName)
            textInputEditProfilePhoneNumber.setText(user.userPhoneNumber)
            textInputEditProfileFragmentDescription.setText(user.userDescription)
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
                Toast.makeText(context, getString(R.string.email_empty_msg), Toast.LENGTH_SHORT)
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

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}