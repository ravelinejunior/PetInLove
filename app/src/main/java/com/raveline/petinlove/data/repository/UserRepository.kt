package com.raveline.petinlove.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

 interface UserRepository{
    suspend fun saveUserOnServer(email: String, password: String): Task<AuthResult>
    suspend fun loginUserFromServer(email: String, password: String): Task<AuthResult>
}