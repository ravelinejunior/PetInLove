package com.raveline.petinlove.domain.utils

// Firebase
const val databaseName = "PetDatabaseName"

//User Database Server
const val userDatabaseReference = "Users"
const val userFieldName = "name"
const val userFieldEmail = "email"
const val userFieldPhoneNumber = "phoneNumber"
const val userFieldProfileImage = "profileImage"
const val userFieldDescription = "description"
const val userFieldFollowers = "followers"
const val userFieldFollowing = "following"
const val userFieldPublications = "publications"
const val userFieldId = "uid"
const val userStorageReferenceImage = "UserImageProfile"
const val firstRegisterUserImage =
    "https://firebasestorage.googleapis.com/v0/b/inpets-f546d.appspot.com/o/UserImageProfile%2Fprofile_empty_start.jpg?alt=media&token=b445ffd3-61fa-4bc5-8324-4b1ca7123b48"

//Post Database Server
const val postFirebaseDatabaseReference = "Posts"
const val postLikesFirebaseDatabaseReference = "PostsLikes"
const val postFieldUid = "uid"
const val postFieldPostId = "postId"
const val postFieldImagePath = "imagePath"
const val postFieldDescription = "description"
const val postFieldLikes = "likes"
const val postFieldComments = "comments"
const val postFieldAuthorId = "authorId"
const val postFieldUserAuthorName = "userAuthorName"
const val postFieldUserAuthorImage = "userAuthorImage"
const val postFieldImagePostedName = "imagePostedName"
const val postFieldDatePosted = "postDate"

//Likes Database Server
const val likeFirebaseDatabaseReference = "Likes"
const val likeFieldIsLiked = "isLiked"

const val mediaStoreKeyProfileImage = 165164
const val postStoreKeyProfileImage = 4455

const val GLOBAL_SHARED_PREFS_KEY = "GlobalSharedKey"
const val USER_SAVED_SHARED_PREF_KEY = "SavedUserSharedKey"

