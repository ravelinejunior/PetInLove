package com.raveline.petinlove.presentation.viewmodels.factory


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.domain.repository_impl.PostRepositoryImpl
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import javax.inject.Inject

class PostViewModelFactory @Inject constructor(
    private val postRepository: PostRepositoryImpl,
    private val firebaseAuth: FirebaseAuth,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(
                postRepository,
                firebaseAuth,
                application
            ) as T
        }
        throw IllegalArgumentException("Wrong class")
    }
}