package com.raveline.petinlove.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import com.raveline.petinlove.data.model.UserModel

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

}