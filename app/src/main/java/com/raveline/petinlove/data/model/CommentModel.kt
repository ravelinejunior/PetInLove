package com.raveline.petinlove.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.domain.utils.*
import java.io.Serializable

data class CommentModel(
    val id: Int = 0,
    val idUser: String,
    val idComment: String,
    val idPost: String,
    val userName: String,
    val userProfileImage: String,
    val comment: String,
    val dateCreation: Timestamp
) : Serializable

fun mapToComment(result: DocumentSnapshot): CommentModel {
    return CommentModel(
        idUser = result[commentFieldUserId].toString(),
        idPost = result[commentFieldIdPost].toString(),
        idComment = result[commentFieldCommentId].toString(),
        userName = result[commentFieldUserName].toString(),
        comment = result[commentFieldComment].toString(),
        userProfileImage = result[commentFieldUserProfileImage].toString(),
        dateCreation = result[commentFieldDataCreation] as Timestamp,
        id = 0
    )
}
