package com.raveline.petinlove.domain.repository_impl

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.raveline.petinlove.data.database.dao.UserDao
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.data.repository.UserRepository
import com.raveline.petinlove.domain.utils.userDatabaseReference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val userDao: UserDao
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
        map: Map<String, Any>
    ): Task<Void> {
        return fireStore.collection(userDatabaseReference).document(userModel.uid).update(map)
    }

    override suspend fun getSearchUsers(): CollectionReference {
        return fireStore.collection(userDatabaseReference)
    }

    override suspend fun saveLocalCurrentUser(user: UserModel) {
        userDao.insertUser(user)
    }

    override suspend fun deleteLocalCurrentUser(id: String) {
        userDao.deleteCurrentLocalUser(id)
    }

    override suspend fun deleteAllLocalUsers() {
        userDao.deleteUsers()
    }

    override suspend fun updateLocalUser(user: UserModel) {
        userDao.updateUser(user)
    }

    override fun getLocalUsers(): Flow<List<UserModel>> {
        return userDao.getLocalUsers()
    }

    override fun getCurrentLocalUser(id: String): Flow<UserModel> {
        return userDao.getCurrentLocalUser(id)
    }

    override fun getSearchLocalUsers(search: String): Flow<List<UserModel>> {
        return userDao.getSearchUsers(search)
    }
}








