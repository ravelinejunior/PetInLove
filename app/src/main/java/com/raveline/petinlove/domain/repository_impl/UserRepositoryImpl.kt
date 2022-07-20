package com.raveline.petinlove.domain.repository_impl

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.repository.UserRepository
import com.raveline.petinlove.domain.utils.userDatabaseReference
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
) : UserRepository {
    override suspend fun saveUserOnServer(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
    }

    override suspend fun loginUserFromServer(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun getUserDataFromServer(uid: String): Task<DocumentSnapshot> {
        return fireStore.collection(userDatabaseReference).document(uid).get()
    }

    override suspend fun editUserDataFromServer(
        userModel: UserModel,
        map:Map<String,Any>
    ): Task<Void> {
        return fireStore.collection(userDatabaseReference).document(userModel.uid).update(map)
    }
}