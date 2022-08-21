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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.databinding.FragmentLoginBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.domain.utils.SystemFunctions.hideKeyboard
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (firebaseAuth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }

        navBar = requireActivity().findViewById(R.id.bnv_main_id)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        navBar.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        initObservers()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            textViewLoginFragmentSignup.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
            }
            toolbarLoginFragment.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            buttonLoginFragment.setOnClickListener {
                getFieldsAndLogin()
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
                            binding.root,
                            getString(R.string.something_wrong_msg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    UiState.Success -> {
                        CustomDialogLoading().dismissLoading()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }

                }
            }
        }
    }

    private fun getFieldsAndLogin() {
        hideKeyboard()
        binding.apply {
            val email = textInputLoginFragmentEmail.text?.toString()?.trim()
            val password = textInputLoginFragmentPassword.text?.toString()?.trim()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, getString(R.string.email_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(context, getString(R.string.password_empty_msg), Toast.LENGTH_SHORT)
                    .show()

                return
            }

            userViewModel.loginUser(
                email = email.toString(),
                password = password.toString()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}