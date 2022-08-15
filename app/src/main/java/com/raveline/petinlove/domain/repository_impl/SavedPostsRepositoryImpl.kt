package com.raveline.petinlove.domain.repository_impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.repository.SavedPostsRepository
import com.raveline.petinlove.domain.utils.savedPostDocumentField
import com.raveline.petinlove.domain.utils.savedPostFirebaseDatabaseReference
import javax.inject.Inject

class SavedPostsRepositoryImpl @Inject constructor(private val fireStore: FirebaseFirestore) :
    SavedPostsRepository {
    override suspend fun setPostSave(idPost: String, idUser: String): DocumentReference {
        return fireStore.collection(savedPostFirebaseDatabaseReference).document(idUser).collection(
            savedPostDocumentField
        ).document(idPost)
    }

    override suspend fun getSavedPosts(userId: String): CollectionReference {
        return fireStore.collection(savedPostFirebaseDatabaseReference).document(userId)
            .collection(savedPostDocumentField)
    }

    override suspend fun verifyIfHasLiked(postId: String, userId: String): DocumentReference {
        return fireStore.collection(savedPostFirebaseDatabaseReference).document(userId).collection(
            savedPostDocumentField
        ).document(postId)
    }
}