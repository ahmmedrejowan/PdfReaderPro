package com.rejowan.pdfreaderpro.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rejowan.pdfreaderpro.presentation.screens.folder.FolderDetailScreen
import com.rejowan.pdfreaderpro.presentation.screens.home.HomeScreen
import com.rejowan.pdfreaderpro.presentation.screens.onboarding.OnboardingScreen
import com.rejowan.pdfreaderpro.presentation.screens.search.SearchScreen
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsScreen
import com.rejowan.pdfreaderpro.presentation.screens.splash.SplashScreen
import com.rejowan.pdfreaderpro.presentation.screens.splash.SplashViewModel
import org.koin.androidx.compose.koinViewModel

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
    startDestination: Any = Splash,
    onShowSnackbar: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable<Splash> {
            val viewModel: SplashViewModel = koinViewModel()
            val state by viewModel.state.collectAsState()

            if (!state.isLoading) {
                SplashScreen(
                    navController = navController,
                    hasCompletedOnboarding = state.hasCompletedOnboarding,
                    hasStoragePermission = state.hasStoragePermission
                )
            }
        }

        // Onboarding Screen
        composable<Onboarding> {
            val splashViewModel: SplashViewModel = koinViewModel()
            OnboardingScreen(
                navController = navController,
                onOnboardingComplete = { splashViewModel.markOnboardingComplete() }
            )
        }

        // Home Screen
        composable<Home> {
            HomeScreen(navController = navController)
        }

        // Search Screen
        composable<Search> { backStackEntry ->
            val search: Search = backStackEntry.toRoute()
            SearchScreen(
                navController = navController,
                initialQuery = search.query
            )
        }

        // Settings Screen
        composable<Settings> {
            SettingsScreen(navController = navController)
        }

        // Folder Detail Screen
        composable<FolderDetail> { backStackEntry ->
            val folderDetail: FolderDetail = backStackEntry.toRoute()
            FolderDetailScreen(
                navController = navController,
                folderPath = folderDetail.folderPath,
                folderName = folderDetail.folderName
            )
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
 * Navigate to folder detail screen.
 */
fun NavController.navigateToFolderDetail(folderPath: String, folderName: String) {
    navigate(FolderDetail(folderPath = folderPath, folderName = folderName))
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
