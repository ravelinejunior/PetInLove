package com.raveline.petinlove.domain.repository_impl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.repository.PostRepository
import com.raveline.petinlove.domain.utils.postFieldDatePosted
import com.raveline.petinlove.domain.utils.postFirebaseDatabaseReference
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage
) :
    PostRepository {
    override suspend fun getPostsFromServer(uid: String): Task<QuerySnapshot> {
        return fireStore.collection(postFirebaseDatabaseReference).orderBy(
            postFieldDatePosted,
            Query.Direction.DESCENDING
        ).get()
    }

    override suspend fun postFirebaseStorageImageToPost(
        user: UserModel,
        imageUri: Uri,
        extension: String,
        description: String
    ): UploadTask {
        val filePath =
            storage.getReference(postFirebaseDatabaseReference).child(user.uid).child(extension)

        return filePath.putFile(imageUri)

    }

    override suspend fun setPostOnFirebaseServer(
        postId: String,
        postMap: Map<String, Any>
    ): Task<Void> {
        return fireStore.collection(postFirebaseDatabaseReference)
            .document(postId).set(postMap)
    }

    override suspend fun getPostsById(): CollectionReference {
        return fireStore.collection(postFirebaseDatabaseReference)
    }

}