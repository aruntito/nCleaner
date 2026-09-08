package com.nothingcleaner.scanner

import android.content.Context
import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.core.model.StorageItem
import com.nothingcleaner.viewmodel.ScanState
import com.nothingcleaner.viewmodel.ScanStep
import com.nothingcleaner.viewmodel.StorageAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScanCoordinator(private val context: Context) {
    private val mediaScanner = MediaScanner(context)
    private val downloadsScanner = DownloadsScanner(context)
    private val duplicateScanner = DuplicateScanner(context)

    suspend fun performScan(stateFlow: MutableStateFlow<ScanState>) {
        try {
            stateFlow.value = ScanState.Scanning(0.1f, ScanStep.CHECKING_STORAGE)
            
            stateFlow.value = ScanState.Scanning(0.3f, ScanStep.SCANNING_MEDIA)
            val mediaItems = mediaScanner.scan()
            
            stateFlow.value = ScanState.Scanning(0.6f, ScanStep.SCANNING_DOWNLOADS)
            val downloadItems = downloadsScanner.scan()
            
            val allItems = (mediaItems + downloadItems).distinctBy { it.id }
            
            stateFlow.value = ScanState.Scanning(0.8f, ScanStep.FINDING_DUPLICATES)
            val duplicateGroups = duplicateScanner.findDuplicates(allItems)
            
            stateFlow.value = ScanState.Scanning(0.9f, ScanStep.ORGANIZING_RESULTS)
            
            val categorizedMap = mutableMapOf<StorageCategory, MutableList<StorageItem>>()
            for (category in StorageCategory.values()) {
                categorizedMap[category] = mutableListOf()
            }
            
            for (item in allItems) {
                for (cat in item.categories) {
                    categorizedMap[cat]?.add(item)
                }
            }
            
            // Add duplicates to category manually, since they are determined post-scan
            val allDuplicateItems = duplicateGroups.flatten()
            categorizedMap[StorageCategory.DUPLICATES]?.addAll(allDuplicateItems)
            
            stateFlow.value = ScanState.Complete(
                StorageAnalysis(
                    categories = categorizedMap,
                    duplicateGroups = duplicateGroups
                )
            )
        } catch (e: Exception) {
            stateFlow.value = ScanState.Error(e.message ?: "Unknown scanning error")
        }
    }
}
