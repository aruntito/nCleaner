package com.nothingcleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.scanner.ScannerItem
import com.nothingcleaner.storage.CleanerService
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ResultsScreen(items: List<ScannerItem>, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedItems by remember { mutableStateOf(items.toSet()) }
    var isCleaning by remember { mutableStateOf(false) }

    val totalSelectedBytes = selectedItems.sumOf { it.sizeBytes }
    val totalSelectedMb = totalSelectedBytes / (1024 * 1024.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "CLEANABLE",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = String.format(Locale.US, "%.1f MB", totalSelectedMb),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedItems.contains(item),
                        onCheckedChange = { checked ->
                            selectedItems = if (checked) {
                                selectedItems + item
                            } else {
                                selectedItems - item
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1
                        )
                        Text(
                            text = "${item.sizeBytes / (1024 * 1024)} MB | ${item.category.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                isCleaning = true
                scope.launch {
                    val cleaner = CleanerService(context)
                    cleaner.deleteItems(selectedItems.toList())
                    isCleaning = false
                    onDone()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedItems.isNotEmpty() && !isCleaning,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                if (isCleaning) "CLEANING..." else String.format(Locale.US, "CLEAN %.1f MB", totalSelectedMb), 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
