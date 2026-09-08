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
import com.nothingcleaner.scanner.StorageScanner
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(onScanComplete: (List<ScannerItem>) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(true) }
    var scannedItems by remember { mutableStateOf<List<ScannerItem>>(emptyList()) }
    var currentPhase by remember { mutableStateOf("Initializing...") }

    LaunchedEffect(Unit) {
        val scanner = StorageScanner(context)
        val allItems = mutableListOf<ScannerItem>()
        
        currentPhase = "Scanning Downloads..."
        allItems.addAll(scanner.scanDownloads())
        
        currentPhase = "Scanning Large Files..."
        allItems.addAll(scanner.scanLargeFiles())
        
        scannedItems = allItems
        isScanning = false
        onScanComplete(allItems)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "SCANNING",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isScanning) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = currentPhase,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
