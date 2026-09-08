package com.nothingcleaner.viewmodel

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothingcleaner.cleaner.DeleteRequestManager
import com.nothingcleaner.cleaner.DeleteState
import com.nothingcleaner.core.model.StorageItem
import com.nothingcleaner.core.storage.StorageRepository
import com.nothingcleaner.scanner.ScanCoordinator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StorageViewModel(application: Application) : AndroidViewModel(application) {
    private val storageRepository = StorageRepository(application)
    private val scanCoordinator = ScanCoordinator(application)
    private val deleteRequestManager = DeleteRequestManager(application)

    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        refreshStorageSummary()
    }

    fun refreshStorageSummary() {
        viewModelScope.launch {
            val summary = storageRepository.getStorageSummary()
            _uiState.update { it.copy(storageSummary = summary) }
        }
    }

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            val scanStateFlow = MutableStateFlow<ScanState>(ScanState.Idle)
            
            // Collect scan progress
            launch {
                scanStateFlow.collect { state ->
                    _uiState.update { it.copy(scanState = state) }
                }
            }
            
            scanCoordinator.performScan(scanStateFlow)
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        _uiState.update { it.copy(scanState = ScanState.Idle) }
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val current = state.selectedItemIds.toMutableSet()
            if (current.contains(id)) {
                current.remove(id)
            } else {
                current.add(id)
            }
            state.copy(selectedItemIds = current)
        }
    }
    
    fun clearSelection() {
        _uiState.update { it.copy(selectedItemIds = emptySet()) }
    }

    suspend fun requestDeletion(): IntentSender? {
        val state = _uiState.value
        if (state.scanState !is ScanState.Complete) return null
        
        val analysis = state.scanState.analysis
        val selectedItems = analysis.categories.values.flatten().distinctBy { it.id }.filter { state.selectedItemIds.contains(it.id) }
        
        val totalBytes = selectedItems.sumOf { it.sizeBytes }
        _uiState.update { it.copy(deletionState = DeleteState.RequestingConfirmation(selectedItems.size, totalBytes)) }
        
        return deleteRequestManager.createDeleteRequest(selectedItems)
    }

    fun handleDeletionConfirmed() {
        viewModelScope.launch {
            _uiState.update { it.copy(deletionState = DeleteState.Deleting) }
            val state = _uiState.value
            if (state.scanState !is ScanState.Complete) return@launch
            
            val selectedItems = state.scanState.analysis.categories.values.flatten().distinctBy { it.id }.filter { state.selectedItemIds.contains(it.id) }
            val result = deleteRequestManager.executeDirectDeletion(selectedItems)
            
            _uiState.update { it.copy(deletionState = DeleteState.Complete(result), selectedItemIds = emptySet()) }
            
            // Re-fetch storage summary and rescan to update UI correctly
            refreshStorageSummary()
            startScan()
        }
    }

    fun handleDeletionCancelled() {
        _uiState.update { it.copy(deletionState = DeleteState.Idle) }
    }
    
    fun resetDeletionState() {
        _uiState.update { it.copy(deletionState = DeleteState.Idle) }
    }
}
