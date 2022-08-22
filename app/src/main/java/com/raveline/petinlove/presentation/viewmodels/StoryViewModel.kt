package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import com.raveline.petinlove.domain.repository_impl.StoryRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel


@HiltViewModel
class StoryViewModel(
    private val storyRepository: StoryRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
    private val application: Application
):ViewModel() {
}