package com.raveline.petinlove.domain.repository_impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.repository.CommentsRepository
import com.raveline.petinlove.domain.utils.commentFirebaseDatabaseReference
import com.raveline.petinlove.domain.utils.commentFirebaseSubNodeDatabaseReference
import javax.inject.Inject

class CommentsRepositoryImpl @Inject constructor(private val fireStore: FirebaseFirestore) :
    CommentsRepository {
    override suspend fun getCommentariesById(postId: String): CollectionReference {
        return fireStore.collection(commentFirebaseDatabaseReference).document(postId).collection(
            commentFirebaseSubNodeDatabaseReference
        )
    }

    override suspend fun postCommentary(postId: String, commentId: String): DocumentReference {
        return fireStore.collection(commentFirebaseDatabaseReference).document(postId)
            .collection(commentFirebaseSubNodeDatabaseReference).document(commentId)
    }
}