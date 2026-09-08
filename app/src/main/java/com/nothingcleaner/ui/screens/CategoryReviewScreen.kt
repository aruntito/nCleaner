package com.nothingcleaner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nothingcleaner.core.model.PreviewType
import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.core.model.StorageItem
import com.nothingcleaner.ui.components.PrimaryButton
import com.nothingcleaner.viewmodel.ScanState
import com.nothingcleaner.viewmodel.StorageViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CategoryReviewScreen(viewModel: StorageViewModel, category: StorageCategory, onBack: () -> Unit, onReviewSelection: () -> Unit, onPreview: (Long) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val analysis = (uiState.scanState as? ScanState.Complete)?.analysis ?: return
    
    val categoryItems = if (category == StorageCategory.DUPLICATES) {
        analysis.duplicateGroups.flatten().distinctBy { it.id }
    } else {
        analysis.categories[category] ?: emptyList()
    }
    
    val totalSizeMb = categoryItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
    
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(24.dp).weight(1f)) {
            Text(
                text = "← BACK",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(bottom = 16.dp)
            )
            
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = String.format(Locale.US, "%.1f MB • %d FILES", totalSizeMb, categoryItems.size),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (categoryItems.isEmpty()) {
                Text(
                    text = "No ${category.displayName.lowercase()} found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(categoryItems) { item ->
                        val isSelected = uiState.selectedItemIds.contains(item.id)
                        val dateStr = if (item.dateAdded != null && item.dateAdded > 0) dateFormat.format(Date(item.dateAdded * 1000L)) else ""
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onPreview(item.id) }
                            ) {
                                if (item.previewType == PreviewType.IMAGE || item.previewType == PreviewType.VIDEO) {
                                    AsyncImage(
                                        model = item.uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = "FILE",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f).clickable { onPreview(item.id) }) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f MB • %s", item.sizeBytes / (1024 * 1024.0), dateStr),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = if (isSelected) "[ SELECTED ✓ ]" else "KEEP   [ SELECT ]",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { viewModel.toggleSelection(item.id) }.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedCategoryItems = categoryItems.filter { it.id in uiState.selectedItemIds }
            val selSizeMb = selectedCategoryItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
            
            Column {
                Text(
                    text = "${selectedCategoryItems.size} SELECTED IN CATEGORY",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                if (selectedCategoryItems.isNotEmpty()) {
                    Text(
                        text = String.format(Locale.US, "%.1f MB", selSizeMb),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (uiState.selectedItemIds.isNotEmpty()) {
                PrimaryButton(
                    text = "REVIEW SELECTION",
                    onClick = onReviewSelection,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}
