package com.nothingcleaner.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothingcleaner.scanner.FileCategory
import com.nothingcleaner.ui.CleanerViewModel
import com.nothingcleaner.ui.screens.*

sealed class Screen {
    object Dashboard : Screen()
    object Scanner : Screen()
    object Overview : Screen()
    data class CategoryReview(val category: FileCategory) : Screen()
    object CleanReview : Screen()
}

@Composable
fun CleanerNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val viewModel: CleanerViewModel = viewModel()

    when (val screen = currentScreen) {
        is Screen.Dashboard -> DashboardScreen(
            viewModel = viewModel,
            onAnalyze = { currentScreen = Screen.Scanner }
        )
        is Screen.Scanner -> ScannerScreen(
            viewModel = viewModel,
            onScanComplete = { currentScreen = Screen.Overview }
        )
        is Screen.Overview -> OverviewScreen(
            viewModel = viewModel,
            onCategorySelected = { cat -> currentScreen = Screen.CategoryReview(cat) },
            onReviewClean = { currentScreen = Screen.CleanReview }
        )
        is Screen.CategoryReview -> CategoryReviewScreen(
            viewModel = viewModel,
            category = screen.category,
            onBack = { currentScreen = Screen.Overview }
        )
        is Screen.CleanReview -> CleanReviewScreen(
            viewModel = viewModel,
            onCancel = { currentScreen = Screen.Overview },
            onCleanComplete = { 
                viewModel.clearScannedItems()
                currentScreen = Screen.Dashboard 
            }
        )
    }
}
