package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.domain.repository_impl.LikesRepositoryImpl
import com.raveline.petinlove.domain.utils.SystemFunctions
import com.raveline.petinlove.domain.utils.likeFieldIsLiked
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikeViewModel @Inject constructor(
    private val likesRepository: LikesRepositoryImpl,
    private val sharedPref: SharedPreferences,
    private val application: Application
) : ViewModel() {

    private val _isLikedFlow = MutableStateFlow(false)
    private val likedFlow: StateFlow<Boolean> get() = _isLikedFlow

    private fun setLike(postModel: PostModel, like: Int) = viewModelScope.launch {
        val user = SystemFunctions.getLoggedUserFromPref(sharedPref)!!
        likesRepository.setLikedPost(postModel, user)
        likesRepository.updateLikeNumbers(postModel.postId, user.uid, like)
    }

    private fun removeLike(postModel: PostModel, like: Int) = viewModelScope.launch {
        val user = SystemFunctions.getLoggedUserFromPref(sharedPref)!!
        likesRepository.removeLikePost(postModel.postId, user.uid)
        likesRepository.updateLikeNumbers(postModel.postId, user.uid, like)
    }


    fun getNumberLikes(postModel: PostModel) = viewModelScope.launch {
        val user = SystemFunctions.getLoggedUserFromPref(sharedPref)!!
        likesRepository.getPostLikes(postModel.postId, user.uid).addOnSuccessListener { docs ->

            if (docs.data?.get(likeFieldIsLiked) != null) {
                val numLikes: Int = docs.data?.get(likeFieldIsLiked).toString().toInt()
                _isLikedFlow.value = docs.contains(user.uid)
                if (likedFlow.value) {
                    removeLike(postModel, numLikes - 1)
                } else {
                    setLike(postModel, numLikes + 1)
                }
            } else {
                setLike(postModel, 1)
            }

        }
    }

    suspend fun checkIsLiked(postModel: PostModel): DocumentReference {
        val user = SystemFunctions.getLoggedUserFromPref(sharedPref)!!
        return likesRepository.verifyIfHasLiked(postModel.postId, user.uid)
    }

}