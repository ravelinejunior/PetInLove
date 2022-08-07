package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.domain.repository_impl.UserRepositoryImpl
import com.raveline.petinlove.domain.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepositoryImpl,
    private val fireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val sharedPref: SharedPreferences,
    private val application: Application,
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

    private val _uiUserProfileStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiUserProfileStateFlow: StateFlow<UiState> get() = _uiUserProfileStateFlow

    private val _userFlow = MutableStateFlow<UserModel?>(null)
    val userModel: StateFlow<UserModel?> get() = _userFlow

    private val _userProfileFlow = MutableStateFlow<UserModel?>(null)
    val userProfileFlow: StateFlow<UserModel?> get() = _userProfileFlow

    private val _userListFlow = MutableLiveData<List<UserModel>>()
    val userListModel: LiveData<List<UserModel>> get() = _userListFlow

    var imagePath = ""
    var profileImage = ""
    var result = ""

    private fun getUserData() {

        viewModelScope.launch {

            if (getLoggedUserFromPref() != null) return@launch
            else {
                userRepository.getUserDataFromServer(firebaseAuth.uid.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val result = task.result
                            saveUserOnPrefs(mapToUser(result))
                            getLoggedUserFromPref()
                            profileImage = result[userFieldProfileImage].toString()
                        }
                    }
            }


        }
    }

     fun getUserById(userId: String) = viewModelScope.launch {
         if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
             _uiUserProfileStateFlow.value = UiState.Loading
             userRepository.getUserDataFromServer(userId).addOnSuccessListener { doc ->
                 if (doc != null) {
                     val user = mapToUser(doc)
                     _userProfileFlow.value = user
                     _uiUserProfileStateFlow.value = UiState.Success
                 }else{
                     _uiUserProfileStateFlow.value = UiState.Error
                 }
             }
         } else {
             _uiUserProfileStateFlow.value = UiState.NoConnection
         }

     }


    fun getSearchedUsers(name: String = "") = viewModelScope.launch {
        if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
            val users = arrayListOf<UserModel>()
            if (name.isNotEmpty()) {
                userRepository.getSearchUsers().orderBy(userFieldName, Query.Direction.ASCENDING)
                    .addSnapshotListener { value, error ->
                        if (value != null) {
                            _userListFlow.value = emptyList()
                            for (doc in value.documents) {

                                if (doc[userFieldName].toString()
                                        .contains(name, ignoreCase = true)
                                ) {
                                    users.add(mapToUser(doc))
                                }

                            }

                            _userListFlow.value = users.sortedBy {
                                it.userName
                            }
                        } else {
                            result = error.toString()
                            _uiStateFlow.value = UiState.Error
                        }
                    }
            } else {
                userRepository.getSearchUsers().addSnapshotListener { value, error ->
                    if (value != null) {
                        _userListFlow.value = emptyList()
                        for (doc in value.documents) {
                            users.add(mapToUser(doc))
                        }

                        _userListFlow.value = users
                    } else {
                        result = error.toString()
                        _uiStateFlow.value = UiState.Error
                    }
                }
            }
        } else {
            _uiStateFlow.value = UiState.NoConnection
            result = "No Internet Connection!"
        }
    }

    fun editUserData(name: String?, phone: String?, description: String?) =
        viewModelScope.launch {

            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                _uiStateFlow.value = UiState.Loading
                if (userModel.value != null && imagePath.isNotEmpty()) {
                    storeImage(name, phone, description)
                } else {
                    updateUserWithoutImage(name, phone, description)
                }
            } else {
                _uiStateFlow.value = UiState.NoConnection
                result = "No Internet Connection!"
            }
        }

    private fun updateUserWithoutImage(name: String?, phone: String?, description: String?) =
        viewModelScope.launch {
            viewModelScope.launch {
                updateUserFlow(name, phone, description)
                val user = userModel.value!!
                val map = mapOf(
                    userFieldPhoneNumber to user.userPhoneNumber,
                    userFieldName to user.userName,
                    userFieldDescription to user.userDescription
                )

                userRepository.editUserDataFromServer(user, map)
                    .addOnCompleteListener { inTask ->
                        if (inTask.isSuccessful) {
                            result = "Successfully Edited!"
                            sharedPref.edit(true) {
                                clear()
                            }
                            saveUserOnPrefs(user)
                            _uiStateFlow.value = UiState.Success
                        } else {
                            result = inTask.result.toString()
                            _uiStateFlow.value = UiState.Error
                        }
                    }
            }
        }

    private fun updateUserFlow(name: String?, phone: String?, description: String?) {
        description?.let {
            _userFlow.value?.userDescription = it
        }

        phone?.let {
            _userFlow.value?.userPhoneNumber = it
        }

        name?.let {
            _userFlow.value?.userName = it
        }

        if (profileImage.isNotEmpty()) {
            _userFlow.value?.userProfileImage = profileImage
        }

    }

    private fun storeImage(name: String?, phone: String?, description: String?) =
        viewModelScope.launch {
            _uiStateFlow.value = UiState.Loading

            storage.reference.child(userStorageReferenceImage).child(firebaseAuth.uid!!)
                .child(userModel.value?.userName?.trim().toString() + "_ProfileImage")
                .putFile(Uri.parse(imagePath))
                .addOnSuccessListener { task ->
                    task.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        if (uri != null) {
                            profileImage = uri.toString()

                            viewModelScope.launch {
                                updateUserFlow(name, phone, description)
                                val user = userModel.value!!
                                val map = mapOf(
                                    userFieldProfileImage to profileImage,
                                    userFieldPhoneNumber to user.userPhoneNumber,
                                    userFieldName to user.userName,
                                    userFieldDescription to user.userDescription
                                )

                                viewModelScope.launch {
                                    userRepository.editUserDataFromServer(user, map)
                                        .addOnCompleteListener { inTask ->
                                            if (inTask.isSuccessful) {
                                                result = "Successfully Edited!"
                                                sharedPref.edit(true) {
                                                    clear()
                                                }
                                                saveUserOnPrefs(user)
                                                _uiStateFlow.value = UiState.Success
                                            } else {
                                                result = inTask.result.toString()
                                                _uiStateFlow.value = UiState.Error
                                            }
                                        }
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    _uiStateFlow.value = UiState.Error
                }

        }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        try {
            _uiStateFlow.value = UiState.Loading
            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                userRepository.loginUserFromServer(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val result = task.result
                            viewModelScope.launch {
                                userRepository.getUserDataFromServer(result.user!!.uid)
                                    .addOnSuccessListener { doc ->
                                        saveUserOnPrefs(mapToUser(doc))
                                        getUserData()
                                        _uiStateFlow.value = UiState.Success
                                    }
                            }


                        } else {
                            _uiStateFlow.value = UiState.Error
                        }
                    }
            } else {
                _uiStateFlow.value = UiState.NoConnection
            }
        } catch (e: Exception) {
            _uiStateFlow.value = UiState.Error
        }
    }

    fun registerUser(email: String, password: String, name: String, phone: String) =
        viewModelScope.launch {
            try {
                _uiStateFlow.value = UiState.Loading
                if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                    userRepository.saveUserOnServer(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = UserModel(
                                userEmail = email,
                                userName = name,
                                userPhoneNumber = phone,
                                uid = task.result.user?.uid.toString(),
                                userProfileImage = firstRegisterUserImage,
                            )

                            saveUserOnPrefs(user)

                            fireStore.collection(userDatabaseReference).document(user.uid)
                                .set(userToMap(user)).addOnCompleteListener { fireTask ->

                                    if (fireTask.isSuccessful) {
                                        _uiStateFlow.value = UiState.Success
                                    } else {
                                        _uiStateFlow.value = UiState.Error
                                    }

                                }

                        } else {
                            _uiStateFlow.value = UiState.Error
                        }
                    }
                } else {
                    _uiStateFlow.value = UiState.NoConnection
                }

            } catch (e: Exception) {
                _uiStateFlow.value = UiState.Error
            }
        }

    fun logout() = viewModelScope.launch {
        firebaseAuth.signOut()
        sharedPref.edit(true) {
            clear()
        }
    }

    private fun userToMap(userModel: UserModel): HashMap<String, String> {
        return hashMapOf(
            userFieldId to userModel.uid,
            userFieldName to userModel.userName,
            userFieldEmail to userModel.userEmail,
            userFieldPhoneNumber to userModel.userPhoneNumber,
            userFieldProfileImage to userModel.userProfileImage,
            userFieldDescription to userModel.userDescription
        )
    }

    private fun mapToUser(result: DocumentSnapshot): UserModel {
        return UserModel(
            uid = result[userFieldId].toString(),
            userName = result[userFieldName].toString(),
            userEmail = result[userFieldEmail].toString(),
            userPhoneNumber = result[userFieldPhoneNumber].toString(),
            userDescription = result[userFieldDescription].toString(),
            userProfileImage = result[userFieldProfileImage].toString(),
            id = 0
        )
    }

    private fun saveUserOnPrefs(user: UserModel) {
        val gson = Gson()
        val stringUserJson = gson.toJson(user)
        sharedPref.edit(true) {
            putString(USER_SAVED_SHARED_PREF_KEY, stringUserJson)
        }
        _userFlow.value = gson.fromJson(stringUserJson, UserModel::class.java)
    }

    fun getLoggedUserFromPref(): UserModel? {
        if (sharedPref.contains(USER_SAVED_SHARED_PREF_KEY)) {
            val userJson = sharedPref.getString(USER_SAVED_SHARED_PREF_KEY, null)
            val gson = Gson()
            val user = gson.fromJson(userJson, UserModel::class.java)
            _userFlow.value = user
            return user
        } else return null
    }
}