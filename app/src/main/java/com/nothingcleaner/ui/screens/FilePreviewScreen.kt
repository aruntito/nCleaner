package com.nothingcleaner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nothingcleaner.core.model.PreviewType
import com.nothingcleaner.core.model.StorageItem
import com.nothingcleaner.ui.components.PrimaryButton
import com.nothingcleaner.ui.components.SecondaryButton
import com.nothingcleaner.viewmodel.ScanState
import com.nothingcleaner.viewmodel.StorageViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilePreviewScreen(viewModel: StorageViewModel, itemId: Long, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val analysis = (uiState.scanState as? ScanState.Complete)?.analysis ?: return
    
    val item = analysis.categories.values.flatten().find { it.id == itemId } ?: analysis.duplicateGroups.flatten().find { it.id == itemId } ?: return
    val isSelected = uiState.selectedItemIds.contains(item.id)
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val dateStr = if (item.dateAdded != null && item.dateAdded > 0) dateFormat.format(Date(item.dateAdded * 1000L)) else ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.clickable { onBack() }.padding(16.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = String.format(Locale.US, "%.1f MB • %s", item.sizeBytes / (1024 * 1024.0), dateStr),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (item.previewType) {
                PreviewType.VIDEO -> VideoPreview(item)
                PreviewType.IMAGE -> AsyncImage(
                    model = item.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                else -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("FILE INFORMATION", color = Color.White)
                        Text(item.mimeType ?: "Unknown", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "KEEP",
                onClick = { 
                    if (isSelected) viewModel.toggleSelection(item.id)
                    onBack()
                },
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = if (isSelected) "SELECTED ✓" else "SELECT",
                onClick = { 
                    if (!isSelected) viewModel.toggleSelection(item.id)
                    onBack()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun VideoPreview(item: StorageItem) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    
    DisposableEffect(item.uri) {
        val exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(item.uri))
            prepare()
            playWhenReady = true
        }
        player = exoPlayer
        
        onDispose {
            exoPlayer.release()
            player = null
        }
    }
    
    AndroidView(
        factory = {
            PlayerView(context).apply {
                this.player = player
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
