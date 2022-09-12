package com.raveline.petinlove.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.raveline.petinlove.data.model.CommentModel
import kotlinx.coroutines.flow.Flow

interface CommentsRepository {
    suspend fun getCommentariesById(postId: String): CollectionReference
    suspend fun postCommentary(postId: String, commentId: String): DocumentReference

    // Local data
    fun getLocalCommentsById(postId: String): Flow<List<CommentModel>>
    suspend fun insertLocalComment(comment: CommentModel)
    suspend fun deleteLocalComment(comment: CommentModel)
    suspend fun deleteLocalComments(comments: List<CommentModel>)

}