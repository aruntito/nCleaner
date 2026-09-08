package com.nothingcleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.ui.CleanerViewModel

@Composable
fun ScannerScreen(viewModel: CleanerViewModel, onScanComplete: () -> Unit) {
    val phase by viewModel.scanPhase.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.startScan(onComplete = onScanComplete)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "ANALYZING STORAGE",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Simulating the log-like UI the user requested
        val phases = listOf(
            "Checking available storage...",
            "Scanning downloads...",
            "Finding large files...",
            "Organizing results..."
        )
        
        phases.forEach { p ->
            val isCurrent = phase == p
            val isDone = phases.indexOf(p) < phases.indexOf(phase)
            
            val icon = when {
                isDone -> "✓"
                isCurrent -> "●"
                else -> "○"
            }
            
            Text(
                text = "$icon $p",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDone || isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
