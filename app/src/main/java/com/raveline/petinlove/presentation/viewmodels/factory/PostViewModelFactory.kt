package com.raveline.petinlove.presentation.viewmodels.factory


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.domain.repository_impl.PostRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import javax.inject.Inject

class PostViewModelFactory @Inject constructor(
    private val postRepository: PostRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(
                postRepository,
                userRepository,
                firestore,
                firebaseAuth,
                application
            ) as T
        }
        throw IllegalArgumentException("Wrong class")
    }
}