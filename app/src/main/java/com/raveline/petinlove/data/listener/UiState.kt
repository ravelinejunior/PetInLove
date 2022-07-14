package com.raveline.petinlove.data.listener
 open class UiState {
    object Success : UiState()
    object Error : UiState()
    object NoConnection : UiState()
    object Loading : UiState()
    object Initial : UiState()
}