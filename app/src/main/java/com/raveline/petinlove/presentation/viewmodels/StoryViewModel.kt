package com.raveline.petinlove.presentation.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.raveline.petinlove.data.listener.UiState
import com.raveline.petinlove.data.model.StoryModel
import com.raveline.petinlove.data.model.UserModel
import com.raveline.petinlove.domain.repository_impl.StoryRepositoryImpl
import com.raveline.petinlove.domain.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepositoryImpl,
    private val application: Application
) : ViewModel() {

    private val _storyStateFlow = MutableStateFlow<List<StoryModel>>(emptyList())
    val storyFlow: StateFlow<List<StoryModel>> get() = _storyStateFlow

    private val _uiStateFlow = MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow: StateFlow<UiState> get() = _uiStateFlow

    private val _idsStoryStateFlow = MutableStateFlow<List<String>>(emptyList())
    val idsFlow: StateFlow<List<String>> get() = _idsStoryStateFlow

    val user = SystemFunctions.getLoggedUserFromPref(application)

    suspend fun getUserById(userId: String): DocumentReference = storyRepository.getUserById(userId)

    fun getActiveStories() = viewModelScope.launch {

        storyRepository.getActiveStories().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = arrayListOf<String>()
                if (snapshot.hasChildren()) {
                    for (doc in snapshot.children) {
                        list.add(doc.key.toString())
                    }
                    _idsStoryStateFlow.value = list
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun setStoryOnServer(
        user: UserModel,
        imageUri: Uri,
        extension: String,
    ) = viewModelScope.launch {
        try {
            if (SystemFunctions.isNetworkAvailable(application.baseContext)) {
                _uiStateFlow.value = UiState.Loading
                storyRepository.saveImageToServerStory(imageUri, extension, user.uid)
                    .addOnSuccessListener { mTask ->
                        if (mTask.metadata != null) {
                            val downloadUriListener = mTask.metadata!!.reference?.downloadUrl
                            downloadUriListener?.addOnSuccessListener { bImageUri ->
                                viewModelScope.launch(IO) {
                                    val timeEnd = System.currentTimeMillis() + 86400000
                                    val storyId =
                                        UUID.randomUUID().toString().replace("-", "").trim()
                                    val storyMap = hashMapOf<String, Any>(
                                        storyIdFieldStory to storyId,
                                        imagePathFieldStory to bImageUri.toString(),
                                        userNameFieldStory to user.userName,
                                        isSeenFieldStory to false,
                                        userIdFieldStory to user.uid,
                                        viewsFieldStory to 0,
                                        timeStartFieldStory to System.currentTimeMillis(),
                                        timeEndFieldStory to timeEnd,
                                        userImageFieldStory to user.userProfileImage
                                    )

                                    storyRepository.saveStory(user.uid, storyId, storyMap)

                                    storyRepository.setStoryOnServer(user.uid, storyId, storyMap)
                                        .addOnCompleteListener { mTask ->
                                            if (mTask.isSuccessful) {
                                                _uiStateFlow.value = UiState.Success
                                            } else {
                                                _uiStateFlow.value = UiState.Error
                                            }
                                        }
                                }
                            }
                        }

                    }
            } else {
                _uiStateFlow.value = UiState.NoConnection
                Log.e("TAGStoryFlow", "No Connection")
            }
        } catch (e: Exception) {
            _uiStateFlow.value = UiState.Error
            e.printStackTrace()
        }
    }

    fun setIds(ids: List<String>) {
        _idsStoryStateFlow.value = ids
    }

}