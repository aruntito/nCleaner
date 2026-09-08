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
import com.nothingcleaner.scanner.FileCategory
import com.nothingcleaner.scanner.ScannerItem
import com.nothingcleaner.ui.CleanerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CategoryReviewScreen(viewModel: CleanerViewModel, category: FileCategory, onBack: () -> Unit, onReviewSelection: () -> Unit, onPreview: (String) -> Unit) {
    val scannedItems by viewModel.scannedItems.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    
    val categoryItems = scannedItems.filter { it.category == category }
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
                text = String.format(Locale.US, "%d ITEMS   %.1f MB", categoryItems.size, totalSizeMb),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(categoryItems) { item ->
                    val isSelected = selectedItemIds.contains(item.id)
                    val dateStr = if (item.dateAdded > 0) dateFormat.format(Date(item.dateAdded * 1000L)) else ""
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail - clicking opens preview
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onPreview(item.id) }
                        ) {
                            if (item.mimeType.startsWith("image/") || item.mimeType.startsWith("video/")) {
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
                        
                        // Details - clicking opens preview
                        Column(modifier = Modifier.weight(1f).clickable { onPreview(item.id) }) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f MB  %s", item.sizeBytes / (1024 * 1024.0), dateStr),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Select Action
                        Text(
                            text = if (isSelected) "SELECTED" else "SELECT",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.toggleSelection(item.id) }.padding(8.dp)
                        )
                    }
                }
            }
        }
        
        // Sticky Selection Footer
        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedCategoryItems = categoryItems.filter { it.id in selectedItemIds }
            val selSizeMb = selectedCategoryItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
            
            Column {
                Text(
                    text = "${selectedItemIds.size} SELECTED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selectedItemIds.isNotEmpty()) {
                    val globalSelSize = scannedItems.filter { it.id in selectedItemIds }.sumOf { it.sizeBytes } / (1024 * 1024.0)
                    Text(
                        text = String.format(Locale.US, "%.1f MB TOTAL", globalSelSize),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (selectedItemIds.isNotEmpty()) {
                Button(
                    onClick = onReviewSelection,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("REVIEW SELECTION", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
