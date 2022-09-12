package com.raveline.petinlove.domain.repository_impl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.raveline.petinlove.data.database.dao.PostDao
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.repository.PostRepository
import com.raveline.petinlove.domain.utils.postFieldDatePosted
import com.raveline.petinlove.domain.utils.postFirebaseDatabaseReference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val postDao: PostDao
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

    override fun getLocalPosts(): Flow<List<PostModel>> {
        return postDao.getLocalPosts()
    }

    override suspend fun insertLocalPost(post: PostModel) {
        postDao.insertLocalPost(post)
    }

    override suspend fun deleteLocalPost(post: PostModel) {
        postDao.deleteLocalPost(post)
    }

    override suspend fun deleteLocalPosts(posts: List<PostModel>) {
        postDao.deleteLocalPosts(posts)
    }

}