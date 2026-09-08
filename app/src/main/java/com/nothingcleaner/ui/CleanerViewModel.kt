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
            
            _scanPhase.value = "Checking storage..."
            delay(500)

            _scanPhase.value = "Scanning media & files..."
            val rawItems = scanner.scanAllMedia()
            
            _scanPhase.value = "Organizing results..."
            val seenIds = mutableSetOf<String>()
            val categorized = mutableListOf<ScannerItem>()
            
            // Priority assignment to avoid double counting
            for (item in rawItems.sortedByDescending { it.sizeBytes }) {
                if (!seenIds.add(item.id)) continue
                
                val pathStr = item.path.lowercase()
                val nameStr = item.name.lowercase()
                val isVideo = item.mimeType.startsWith("video/")
                val isImage = item.mimeType.startsWith("image/")
                
                val category = when {
                    isVideo && (pathStr.contains("screenrecord") || nameStr.contains("screen")) -> FileCategory.SCREEN_RECORDINGS
                    isImage && (pathStr.contains("screenshot") || nameStr.contains("screenshot")) -> FileCategory.SCREENSHOTS
                    pathStr.contains("/download/") -> FileCategory.DOWNLOADS
                    item.sizeBytes > 100 * 1024 * 1024 -> FileCategory.LARGE_FILES
                    isVideo -> FileCategory.VIDEOS
                    isImage -> FileCategory.IMAGES
                    else -> FileCategory.DOWNLOADS // Fallback
                }
                
                categorized.add(item.copy(category = category))
            }

            _scannedItems.value = categorized
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
