package com.raveline.petinlove.presentation.viewmodels.factory


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.domain.repository_impl.CommentsRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.PostRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import com.raveline.petinlove.domain.utils.commentFirebaseDatabaseReference
import com.raveline.petinlove.presentation.viewmodels.CommentViewModel
import com.raveline.petinlove.presentation.viewmodels.PostViewModel
import javax.inject.Inject

class CommentViewModelFactory @Inject constructor(
    private val commentsRepository: CommentsRepositoryImpl,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentViewModel::class.java)) {
            return CommentViewModel(
                commentsRepository,
                application
            ) as T
        }
        throw IllegalArgumentException("Wrong class")
    }
}