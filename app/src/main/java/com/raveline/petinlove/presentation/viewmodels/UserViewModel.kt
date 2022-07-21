package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    private val application: Application,
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

    private val _userFlow = MutableStateFlow<UserModel?>(null)
    val userModel: StateFlow<UserModel?> get() = _userFlow

    var imagePath = ""
    var profileImage = ""
    var result = ""

    fun getUserData() {

        viewModelScope.launch {
            userRepository.getUserDataFromServer(firebaseAuth.uid.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result
                        _userFlow.value = mapToUser(result)
                        profileImage = result[userFieldProfileImage].toString()
                    }
                }
        }
    }

    fun editUserData(name: String?, phone: String?, description: String?) =
        viewModelScope.launch {

            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                _uiStateFlow.value = UiState.Loading
                if (userModel.value != null && imagePath.isNotEmpty()) {
                    storeImage(name, phone, description)
                }else{
                    updateUserWithoutImage(name, phone, description)
                }
            } else {
                _uiStateFlow.value = UiState.NoConnection
                result = "No Internet Connection!"
            }
        }

    private fun updateUserWithoutImage(name: String?, phone: String?, description: String?) = viewModelScope.launch {
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

                                userRepository.editUserDataFromServer(user, map)
                                    .addOnCompleteListener { inTask ->
                                        if (inTask.isSuccessful) {
                                            result = "Successfully Edited!"
                                            _uiStateFlow.value = UiState.Success
                                        } else {
                                            result = inTask.result.toString()
                                            _uiStateFlow.value = UiState.Error
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
                            _uiStateFlow.value = UiState.Success
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
                                uid = task.result.user?.uid.toString()
                            )

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

    private fun checkNonFilledFields(
        name: String,
        city: String,
        address: String,
        number: String
    ): Boolean {
        if (name.isNotEmpty()) {
            if (city.isNotEmpty()) {
                if (address.isNotEmpty()) {
                    if (number.isNotEmpty()) {
                        return number.isNotEmpty()
                    } else {
                        return false
                    }
                } else {
                    return false
                }
            } else {
                return false
            }
        } else {
            return false
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
}