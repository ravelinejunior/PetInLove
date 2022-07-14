package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val firestore: FirebaseFirestore,
    private val application: Application,
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

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

                            firestore.collection(userDatabaseReference).document(user.uid)
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

    fun logout(firebaseAuth: FirebaseAuth) = viewModelScope.launch {
        firebaseAuth.signOut()
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
            userFieldProfileImage to userModel.userProfileImage
        )
    }
}