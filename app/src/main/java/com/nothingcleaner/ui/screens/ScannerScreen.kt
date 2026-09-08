package com.nothingcleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nothingcleaner.ui.components.SecondaryButton
import com.nothingcleaner.viewmodel.ScanState
import com.nothingcleaner.viewmodel.StorageViewModel

@Composable
fun ScannerScreen(viewModel: StorageViewModel, onScanComplete: () -> Unit, onCancel: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scanState = uiState.scanState

    LaunchedEffect(scanState) {
        if (scanState is ScanState.Complete) {
            onScanComplete()
        }
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

        Spacer(modifier = Modifier.weight(1f))

        when (scanState) {
            is ScanState.Scanning -> {
                Text(
                    text = scanState.currentStep.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = scanState.progress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                Text(
                    text = "${(scanState.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                )
            }
            is ScanState.Error -> {
                Text(
                    text = "ERROR",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = scanState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            else -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SecondaryButton(
            text = "CANCEL",
            onClick = {
                viewModel.cancelScan()
                onCancel()
            }
        )
    }
}
