package com.raveline.petinlove.presentation.viewmodels.factory


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.raveline.petinlove.domain.repository_impl.StoryRepositoryImpl
import com.raveline.petinlove.presentation.viewmodels.StoryViewModel
import javax.inject.Inject

class StoryViewModelFactory @Inject constructor(
    private val storyRepository: StoryRepositoryImpl,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            return StoryViewModel(
                storyRepository, application
            ) as T
        }
        throw IllegalArgumentException("Wrong class")
    }
}