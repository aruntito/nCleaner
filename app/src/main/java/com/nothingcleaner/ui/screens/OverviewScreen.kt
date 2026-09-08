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
import java.util.Locale

@Composable
fun OverviewScreen(viewModel: CleanerViewModel, onCategorySelected: (FileCategory) -> Unit, onReviewClean: () -> Unit) {
    val scannedItems by viewModel.scannedItems.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()

    val totalReviewableMb = scannedItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
    val totalSelectedSizeMb = scannedItems.filter { it.id in selectedItemIds }.sumOf { it.sizeBytes } / (1024 * 1024.0)

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
        Text(
            text = String.format(Locale.US, "%.1f MB REVIEWABLE FILES", totalReviewableMb),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            val categoriesToShow = listOf(FileCategory.LARGE_FILES, FileCategory.DOWNLOADS, FileCategory.SCREEN_RECORDINGS, FileCategory.SCREENSHOTS)
            
            items(categoriesToShow) { category ->
                val items = grouped[category] ?: emptyList()
                if (items.isNotEmpty()) {
                    val sizeMb = items.sumOf { it.sizeBytes } / (1024 * 1024.0)
                    CategoryRow(
                        title = category.displayName,
                        subtitle = String.format(Locale.US, "%.1f MB | %d files", sizeMb, items.size),
                        actionText = "[ REVIEW → ]",
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            // Duplicates placeholder
            item {
                CategoryRow(
                    title = FileCategory.DUPLICATE_CANDIDATES.displayName,
                    subtitle = "NOT ANALYZED",
                    actionText = "[ ANALYZE → ]",
                    onClick = { /* Future Implementation */ }
                )
            }
        }

        if (selectedItemIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReviewClean,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    String.format(Locale.US, "%d FILES SELECTED | %.1f MB  [ REVIEW ]", selectedItemIds.size, totalSelectedSizeMb), 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
