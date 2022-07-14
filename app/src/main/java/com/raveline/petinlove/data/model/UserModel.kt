package com.raveline.petinlove.data.model

data class UserModel(
    val id: Int = 0,
    var uid:String,
    var userName: String,
    var userEmail: String,
    var userPhoneNumber: String,
    var userProfileImage: String = ""
)