package com.raveline.petinlove.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.data.model.UserModel

interface UserRepository {
    suspend fun saveUserOnServer(email: String, password: String): Task<AuthResult>
    suspend fun loginUserFromServer(email: String, password: String): Task<AuthResult>
    suspend fun getUserDataFromServer(uid: String): Task<DocumentSnapshot>
    suspend fun editUserDataFromServer(
        imageUrl: String,
        userModel: UserModel
    ): Task<DocumentSnapshot>
}