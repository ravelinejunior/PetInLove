package com.raveline.petinlove.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.UploadTask

interface StoryRepository {
    suspend fun saveImageToServerStory(
        imageUri: Uri,
        extension: String,
        userId: String
    ): UploadTask

    suspend fun setStoryOnServer(
        userId: String,
        storyId: String,
        storyMap: HashMap<String, Any>
    ): Task<Void>

    suspend fun saveStory(
        userId: String,
        storyId: String,
        storyMap: HashMap<String, Any>
    ): Task<Void>

    suspend fun getStories(): CollectionReference
    suspend fun getActiveStories(): DatabaseReference
    suspend fun updateStories(storyId: String)
    suspend fun getUserById(userId: String): DocumentReference

}