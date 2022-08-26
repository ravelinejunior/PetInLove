package com.raveline.petinlove.domain.repository_impl

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.raveline.petinlove.data.repository.StoryRepository
import com.raveline.petinlove.domain.utils.storyFirebaseDatabaseReference
import com.raveline.petinlove.domain.utils.storyFirebaseDocumentReference
import com.raveline.petinlove.domain.utils.userDatabaseReference
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
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

    override suspend fun setStoryOnServer(userId: String,storyId: String, storyMap: HashMap<String, Any>): Task<Void> {

        return firestore.collection(storyFirebaseDatabaseReference)
            .document(userId).collection(storyFirebaseDocumentReference).document(storyId).set(storyMap)
    }

    override suspend fun saveStory(
        userId: String,
        storyId: String,
        storyMap: HashMap<String, Any>
    ): Task<Void> {
        return firebaseDatabase.getReference(storyFirebaseDatabaseReference).child(userId).child(storyId).setValue(storyMap)
    }

    override suspend fun getStories(): CollectionReference {
        return firestore.collection(storyFirebaseDatabaseReference)
    }

    override suspend fun getActiveStories(): DatabaseReference {
        return firebaseDatabase.getReference(storyFirebaseDatabaseReference)
    }

    override suspend fun getStoriesIds(): DatabaseReference {
        return firebaseDatabase.getReference(storyFirebaseDatabaseReference)
    }

    override suspend fun updateStories(storyId: String) {
        val removeStoryMap = mapOf(
            storyId to FieldValue.delete()
        )

        firestore.collection(storyFirebaseDatabaseReference).document(storyId)
            .update(removeStoryMap)
    }

    override suspend fun getUserById(userId: String): DocumentReference {
        return firestore.collection(userDatabaseReference).document(userId)
    }
}