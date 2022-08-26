package com.raveline.petinlove.data.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.domain.utils.*
import java.io.Serializable

data class StoryModel(
    val isSeen: Boolean,
    val timeStart: Long,
    val timeEnd: Long,
    val imagePath: String,
    val userName: String,
    val userId: String,
    val storyId: String,
    val views: Int,
    val profileImage: String,
) : Serializable

fun mapToStory(result: DocumentSnapshot): StoryModel {
    return StoryModel(
        isSeen = result[isSeenFieldStory].toString().toBoolean(),
        timeEnd = result[timeEndFieldStory].toString().toLong(),
        timeStart = result[timeStartFieldStory].toString().toLong(),
        imagePath = result[imagePathFieldStory].toString(),
        userName = result[userNameFieldStory].toString(),
        storyId = result[storyIdFieldStory].toString(),
        userId = result[userIdFieldStory].toString(),
        views = result[viewsFieldStory].toString().toInt(),
        profileImage = result[userImageFieldStory].toString(),
    )
}

fun mapToStory(result: Map<String,Any>): StoryModel {
    return StoryModel(
        isSeen = result[isSeenFieldStory].toString().toBoolean(),
        timeEnd = result[timeEndFieldStory].toString().toLong(),
        timeStart = result[timeStartFieldStory].toString().toLong(),
        imagePath = result[imagePathFieldStory].toString(),
        userName = result[userNameFieldStory].toString(),
        storyId = result[storyIdFieldStory].toString(),
        userId = result[userIdFieldStory].toString(),
        views = result[viewsFieldStory].toString().toInt(),
        profileImage = result[userImageFieldStory].toString(),
    )
}

