package com.nothingcleaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.scanner.FileCategory
import com.nothingcleaner.scanner.ScannerItem
import com.nothingcleaner.ui.CleanerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CategoryReviewScreen(viewModel: CleanerViewModel, category: FileCategory, onBack: () -> Unit) {
    val scannedItems by viewModel.scannedItems.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    
    val categoryItems = scannedItems.filter { it.category == category }
    val totalSizeMb = categoryItems.sumOf { it.sizeBytes } / (1024 * 1024.0)
    val selectedSizeMb = categoryItems.filter { it.id in selectedItemIds }.sumOf { it.sizeBytes } / (1024 * 1024.0)
    
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
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
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(categoryItems) { item ->
                val isSelected = selectedItemIds.contains(item.id)
                val dateStr = if (item.dateAdded > 0) "Added " + dateFormat.format(Date(item.dateAdded * 1000L)) else ""
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (!isSelected) "[ KEEP ]" else "  KEEP  ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (!isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.clickable { if (isSelected) viewModel.toggleSelection(item.id) }.padding(end = 16.dp)
                        )
                        Text(
                            text = if (isSelected) "[ SELECT ]" else "  SELECT  ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.clickable { if (!isSelected) viewModel.toggleSelection(item.id) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                "BACK TO OVERVIEW", 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
