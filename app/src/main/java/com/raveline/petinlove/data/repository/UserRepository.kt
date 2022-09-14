package com.raveline.petinlove.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.raveline.petinlove.data.model.UserModel
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUserOnServer(email: String, password: String): Task<AuthResult>
    suspend fun loginUserFromServer(email: String, password: String): Task<AuthResult>
    suspend fun getUserDataFromServer(uid: String): Task<DocumentSnapshot>
    suspend fun editUserDataFromServer(
        userModel: UserModel,
        map: Map<String, Any>
    ): Task<Void>

    suspend fun getSearchUsers(): CollectionReference

    // Local Data

    suspend fun saveLocalCurrentUser(user: UserModel)
    suspend fun deleteLocalCurrentUser(id: String)
    suspend fun deleteAllLocalUsers()
    suspend fun updateLocalUser(user: UserModel)

    fun getLocalUsers(): Flow<List<UserModel>>
    fun getCurrentLocalUser(id: String): Flow<UserModel>
    fun getSearchLocalUsers(search: String): Flow<List<UserModel>>
}