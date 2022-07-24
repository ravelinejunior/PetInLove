package com.raveline.petinlove.data.model

data class PostModel(
    val id: Int = 0,
    var userName: String,
    var userImage: String = "",
    var uid: String,
    var description: String,
    var imagePath: String,
    var likes: Int = 0,
)
