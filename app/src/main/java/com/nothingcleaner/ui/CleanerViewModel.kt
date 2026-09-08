package com.nothingcleaner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothingcleaner.scanner.FileCategory
import com.nothingcleaner.scanner.ScannerItem
import com.nothingcleaner.scanner.StorageScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CleanerViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = StorageScanner(application)

    private val _scannedItems = MutableStateFlow<List<ScannerItem>>(emptyList())
    val scannedItems: StateFlow<List<ScannerItem>> = _scannedItems.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds.asStateFlow()

    private val _scanPhase = MutableStateFlow("")
    val scanPhase: StateFlow<String> = _scanPhase.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun startScan(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isScanning.value = true
            _selectedItemIds.value = emptySet()
            _scannedItems.value = emptyList()
            
            val allItems = mutableListOf<ScannerItem>()
            val seenIds = mutableSetOf<String>()

            _scanPhase.value = "Checking available storage..."
            delay(500) // Small intentional delay to show the phase

            _scanPhase.value = "Scanning downloads..."
            val downloads = scanner.scanDownloads()
            for (item in downloads) {
                if (seenIds.add(item.id)) allItems.add(item)
            }

            _scanPhase.value = "Finding large files..."
            val largeFiles = scanner.scanLargeFiles()
            for (item in largeFiles) {
                // Determine true category to avoid double counting
                if (seenIds.add(item.id)) {
                    val isScreenRecording = item.name.contains("Screen", ignoreCase = true) || item.path.contains("Screenrecord", ignoreCase = true)
                    val cat = if (isScreenRecording) FileCategory.SCREEN_RECORDINGS else FileCategory.LARGE_FILES
                    allItems.add(item.copy(category = cat))
                }
            }

            _scanPhase.value = "Organizing results..."
            delay(300)

            _scannedItems.value = allItems
            _isScanning.value = false
            onComplete()
        }
    }

    fun toggleSelection(id: String) {
        val current = _selectedItemIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedItemIds.value = current
    }

    fun clearScannedItems() {
        _scannedItems.value = emptyList()
        _selectedItemIds.value = emptySet()
    }
}
