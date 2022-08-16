package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.model.mapToPost
import com.raveline.petinlove.domain.repository_impl.LikesRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.SavedPostsRepositoryImpl
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
    private val savedPostsRepository: SavedPostsRepositoryImpl,
    private val sharedPref: SharedPreferences,
    private val application: Application
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

    private val _isLikedFlow = MutableStateFlow(false)
    private val likedFlow: StateFlow<Boolean> get() = _isLikedFlow

    private val _postsStateFlow = MutableStateFlow<List<PostModel>>(emptyList())
    val postsFlow: StateFlow<List<PostModel>> get() = _postsStateFlow

    private val _isSavedFlow = MutableStateFlow(false)
    private val savedFlow: StateFlow<Boolean> get() = _isSavedFlow

    private fun setLike(postModel: PostModel, like: Int) = viewModelScope.launch {
        val user = SystemFunctions.getLoggedUserFromPref(application)!!
        likesRepository.setLikedPost(postModel, user)
        likesRepository.updateLikeNumbers(postModel.postId, user.uid, like)
    }

    private fun removeLike(postModel: PostModel, like: Int) = viewModelScope.launch {
        val user = SystemFunctions.getLoggedUserFromPref(application)!!
        likesRepository.removeLikePost(postModel.postId, user.uid)
        likesRepository.updateLikeNumbers(postModel.postId, user.uid, like)
    }


    fun getNumberLikes(postModel: PostModel) = viewModelScope.launch {
        val user = SystemFunctions.getLoggedUserFromPref(application)!!
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
        val user = SystemFunctions.getLoggedUserFromPref(application)!!
        return likesRepository.verifyIfHasLiked(postModel.postId, user.uid)
    }

    fun getSavedPosts(userId: String) = viewModelScope.launch {
        if (SystemFunctions.isNetworkAvailable(application)) {
            _uiStateFlow.value = UiState.Loading

            savedPostsRepository.getSavedPosts(userId).addSnapshotListener { docs, error ->
                if (docs != null) {
                    val posts = arrayListOf<PostModel>()
                    for (doc in docs) {
                        posts.add(mapToPost(doc))
                    }
                    _postsStateFlow.value = posts
                    _uiStateFlow.value = UiState.Success
                } else {
                    _uiStateFlow.value = UiState.Error
                }

                if (error != null) {
                    _uiStateFlow.value = UiState.Error
                }
            }

        } else {
            _uiStateFlow.value = UiState.NoConnection
        }
    }

    fun setOrRemoveSavePost(
        post: PostModel,
        userModel: UserModel,
        hashMap: HashMap<String, Any>? = null
    ) {
        viewModelScope.launch {
            if (SystemFunctions.isNetworkAvailable(application)) {
                _uiStateFlow.value = UiState.Loading

                savedPostsRepository.getPostsSavedToSet(userModel.uid).document(post.postId).get()
                    .addOnSuccessListener { doc ->
                        if (doc?.exists() == true) {
                            removeSavedPost(post, userModel.uid)
                        } else {
                            setSave(post.postId, userModel, hashMap!!)
                        }
                    }

            } else {
                _uiStateFlow.value = UiState.NoConnection
            }
        }
    }

    private fun setSave(postId: String, userModel: UserModel, hashMap: HashMap<String, Any>) =
        viewModelScope.launch {
            savedPostsRepository.setPostSave(postId, userModel.uid).set(hashMap)
        }

    private fun removeSavedPost(post: PostModel, userId: String) = viewModelScope.launch {
        savedPostsRepository.deletePostSaved(post.postId, userId).addOnCompleteListener {
            _isLikedFlow.value = false
        }.addOnFailureListener {
            it.printStackTrace()
        }
    }

    suspend fun checkIsSaved(postModel: PostModel, userId: String): DocumentReference {
        return savedPostsRepository.getPostsSavedToSet(userId).document(postModel.postId)
    }


}