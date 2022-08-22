package com.raveline.petinlove.domain.repository_impl

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.model.userToMap
import com.raveline.petinlove.data.repository.LikesRepository
import com.raveline.petinlove.domain.utils.likeFieldIsLiked
import com.raveline.petinlove.domain.utils.postLikesFirebaseDatabaseReference
import javax.inject.Inject

class LikesRepositoryImpl @Inject constructor(private val fireStore: FirebaseFirestore) :
    LikesRepository {

    override suspend fun setLikedPost(
        postModel: PostModel,
        userModel: UserModel
    ) {
        val result = 0
        val likeDoc = fireStore.collection(postLikesFirebaseDatabaseReference)
            .document(postModel.postId)

        val likedMap = hashMapOf(
            likeFieldIsLiked to result,
            userModel.uid to userToMap(userModel)
        )

        likeDoc.set(likedMap, SetOptions.merge()).addOnCompleteListener { mTask ->
            if (mTask.isSuccessful) {
                Log.i("TAGLiked", "setLikedPost: ${mTask.result}")
            } else {
                Log.e("TAGLiked", "Error: ${mTask.exception.toString()}")
            }

        }

    }

    override suspend fun updateLikeNumbers(postId: String, userId: String, numLike: Int) {
        fireStore.collection(postLikesFirebaseDatabaseReference)
            .document(postId).update(likeFieldIsLiked, numLike)
    }

    override suspend fun removeLikePost(postId: String, userId: String) {
        val update = mapOf(
            userId to FieldValue.delete()
        )

        fireStore.collection(postLikesFirebaseDatabaseReference)
            .document(postId).update(update)
    }

    override suspend fun getPostLikes(postId: String, userId: String): Task<DocumentSnapshot> {
        val likeDoc = fireStore.collection(postLikesFirebaseDatabaseReference)
            .document(postId)

        return likeDoc.get()
    }

    override suspend fun verifyIfHasLiked(postId: String, userId: String): DocumentReference {
        return fireStore.collection(postLikesFirebaseDatabaseReference)
            .document(postId)
    }


}