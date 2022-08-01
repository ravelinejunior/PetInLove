package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.domain.repository_impl.PostRepositoryImpl
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import com.raveline.petinlove.domain.utils.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*


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

    fun savePostOnServer(
        user: UserModel,
        imageUri: Uri?,
        extension: String,
        description: String
    ) = viewModelScope.launch(Main) {
        try {
            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                _uiStateFlow.value = UiState.Loading
                postRepository.postFirebaseStorageImageToPost(
                    user,
                    imageUri!!,
                    extension,
                    description
                ).addOnSuccessListener { taskSnapshot ->

                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { dImageUri ->
                        if (dImageUri != null) {
                            viewModelScope.launch(IO) {
                                val postId = UUID.randomUUID().toString().replace("-", "").trim()
                                val postMap = hashMapOf(
                                    postFieldPostId to postId,
                                    postFieldUserAuthorName to user.userName,
                                    postFieldUserAuthorImage to user.userProfileImage,
                                    postFieldLikes to 0,
                                    postFieldAuthorId to user.uid,
                                    postFieldDescription to description,
                                    postFieldImagePath to dImageUri.toString(),
                                    postFieldImagePostedName to extension,
                                    postFieldDatePosted to Timestamp(Date(System.currentTimeMillis()))
                                )

                                postRepository.setPostOnFirebaseServer(postId, postMap)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            _uiStateFlow.value = UiState.Success
                                        }
                                    }.addOnFailureListener {
                                        _uiStateFlow.value = UiState.Error
                                    }
                            }
                        }
                    }

                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiStateFlow.value = UiState.Success
                    }
                }.addOnFailureListener {
                    _uiStateFlow.value = UiState.Error
                }
            } else {
                _uiStateFlow.value = UiState.NoConnection
            }
        } catch (e: Exception) {
            _uiStateFlow.value = UiState.Error
        }
    }

     fun getPostsFromServer() {

        try {
            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                getData()
            } else {
                _uiStateFlow.value = UiState.NoConnection
            }
        } catch (e: Exception) {
            _uiStateFlow.value = UiState.Error
        }
    }

    private fun getData() = viewModelScope.launch {
        _uiStateFlow.value = UiState.Loading
        postRepository.getPostsFromServer(firebaseAuth.uid!!).addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                val posts = arrayListOf<PostModel>()
                for (doc in docs) {
                    val post = mapToPost(doc)
                    posts.add(post)
                }

                _postsStateFlow.value = posts
                _uiStateFlow.value = UiState.Success
            } else {
                _uiStateFlow.value = UiState.Error
            }
        }
            .addOnFailureListener {
                _uiStateFlow.value = UiState.Error
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
            dateCreated = result[postFieldDatePosted] as Timestamp,
            id = 0
        )
    }

}