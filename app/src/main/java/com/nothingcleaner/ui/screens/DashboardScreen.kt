package com.nothingcleaner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothingcleaner.ui.theme.NothingRed
import com.nothingcleaner.util.StorageUtil
import java.util.Locale

@Composable
fun DashboardScreen(onStartScan: () -> Unit) {
    var stats by remember { mutableStateOf(StorageUtil.getStorageStats()) }

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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val usedGb = stats.usedBytes / (1024 * 1024 * 1024.0)
        val totalGb = stats.totalBytes / (1024 * 1024 * 1024.0)
        
        Text(
            text = String.format(Locale.US, "%.1f GB", usedGb),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = String.format(Locale.US, "OF %.1f GB USED", totalGb),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LinearProgressIndicator(
            progress = { (stats.usedBytes.toDouble() / stats.totalBytes).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = NothingRed,
            trackColor = MaterialTheme.colorScheme.surface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                "SCAN", 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
