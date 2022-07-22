package com.raveline.petinlove.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

interface PostRepository {
    suspend fun getPostsFromServer(uid: String): Task<QuerySnapshot>
}