package com.raveline.petinlove.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel

interface LikesRepository {

    suspend fun setLikedPost(
        postModel: PostModel, userModel: UserModel
    )

    suspend fun updateLikeNumbers(postId: String, userId: String, numLike: Int)
    suspend fun removeLikePost(postId: String, userId: String)
    suspend fun getPostLikes(postId: String, userId: String): Task<DocumentSnapshot>
    suspend fun verifyIfHasLiked(postId: String, userId: String): DocumentReference
}