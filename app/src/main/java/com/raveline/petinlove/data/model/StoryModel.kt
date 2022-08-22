package com.raveline.petinlove.data.model

import java.io.Serializable

data class StoryModel(
    val isSeen: Boolean,
    val timeStart: Long,
    val timeEnd: Long,
    val imagePath: String,
    val userName: String,
    val userId: String,
    val storyId: String,
    val views: Int
) : Serializable
