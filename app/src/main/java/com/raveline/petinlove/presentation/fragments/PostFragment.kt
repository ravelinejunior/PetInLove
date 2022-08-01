package com.raveline.petinlove.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.raveline.petinlove.R
import com.raveline.petinlove.databinding.FragmentPostBinding

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding: FragmentPostBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvPostFragmentGallery.setOnClickListener{
                findNavController().navigate(R.id.action_postFragment_to_addPostFragment)
            }
            tvPostFragmentCamera.setOnClickListener{
                findNavController().navigate(R.id.action_postFragment_to_addPostFragment)
            }
            ivPostFragmentCamera.setOnClickListener{
                findNavController().navigate(R.id.action_postFragment_to_addPostFragment)
            }
            ivPostFragmentGallery.setOnClickListener{
                findNavController().navigate(R.id.action_postFragment_to_addPostFragment)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

}

}