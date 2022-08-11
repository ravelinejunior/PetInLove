package com.raveline.petinlove.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

interface CommentsRepository {
    suspend fun getCommentariesById(postId: String): CollectionReference
    suspend fun postCommentary(postId: String, commentId: String): DocumentReference
}