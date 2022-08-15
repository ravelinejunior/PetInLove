package com.raveline.petinlove.presentation.viewmodels.factory


import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.raveline.petinlove.domain.repository_impl.LikesRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.SavedPostsRepositoryImpl
import com.raveline.petinlove.presentation.viewmodels.LikeViewModel
import javax.inject.Inject

class LikesViewModelFactory @Inject constructor(
    private val likesRepository: LikesRepositoryImpl,
    private val savedPostsRepository: SavedPostsRepositoryImpl,
    private val sharedPreferences: SharedPreferences,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LikeViewModel::class.java)) {
            return LikeViewModel(
                likesRepository,
                savedPostsRepository,
                sharedPreferences,
                application
            ) as T
        }
        throw IllegalArgumentException("Wrong class")
    }
}