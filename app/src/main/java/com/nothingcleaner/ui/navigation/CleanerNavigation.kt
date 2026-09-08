package com.nothingcleaner.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nothingcleaner.core.model.StorageCategory
import com.nothingcleaner.ui.screens.*
import com.nothingcleaner.viewmodel.StorageViewModel

sealed class Screen {
    object Dashboard : Screen()
    object Scanner : Screen()
    object Overview : Screen()
    data class CategoryReview(val category: StorageCategory) : Screen()
    object CleanReview : Screen()
    data class FilePreview(val itemId: Long, val fromCategory: StorageCategory?) : Screen()
}

@Composable
fun CleanerNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val viewModel: StorageViewModel = viewModel()

    when (val screen = currentScreen) {
        is Screen.Dashboard -> DashboardScreen(
            viewModel = viewModel,
            onAnalyze = { currentScreen = Screen.Scanner }
        )
        is Screen.Scanner -> ScannerScreen(
            viewModel = viewModel,
            onScanComplete = { currentScreen = Screen.Overview },
            onCancel = { currentScreen = Screen.Dashboard }
        )
        is Screen.Overview -> OverviewScreen(
            viewModel = viewModel,
            onCategorySelected = { cat -> currentScreen = Screen.CategoryReview(cat) },
            onReviewClean = { currentScreen = Screen.CleanReview }
        )
        is Screen.CategoryReview -> CategoryReviewScreen(
            viewModel = viewModel,
            category = screen.category,
            onBack = { currentScreen = Screen.Overview },
            onReviewSelection = { currentScreen = Screen.CleanReview },
            onPreview = { id -> currentScreen = Screen.FilePreview(id, screen.category) }
        )
        is Screen.CleanReview -> CleanReviewScreen(
            viewModel = viewModel,
            onCancel = { currentScreen = Screen.Overview },
            onCleanComplete = { 
                // We'll hook this up to MainActivity's ActivityResultLauncher later
            },
            onPreview = { id -> currentScreen = Screen.FilePreview(id, null) }
        )
        is Screen.FilePreview -> FilePreviewScreen(
            viewModel = viewModel,
            itemId = screen.itemId,
            onBack = { 
                currentScreen = if (screen.fromCategory != null) {
                    Screen.CategoryReview(screen.fromCategory)
                } else {
                    Screen.CleanReview
                }
            }
        )
    }
}
