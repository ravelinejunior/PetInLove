package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.R
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.databinding.FragmentSearchPersonBinding
import com.raveline.petinlove.domain.utils.CustomDialogLoading
import com.raveline.petinlove.presentation.adapters.UserItemAdapter
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import com.raveline.petinlove.presentation.viewmodels.factory.UserViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SearchPersonFragment : Fragment() {

    private var _binding: FragmentSearchPersonBinding? = null
    private val binding: FragmentSearchPersonBinding get() = _binding!!

    @Inject
    lateinit var userViewModelFactory: UserViewModelFactory
    private val userViewModel: UserViewModel by viewModels { userViewModelFactory }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val userAdapter: UserItemAdapter by lazy {
        UserItemAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        _binding = FragmentSearchPersonBinding.inflate(inflater, container, false)
        userViewModel.getSearchedUsers()
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchBar()
        initObservers()
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
                    }

                }
            }
        }

        lifecycleScope.launchWhenStarted {
            userViewModel.userListModel.observe(viewLifecycleOwner) { users ->
                if (users.isNotEmpty()) {
                    userAdapter.setData(users)
                    setupRecyclerView()
                }
            }
        }
    }

    private fun setSearchBar() {
        binding.searchViewSearchPersonFragment.addTextChangedListener { text ->
            if(text != null && text.length > 3){
                userViewModel.getSearchedUsers(text.toString())
            }else{
                userViewModel.getSearchedUsers()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSearchPersonFragment.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}