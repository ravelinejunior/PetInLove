package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.domain.repository_impl.PostRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import com.raveline.petinlove.domain.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel(
    private val postRepository: PostRepositoryImpl,
    private val userRepository: UserRepositoryImpl,
    private val fireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val application: Application,
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

    private val _postsStateFlow = MutableStateFlow<List<PostModel>>(emptyList())
    val postsFlow: StateFlow<List<PostModel>> get() = _postsStateFlow

    private var mUser: UserModel? = null

    init {
        viewModelScope.launch {
            getUserData()
        }

    }

    private fun getPostsFromServer() = viewModelScope.launch {

        try {
            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                _uiStateFlow.value = UiState.Loading
                getData()
            } else {
                _uiStateFlow.value = UiState.NoConnection
            }
        } catch (e: Exception) {
            _uiStateFlow.value = UiState.Error
        }
    }

    private fun getData() = viewModelScope.launch {
        postRepository.getPostsFromServer(firebaseAuth.uid!!).addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                val posts = arrayListOf<PostModel>()
                for (doc in docs) {
                    val post = mapToPost(doc)
                    posts.add(post)
                }

                _postsStateFlow.value = posts
                _uiStateFlow.value = UiState.Success
                Log.i("TAGPostFlow", "$posts")
            } else {
                _uiStateFlow.value = UiState.Error
            }
        }
            .addOnFailureListener {
                _uiStateFlow.value = UiState.Error
            }
    }

    private fun getUserData() {
        _uiStateFlow.value = UiState.Loading
        viewModelScope.launch {
            userRepository.getUserDataFromServer(firebaseAuth.uid.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        mUser = mapToUser(result)
                        viewModelScope.launch {
                            getPostsFromServer()
                        }
                    }
                }
        }
    }

    private fun mapToPost(result: DocumentSnapshot): PostModel {
        return PostModel(
            postId = result[postFieldUid].toString(),
            authorId = result[postFieldAuthorId].toString(),
            userAuthorName = result[postFieldUserAuthorName].toString(),
            userAuthorImage = result[postFieldUserAuthorImage].toString(),
            imagePath = result[postFieldImagePath].toString(),
            description = result[postFieldDescription].toString(),
            likes = result[postFieldLikes].toString().toInt(),
            id = 0
        )
    }

    private fun mapToUser(result: DocumentSnapshot): UserModel {
        return UserModel(
            uid = result[userFieldId].toString(),
            userName = result[userFieldName].toString(),
            userEmail = result[userFieldEmail].toString(),
            userPhoneNumber = result[userFieldPhoneNumber].toString(),
            userDescription = result[userFieldDescription].toString(),
            userProfileImage = result[userFieldProfileImage].toString(),
            id = 0
        )
    }
}