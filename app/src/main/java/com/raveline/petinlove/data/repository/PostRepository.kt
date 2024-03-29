package com.raveline.petinlove.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun getPostsFromServer(uid: String): Task<QuerySnapshot>
    suspend fun postFirebaseStorageImageToPost(
        user: UserModel,
        imageUri: Uri,
        extension: String,
        description: String
    ): UploadTask

    suspend fun setPostOnFirebaseServer(
        postId: String, postMap: Map<String, Any>
    ): Task<Void>

    suspend fun getPostsById():CollectionReference

    // Local Data
    fun getLocalPosts(): Flow<List<PostModel>>
    suspend fun insertLocalPost(post: PostModel)
    suspend fun deleteLocalPost(post: PostModel)
    suspend fun deleteLocalPosts(posts: List<PostModel>)
}