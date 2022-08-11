package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.CommentModel
import com.raveline.petinlove.data.model.mapToComment
import com.raveline.petinlove.domain.repository_impl.CommentsRepositoryImpl
import com.raveline.petinlove.domain.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommentViewModel @Inject constructor(
    private val commentsRepository: CommentsRepositoryImpl,
    private val application: Application
) : ViewModel() {
    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

    private val _commentStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val commentStateFlow: StateFlow<UiState> get() = _commentStateFlow

    private val _commentariesMutStateFlow = MutableStateFlow<List<CommentModel>>(emptyList())
    val commentariesFlow: StateFlow<List<CommentModel>> get() = _commentariesMutStateFlow

    fun getCommentsByPost(postId: String) = viewModelScope.launch {
        if (SystemFunctions.isNetworkAvailable(application)) {
            _uiStateFlow.value = UiState.Loading
            commentsRepository.getCommentariesById(postId)
                .orderBy(commentFieldDataCreation, Query.Direction.DESCENDING)
                .addSnapshotListener { docs, error ->
                    if (docs != null) {
                        val comments = arrayListOf<CommentModel>()
                        for (doc in docs) {
                            comments.add(mapToComment(doc))
                        }

                        _commentariesMutStateFlow.value = comments
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

    fun postComment(postId: String, commentId: String, commentModel: CommentModel) =
        viewModelScope.launch {
            if (SystemFunctions.isNetworkAvailable(application)) {
                _commentStateFlow.value = UiState.Loading
                val commentMap = hashMapOf(
                    commentFieldIdPost to commentModel.idPost,
                    commentFieldUserName to commentModel.userName,
                    commentFieldUserId to commentModel.idUser,
                    commentFieldUserProfileImage to commentModel.userProfileImage,
                    commentFieldCommentId to commentId,
                    commentFieldComment to commentModel.comment,
                    commentFieldDataCreation to commentModel.dateCreation
                )
                commentsRepository.postCommentary(postId, commentId)
                    .set(commentMap, SetOptions.merge())
                    .addOnCompleteListener { mTask ->
                        if (mTask.isSuccessful) {
                            _commentStateFlow.value = UiState.Success
                        } else {
                            _commentStateFlow.value = UiState.Error
                        }
                    }.addOnFailureListener {
                        _commentStateFlow.value = UiState.Error
                        it.printStackTrace()
                    }

            } else {
                _commentStateFlow.value = UiState.NoConnection
            }
        }
}


