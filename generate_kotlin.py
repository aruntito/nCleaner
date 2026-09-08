import os

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        f.write(content.strip() + '\n')

write_file('app/src/main/java/com/nothingcleaner/util/StorageUtil.kt', """
package com.nothingcleaner.util

import android.os.Environment
import android.os.StatFs

object StorageUtil {
    fun getStorageStats(): StorageStats {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - availableBytes
        
        return StorageStats(totalBytes, usedBytes, availableBytes)
    }
}

data class StorageStats(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
)
""")

write_file('app/src/main/java/com/nothingcleaner/scanner/FileCategory.kt', """
package com.nothingcleaner.scanner

enum class FileCategory {
    DOWNLOADS,
    LARGE_FILES,
    TEMPORARY_FILES,
    DUPLICATE_CANDIDATES
}
""")

write_file('app/src/main/java/com/nothingcleaner/scanner/ScannerItem.kt', """
package com.nothingcleaner.scanner

import android.net.Uri

data class ScannerItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val sizeBytes: Long,
    val category: FileCategory,
    val path: String
)
""")

write_file('app/src/main/java/com/nothingcleaner/scanner/StorageScanner.kt', """
package com.nothingcleaner.scanner

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageScanner(private val context: Context) {
    suspend fun scanDownloads(): List<ScannerItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<ScannerItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA
        )

        val selection = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.DATA + " LIKE '%/Download/%'"
        } else null

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val uri = Uri.withAppendedPath(collection, id.toString())

                items.add(ScannerItem(id.toString(), name, uri, size, FileCategory.DOWNLOADS, data))
            }
        }
        items
    }
    
    suspend fun scanLargeFiles(thresholdBytes: Long = 100 * 1024 * 1024): List<ScannerItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<ScannerItem>()
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATA
        )
        val selection = "${MediaStore.MediaColumns.SIZE} >= ?"
        val selectionArgs = arrayOf(thresholdBytes.toString())

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.MediaColumns.SIZE} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val data = cursor.getString(dataColumn) ?: ""
                val uri = Uri.withAppendedPath(collection, id.toString())

                items.add(ScannerItem(id.toString(), name, uri, size, FileCategory.LARGE_FILES, data))
            }
        }
        items
    }
}
""")

write_file('app/src/main/java/com/nothingcleaner/storage/CleanerService.kt', """
package com.nothingcleaner.storage

import android.app.RecoverableSecurityException
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.nothingcleaner.scanner.ScannerItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CleanerService(private val context: Context) {
    suspend fun deleteItems(items: List<ScannerItem>): DeleteResult = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var failedCount = 0
        var freedBytes = 0L

        for (item in items) {
            try {
                val deleted = context.contentResolver.delete(item.uri, null, null)
                if (deleted > 0) {
                    deletedCount++
                    freedBytes += item.sizeBytes
                } else {
                    failedCount++
                }
            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverable = e as? RecoverableSecurityException
                    if (recoverable != null) {
                        // In a real app we might request permission for each, but we'll count as failed for bulk
                        failedCount++
                    } else {
                        failedCount++
                    }
                } else {
                    failedCount++
                }
            } catch (e: Exception) {
                failedCount++
            }
        }
        DeleteResult(deletedCount, failedCount, freedBytes)
    }
}

data class DeleteResult(
    val deletedCount: Int,
    val failedCount: Int,
    val freedBytes: Long
)
""")

write_file('app/src/main/java/com/nothingcleaner/ui/screens/DashboardScreen.kt', """
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
""")

write_file('app/src/main/java/com/nothingcleaner/ui/screens/ScannerScreen.kt', """
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
""")

write_file('app/src/main/java/com/nothingcleaner/ui/screens/ResultsScreen.kt', """
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
""")

write_file('app/src/main/java/com/nothingcleaner/ui/navigation/CleanerNavigation.kt', """
package com.nothingcleaner.ui.navigation

import androidx.compose.runtime.*
import com.nothingcleaner.scanner.ScannerItem
import com.nothingcleaner.ui.screens.DashboardScreen
import com.nothingcleaner.ui.screens.ResultsScreen
import com.nothingcleaner.ui.screens.ScannerScreen

enum class Screen {
    DASHBOARD,
    SCANNER,
    RESULTS
}

@Composable
fun CleanerNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var scannedItems by remember { mutableStateOf<List<ScannerItem>>(emptyList()) }

    when (currentScreen) {
        Screen.DASHBOARD -> DashboardScreen(
            onStartScan = { currentScreen = Screen.SCANNER }
        )
        Screen.SCANNER -> ScannerScreen(
            onScanComplete = { items ->
                scannedItems = items
                currentScreen = Screen.RESULTS
            }
        )
        Screen.RESULTS -> ResultsScreen(
            items = scannedItems,
            onDone = { currentScreen = Screen.DASHBOARD }
        )
    }
}
""")

write_file('app/src/main/java/com/nothingcleaner/MainActivity.kt', """
package com.nothingcleaner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.nothingcleaner.ui.navigation.CleanerNavigation
import com.nothingcleaner.ui.theme.NothingCleanerTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissionsIfNecessary()

        setContent {
            NothingCleanerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CleanerNavigation()
                }
            }
        }
    }

    private fun requestPermissionsIfNecessary() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}
""")
