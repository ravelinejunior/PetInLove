package com.raveline.petinlove.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.domain.utils.*
import java.io.Serializable

data class UserModel(
    val id: Int = 0,
    var uid: String,
    var userName: String,
    var userEmail: String,
    var userPhoneNumber: String,
    var userProfileImage: String = "",
    var userDescription: String = "",
    var userFollowing: Int = 0,
    var userFollowers: Int = 0,
    var userPublications: Int = 0,
) : Serializable

fun hashMapToUserModel(result: DocumentSnapshot): UserModel {

    return UserModel(
        uid = result[userFieldId].toString(),
        userName = result[userFieldName].toString(),
        userEmail = result[userFieldEmail].toString(),
        userPhoneNumber = result[userFieldPhoneNumber].toString(),
        userDescription = result[userFieldDescription].toString(),
        userProfileImage = result[userFieldProfileImage].toString(),
        userFollowers = result[userFieldFollowers].toString().toInt(),
        userFollowing = result[userFieldFollowing].toString().toInt(),
        userPublications = result[userFieldPublications].toString().toInt(),
        id = 0
    )
}

fun userToMap(userModel: UserModel): HashMap<String, String> {
    return hashMapOf(
        userFieldId to userModel.uid,
        userFieldName to userModel.userName,
        userFieldEmail to userModel.userEmail,
        userFieldPhoneNumber to userModel.userPhoneNumber,
        userFieldProfileImage to userModel.userProfileImage,
        userFieldDescription to userModel.userDescription,
        userFieldFollowing to userModel.userFollowing.toString(),
        userFieldFollowers to userModel.userFollowers.toString(),
        userFieldPublications to userModel.userPublications.toString(),
    )
}