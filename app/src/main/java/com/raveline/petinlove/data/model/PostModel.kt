package com.raveline.petinlove.data.model

import com.google.firebase.Timestamp


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
)
