package com.raveline.petinlove.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

interface SavedPostsRepository {
    suspend fun setPostSave(idPost: String, idUser: String): DocumentReference
    suspend fun getSavedPosts(userId: String): CollectionReference
    suspend fun verifyIfHasLiked(postId: String, userId: String): DocumentReference
}