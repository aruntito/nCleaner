package com.nothingcleaner.viewmodel

import com.nothingcleaner.cleaner.DeleteState
import com.nothingcleaner.core.model.StorageSummary

data class StorageUiState(
    val scanState: ScanState = ScanState.Idle,
    val storageSummary: StorageSummary? = null,
    val selectedItemIds: Set<Long> = emptySet(),
    val deletionState: DeleteState = DeleteState.Idle,
    val error: String? = null
)
