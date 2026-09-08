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
