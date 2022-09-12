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
const val savedPostFirebaseDatabaseReference = "SavedPosts"
const val savedPostDocumentField = "Saved"

//Comments Database Server
const val commentFirebaseDatabaseReference = "Comments"
const val commentFirebaseSubNodeDatabaseReference = "Comment"
const val commentFieldIdPost = "postId"
const val commentFieldUserName = "name"
const val commentFieldCommentId = "idComment"
const val commentFieldUserId = "authorId"
const val commentFieldComment = "comment"
const val commentFieldUserProfileImage = "profileImage"
const val commentFieldDataCreation = "dateCreated"

//Stories Database Server
const val storyFirebaseDatabaseReference = "Stories"
const val storyFirebaseDocumentReference = "StorySeen"
const val isSeenFieldStory = "isSeen"
const val timeStartFieldStory = "timeStart"
const val timeEndFieldStory = "timeEnd"
const val imagePathFieldStory = "imagePath"
const val userNameFieldStory = "userName"
const val userIdFieldStory = "userId"
const val storyIdFieldStory = "storyId"
const val viewsFieldStory = "views"
const val userImageFieldStory = "profileImage"

//Local Database Data
const val appLocalDatabaseName = "APP_DATABASE"
const val postLocalDatabaseTable = "POST_TABLE"
const val userLocalDatabaseTable = "USER_TABLE"
const val commentLocalDatabaseTable = "COMMENT_TABLE"

const val mediaStoreKeyProfileImage = 165164
const val postStoreKeyProfileImage = 4455
const val storyStoreKeyImage = 6652

const val GLOBAL_SHARED_PREFS_KEY = "GlobalSharedKey"
const val USER_SAVED_SHARED_PREF_KEY = "SavedUserSharedKey"

