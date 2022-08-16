package com.raveline.petinlove.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

interface SavedPostsRepository {
    suspend fun setPostSave(idPost: String, idUser: String): DocumentReference
    suspend fun deletePostSaved(idPost: String, idUser: String): Task<Void>
    suspend fun getSavedPosts(userId: String): CollectionReference
    suspend fun getPostsSavedToSet(userId: String): CollectionReference
    suspend fun verifyIfHasLiked(postId: String, userId: String): DocumentReference
}