package com.nothingcleaner.cleaner

sealed interface DeleteState {
    object Idle : DeleteState
    data class RequestingConfirmation(val itemCount: Int, val bytes: Long) : DeleteState
    object Deleting : DeleteState
    data class Complete(val result: DeletionResult) : DeleteState
    data class Error(val message: String) : DeleteState
}

data class DeletionResult(
    val deletedCount: Int,
    val failedCount: Int,
    val recoveredBytes: Long
)
