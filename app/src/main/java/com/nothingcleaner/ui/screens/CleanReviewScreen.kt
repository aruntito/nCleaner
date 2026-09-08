package com.nothingcleaner.ui.screens

import androidx.compose.foundation.clickable
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
import coil.compose.AsyncImage
import com.nothingcleaner.core.model.PreviewType
import com.nothingcleaner.ui.components.DestructiveButton
import com.nothingcleaner.ui.components.SecondaryButton
import com.nothingcleaner.viewmodel.ScanState
import com.nothingcleaner.viewmodel.StorageViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CleanReviewScreen(viewModel: StorageViewModel, onCancel: () -> Unit, onCleanComplete: () -> Unit, onPreview: (Long) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val analysis = (uiState.scanState as? ScanState.Complete)?.analysis ?: return
    val selectedItemIds = uiState.selectedItemIds
    
    val selectedItems = analysis.categories.values.flatten().distinctBy { it.id }.filter { it.id in selectedItemIds }
    val totalSelectedMb = selectedItems.sumOf { it.sizeBytes } / (1024 * 1024.0)

    val scope = rememberCoroutineScope()
    var isCleaning by remember { mutableStateOf(false) }

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
            text = String.format(Locale.US, "%.1f MB • %d FILES SELECTED", totalSelectedMb, selectedItems.size),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(selectedItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPreview(item.id) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp)) {
                        if (item.previewType == PreviewType.IMAGE || item.previewType == PreviewType.VIDEO) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "FILE", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(end = 16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "WHAT HAPPENS NEXT",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Android will ask you to confirm the deletion.\nNothing else will be removed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SecondaryButton(
                text = "BACK",
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isCleaning
            )
            
            DestructiveButton(
                text = if (isCleaning) "WAITING..." else "CLEAN",
                onClick = {
                    isCleaning = true
                    onCleanComplete()
                },
                modifier = Modifier.weight(1f),
                enabled = !isCleaning && selectedItems.isNotEmpty()
            )
        }
    }
}
