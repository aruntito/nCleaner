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
import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.ui.components.PrimaryButton
import com.nothingcleaner.viewmodel.ScanState
import com.nothingcleaner.viewmodel.StorageViewModel
import java.util.Locale

@Composable
fun OverviewScreen(viewModel: StorageViewModel, onCategorySelected: (StorageCategory) -> Unit, onReviewClean: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    val analysis = (uiState.scanState as? ScanState.Complete)?.analysis ?: return

    val totalReviewableMb = analysis.categories.values.flatten().distinctBy { it.id }.sumOf { it.sizeBytes } / (1024 * 1024.0)
    val duplicateMb = analysis.duplicateGroups.flatten().distinctBy { it.id }.sumOf { it.sizeBytes } / (1024 * 1024.0)
    
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
        
        Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        
        Text(
            text = String.format(Locale.US, "%.1f MB REVIEWABLE FILES", totalReviewableMb),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            val order = listOf(
                StorageCategory.SCREEN_RECORDINGS,
                StorageCategory.LARGE_FILES,
                StorageCategory.DOWNLOADS,
                StorageCategory.SCREENSHOTS,
                StorageCategory.OTHER_MEDIA
            )
            
            items(order) { category ->
                val catItems = analysis.categories[category] ?: emptyList()
                val sizeMb = catItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
                CategoryRow(
                    title = category.displayName,
                    subtitle = String.format(Locale.US, "%.1f MB • %d files", sizeMb, catItems.size),
                    actionText = "[ REVIEW → ]",
                    onClick = { onCategorySelected(category) }
                )
            }

            // Duplicates
            item {
                CategoryRow(
                    title = StorageCategory.DUPLICATES.displayName,
                    subtitle = String.format(Locale.US, "%.1f MB • %d files", duplicateMb, analysis.duplicateGroups.flatten().distinctBy { it.id }.size),
                    actionText = "[ REVIEW → ]",
                    onClick = { onCategorySelected(StorageCategory.DUPLICATES) }
                )
            }
        }
        
        if (uiState.selectedItemIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            val globalSelSize = analysis.categories.values.flatten().distinctBy { it.id }.filter { it.id in uiState.selectedItemIds }.sumOf { it.sizeBytes } / (1024 * 1024.0)
            PrimaryButton(
                text = String.format(Locale.US, "%d SELECTED   %.1f MB   [ REVIEW SELECTION ]", uiState.selectedItemIds.size, globalSelSize),
                onClick = onReviewClean
            )
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
        Divider(modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    }
}
