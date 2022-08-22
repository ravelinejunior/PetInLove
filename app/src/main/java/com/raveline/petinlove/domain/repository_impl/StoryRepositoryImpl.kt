package com.raveline.petinlove.domain.repository_impl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.raveline.petinlove.data.repository.StoryRepository
import com.raveline.petinlove.domain.utils.storyFirebaseDatabaseReference
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : StoryRepository {
    override suspend fun saveImageToServerStory(
        imageUri: Uri,
        extension: String,
        userId: String
    ): UploadTask {
        val filePath =
            storage.getReference(storyFirebaseDatabaseReference).child(userId).child(extension)

        return filePath.putFile(imageUri)
    }

    override suspend fun setStoryOnServer(storyId: String, storyMap: Map<String, Any>): Task<Void> {
        return firestore.collection(storyFirebaseDatabaseReference)
            .document(storyId).set(storyMap)
    }
}