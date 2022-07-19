package com.raveline.petinlove.presentation.viewmodels.factory


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import com.raveline.petinlove.presentation.viewmodels.UserViewModel
import javax.inject.Inject

class UserViewModelFactory @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository, firestore, firebaseAuth, application) as T
        }
        throw IllegalArgumentException("Wrong class")
    }
}