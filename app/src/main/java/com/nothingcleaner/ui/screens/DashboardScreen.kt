package com.nothingcleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.ui.components.PrimaryButton
import com.nothingcleaner.viewmodel.StorageViewModel
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: StorageViewModel, onAnalyze: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.storageSummary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "CLEANER",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "STORAGE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (summary != null) {
            val usedGb = summary.usedBytes / (1024 * 1024 * 1024.0)
            val totalGb = summary.totalBytes / (1024 * 1024 * 1024.0)
            val progress = (summary.usedBytes.toFloat() / summary.totalBytes.toFloat()).coerceIn(0f, 1f)

            Text(
                text = String.format(Locale.US, "%.1f GB", usedGb),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = String.format(Locale.US, "USED OF %.1f GB", totalGb),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Text(
                text = "${(progress * 100).toInt()}% USED",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "STORAGE ANALYSIS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Understand what's taking space\nbefore deleting anything.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        PrimaryButton(
            text = "ANALYZE STORAGE",
            onClick = {
                viewModel.startScan()
                onAnalyze()
            }
        )
    }
}
