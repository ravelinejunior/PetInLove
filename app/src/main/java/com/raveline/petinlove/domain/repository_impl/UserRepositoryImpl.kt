package com.raveline.petinlove.domain.repository_impl

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.raveline.petinlove.data.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): UserRepository {
    override suspend fun saveUserOnServer(email: String, password: String): Task<AuthResult> {
       return firebaseAuth.createUserWithEmailAndPassword(email, password)
    }

    override suspend fun loginUserFromServer(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email,password)
    }
}