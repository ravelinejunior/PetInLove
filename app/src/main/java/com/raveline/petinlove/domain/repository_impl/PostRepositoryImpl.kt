package com.raveline.petinlove.domain.repository_impl

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.raveline.petinlove.data.repository.PostRepository
import com.raveline.petinlove.domain.utils.postFirebaseDatabaseReference
import com.raveline.petinlove.domain.utils.userDatabaseReference
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(private val fireStore: FirebaseFirestore) :
    PostRepository {
    override suspend fun getPostsFromServer(uid: String): Task<QuerySnapshot> {
        return fireStore.collection(postFirebaseDatabaseReference).get()
    }

}