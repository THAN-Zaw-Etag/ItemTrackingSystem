package com.tzh.itemTrackingSystem.ulti

sealed class SaveResult(val errorMessage: String? = null) {
    data object Loading : SaveResult()

    class Error(message: String) : SaveResult(message)

    data object Success : SaveResult()
}