package com.rejowan.pdfreaderpro.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

/**
 * Main entry point for navigation.
 * Creates its own NavController and hosts the NavGraph.
 */
@Composable
fun PdfReaderNavGraph() {
    val navController = rememberNavController()
    PdfReaderNavHost(navController = navController)
}

/**
 * Main navigation graph for the app.
 * Uses type-safe navigation with Kotlin Serialization.
 */
@Composable
fun PdfReaderNavHost(
    navController: NavHostController,
    startDestination: Any = Home,
    onShowSnackbar: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable<Splash> {
            // TODO: SplashScreen(navController)
            // Placeholder - will be implemented in Phase 1
        }

        // Onboarding Screen
        composable<Onboarding> {
            // TODO: OnboardingScreen(navController)
            // Placeholder - will be implemented in Phase 1
        }

        // Home Screen
        composable<Home> {
            // Placeholder - will be replaced with HomeScreen in Phase 1
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("PDF Reader Pro v2\n\nHome Screen Coming Soon...")
            }
        }

        // Search Screen
        composable<Search> { backStackEntry ->
            val search: Search = backStackEntry.toRoute()
            // TODO: SearchScreen(navController, search.query)
        }

        // Settings Screen
        composable<Settings> {
            // TODO: SettingsScreen(navController)
        }

        // PDF Reader Screen
        composable<Reader> { backStackEntry ->
            val reader: Reader = backStackEntry.toRoute()
            // TODO: ReaderScreen(navController, reader.path, reader.page)
        }

        // Tools Screen
        composable<Tools> {
            // TODO: ToolsScreen(navController)
        }

        // Tool: Merge PDFs
        composable<ToolMerge> { backStackEntry ->
            val toolMerge: ToolMerge = backStackEntry.toRoute()
            // TODO: MergePdfsScreen(navController, toolMerge.selectedFiles)
        }

        // Tool: Split PDF
        composable<ToolSplit> { backStackEntry ->
            val toolSplit: ToolSplit = backStackEntry.toRoute()
            // TODO: SplitPdfScreen(navController, toolSplit.filePath)
        }

        // Tool: Compress PDF
        composable<ToolCompress> { backStackEntry ->
            val toolCompress: ToolCompress = backStackEntry.toRoute()
            // TODO: CompressPdfScreen(navController, toolCompress.filePath)
        }

        // Tool Result Screen
        composable<ToolResult> { backStackEntry ->
            val toolResult: ToolResult = backStackEntry.toRoute()
            // TODO: ToolResultScreen(navController, toolResult)
        }
    }
}

// ============================================================================
// Navigation Extensions
// ============================================================================

/**
 * Navigate to PDF reader with a file path.
 */
fun NavController.navigateToReader(path: String, page: Int = 0, fromIntent: Boolean = false) {
    navigate(Reader(path = path, page = page, fromIntent = fromIntent))
}

/**
 * Navigate to search with optional initial query.
 */
fun NavController.navigateToSearch(query: String = "") {
    navigate(Search(query = query))
}

/**
 * Navigate to home, clearing the back stack.
 */
fun NavController.navigateToHome() {
    navigate(Home) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigate to onboarding, clearing the back stack.
 */
fun NavController.navigateToOnboarding() {
    navigate(Onboarding) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigate to tool merge screen.
 */
fun NavController.navigateToMergeTool(selectedFiles: List<String> = emptyList()) {
    navigate(ToolMerge(selectedFiles = selectedFiles))
}

/**
 * Navigate to tool split screen.
 */
fun NavController.navigateToSplitTool(filePath: String) {
    navigate(ToolSplit(filePath = filePath))
}

/**
 * Navigate to tool compress screen.
 */
fun NavController.navigateToCompressTool(filePath: String) {
    navigate(ToolCompress(filePath = filePath))
}
