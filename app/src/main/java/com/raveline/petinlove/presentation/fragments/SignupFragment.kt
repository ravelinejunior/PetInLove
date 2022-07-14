package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.databinding.FragmentSignupBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.SystemFunctions.hideKeyboard
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SignupFragment : Fragment() {
    private var _signupBinding: FragmentSignupBinding? = null
    private val signupBinding get() = _signupBinding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels {
        userViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _signupBinding = FragmentSignupBinding.inflate(inflater, container, false)
        initObservers()
        return signupBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signupBinding.apply {
            toolbarSignupFragment.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            textViewGotoLoginFragmentSignup.setOnClickListener {
                findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
            }

            buttonSignupFragment.setOnClickListener {
                getFieldsAndRegister()
            }
        }
    }

    private fun initObservers() {
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
                            signupBinding.root,
                            getString(R.string.something_wrong_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    UiState.Success -> {
                        CustomDialogLoading().dismissLoading()
                        findNavController().navigate(R.id.action_signupFragment_to_mainFragment)
                    }

                }
            }
        }
    }

    private fun getFieldsAndRegister() {
        hideKeyboard()
        signupBinding.apply {
            val email = textInputSignupFragmentEmail.text?.toString()?.trim()
            val name = textInputSignupFragmentName.text?.toString()?.trim()
            val phone = textInputSignupFragmentPhoneNumber.text?.toString()?.trim()
            val password = textInputSignupFragmentPassword.text?.toString()?.trim()

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(context, getString(R.string.name_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, getString(R.string.email_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(context, getString(R.string.phone_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(context, getString(R.string.password_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            userViewModel.registerUser(
                email = email.toString(),
                password = password.toString(),
                name = name.toString(),
                phone = phone.toString()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _signupBinding = null
    }
}