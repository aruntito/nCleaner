package com.nothingcleaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.storage.CleanerService
import com.nothingcleaner.ui.CleanerViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CleanReviewScreen(viewModel: CleanerViewModel, onCancel: () -> Unit, onCleanComplete: () -> Unit, onPreview: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val scannedItems by viewModel.scannedItems.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    
    val selectedItems = scannedItems.filter { it.id in selectedItemIds }
    val totalSelectedMb = selectedItems.sumOf { it.sizeBytes } / (1024 * 1024.0)

    var isCleaning by remember { mutableStateOf(false) }

    // If selections become empty (e.g. they unchecked everything from preview), auto go back
    LaunchedEffect(selectedItemIds) {
        if (selectedItemIds.isEmpty() && !isCleaning) {
            onCancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "READY TO CLEAN",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = String.format(Locale.US, "%d FILES | %.1f MB", selectedItems.size, totalSelectedMb),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "YOU SELECTED",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            items(selectedItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPreview(item.id) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(end = 16.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f MB  ›", item.sizeBytes / (1024 * 1024.0)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "These selected files will be permanently removed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isCleaning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text("CANCEL", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = {
                    isCleaning = true
                    scope.launch {
                        val cleaner = CleanerService(context)
                        cleaner.deleteItems(selectedItems)
                        isCleaning = false
                        onCleanComplete()
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isCleaning && selectedItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    if (isCleaning) "CLEANING..." else String.format(Locale.US, "[ CLEAN %.1f MB ]", totalSelectedMb), 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
