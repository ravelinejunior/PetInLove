package com.raveline.petinlove.domain.repository_impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.database.dao.CommentDao
import com.raveline.petinlove.data.model.CommentModel
import com.raveline.petinlove.data.repository.CommentsRepository
import com.raveline.petinlove.domain.utils.commentFirebaseDatabaseReference
import com.raveline.petinlove.domain.utils.commentFirebaseSubNodeDatabaseReference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CommentsRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val commentDao: CommentDao
) :
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

    override fun getLocalCommentsById(postId: String): Flow<List<CommentModel>> {
        return commentDao.getLocalCommentsById(postId)
    }

    override suspend fun insertLocalComment(comment: CommentModel) {
        commentDao.insertLocalComment(comment)
    }

    override suspend fun deleteLocalComment(comment: CommentModel) {
        commentDao.deleteLocalComment(comment)
    }

    override suspend fun deleteLocalComments(comments: List<CommentModel>) {
        commentDao.deleteLocalComments(comments)
    }
}