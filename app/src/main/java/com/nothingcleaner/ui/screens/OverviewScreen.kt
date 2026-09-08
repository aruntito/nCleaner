package com.nothingcleaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.scanner.FileCategory
import com.nothingcleaner.ui.CleanerViewModel
import com.nothingcleaner.util.StorageUtil
import java.util.Locale

@Composable
fun OverviewScreen(viewModel: CleanerViewModel, onCategorySelected: (FileCategory) -> Unit, onReviewClean: () -> Unit) {
    val scannedItems by viewModel.scannedItems.collectAsState()
    val stats = StorageUtil.getStorageStats()

    val totalReviewableMb = scannedItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
    val usedGb = stats.usedBytes / (1024 * 1024 * 1024.0)
    val totalGb = stats.totalBytes / (1024 * 1024 * 1024.0)

    val grouped = scannedItems.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "STORAGE ANALYSIS",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = String.format(Locale.US, "%.1f GB TOTAL   %.1f GB USED", totalGb, usedGb),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        
        Text(
            text = String.format(Locale.US, "CAN FREE UP %.1f MB", totalReviewableMb),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            val order = listOf(
                FileCategory.LARGE_FILES,
                FileCategory.DOWNLOADS,
                FileCategory.SCREEN_RECORDINGS,
                FileCategory.SCREENSHOTS,
                FileCategory.VIDEOS,
                FileCategory.IMAGES
            )
            
            items(order) { category ->
                val items = grouped[category] ?: emptyList()
                val sizeMb = items.sumOf { it.sizeBytes } / (1024 * 1024.0)
                CategoryRow(
                    title = category.displayName,
                    subtitle = String.format(Locale.US, "%.1f MB | %d items", sizeMb, items.size),
                    actionText = "REVIEW →",
                    onClick = { onCategorySelected(category) }
                )
            }

            // Duplicates placeholder
            item {
                CategoryRow(
                    title = "DUPLICATES",
                    subtitle = "Not analyzed",
                    actionText = "ANALYZE →",
                    onClick = { /* Future Implementation */ }
                )
            }

        }
        
        val selectedItemIds by viewModel.selectedItemIds.collectAsState()
        if (selectedItemIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            val globalSelSize = scannedItems.filter { it.id in selectedItemIds }.sumOf { it.sizeBytes } / (1024 * 1024.0)
            Button(
                onClick = onReviewClean,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(String.format(java.util.Locale.US, "%d SELECTED   %.1f MB   [ REVIEW SELECTION ]", selectedItemIds.size, globalSelSize), fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun CategoryRow(title: String, subtitle: String, actionText: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}
