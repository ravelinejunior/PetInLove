package com.raveline.petinlove.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.domain.utils.*
import java.io.Serializable


data class PostModel(
    val id: Int = 0,
    var authorId: String,
    var userAuthorName: String,
    var userAuthorImage: String,
    var postId: String,
    var description: String,
    var imagePath: String,
    var likes: Int = 0,
    var dateCreated: Timestamp
) : Serializable

fun mapToPost(result: DocumentSnapshot): PostModel {
    return PostModel(
        postId = result[postFieldPostId].toString(),
        authorId = result[postFieldAuthorId].toString(),
        userAuthorName = result[postFieldUserAuthorName].toString(),
        userAuthorImage = result[postFieldUserAuthorImage].toString(),
        imagePath = result[postFieldImagePath].toString(),
        description = result[postFieldDescription].toString(),
        likes = result[postFieldLikes].toString().toInt(),
        dateCreated = result[postFieldDatePosted] as Timestamp,
        id = 0
    )
}
