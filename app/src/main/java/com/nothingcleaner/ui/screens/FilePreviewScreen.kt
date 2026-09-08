package com.nothingcleaner.ui.screens

import androidx.annotation.OptIn
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nothingcleaner.ui.CleanerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun FilePreviewScreen(viewModel: CleanerViewModel, itemId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scannedItems by viewModel.scannedItems.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    
    val item = scannedItems.find { it.id == itemId } ?: return
    val isSelected = selectedItemIds.contains(item.id)
    
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val dateStr = if (item.dateAdded > 0) "Added " + dateFormat.format(Date(item.dateAdded * 1000L)) else ""
    val sizeStr = String.format(Locale.US, "%.1f MB", item.sizeBytes / (1024 * 1024.0))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Text(
            text = "← BACK",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = item.name.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1
        )
        
        Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                item.mimeType.startsWith("image/") -> {
                    AsyncImage(
                        model = item.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                item.mimeType.startsWith("video/") -> {
                    var player by remember { mutableStateOf<ExoPlayer?>(null) }
                    
                    DisposableEffect(Unit) {
                        val exoPlayer = ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(item.uri))
                            prepare()
                            playWhenReady = false
                        }
                        player = exoPlayer
                        onDispose {
                            exoPlayer.release()
                        }
                    }
                    
                    player?.let {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    this.player = it
                                    setShowNextButton(false)
                                    setShowPreviousButton(false)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[ FILE ]",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "TYPE: ${item.mimeType}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))
        
        Text(text = item.name, style = MaterialTheme.typography.bodyLarge, color = Color.White, maxLines = 1)
        Text(text = "$sizeStr • $dateStr", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (!isSelected) "[ KEEP ]" else "  KEEP  ",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (!isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (!isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.clickable { if (isSelected) viewModel.toggleSelection(item.id) }.padding(16.dp)
            )
            Text(
                text = if (isSelected) "[ SELECT ]" else "  SELECT  ",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Red else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.clickable { if (!isSelected) viewModel.toggleSelection(item.id) }.padding(16.dp)
            )
        }
    }
}
