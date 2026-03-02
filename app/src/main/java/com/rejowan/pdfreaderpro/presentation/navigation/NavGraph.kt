package com.rejowan.pdfreaderpro.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rejowan.pdfreaderpro.presentation.screens.folder.FolderDetailScreen
import com.rejowan.pdfreaderpro.presentation.screens.home.HomeScreen
import com.rejowan.pdfreaderpro.presentation.screens.onboarding.OnboardingScreen
import com.rejowan.pdfreaderpro.presentation.screens.onboarding.OnboardingViewModel
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderScreen
import com.rejowan.pdfreaderpro.presentation.screens.search.SearchScreen
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.lock.LockScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.reorder.ReorderScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.unlock.UnlockScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.removepages.RemovePagesScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.RotateScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkScreen
import com.rejowan.pdfreaderpro.presentation.screens.tools.split.SplitScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Main entry point for navigation.
 * Creates its own NavController and hosts the NavGraph.
 */
@Composable
fun PdfReaderNavGraph(startDestination: Any = Home) {
    val navController = rememberNavController()
    PdfReaderNavHost(navController = navController, startDestination = startDestination)
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
        // Onboarding Screen
        composable<Onboarding> {
            val viewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(
                navController = navController,
                onOnboardingComplete = { viewModel.markOnboardingComplete() }
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
            ReaderScreen(
                navController = navController,
                path = reader.path,
                initialPage = reader.page
            )
        }

        // Tools Screen
        composable<Tools> {
            // TODO: ToolsScreen(navController)
        }

        // Tool: Merge PDFs
        composable<ToolMerge> { backStackEntry ->
            val toolMerge: ToolMerge = backStackEntry.toRoute()
            MergeScreen(
                navController = navController,
                initialFiles = toolMerge.selectedFiles
            )
        }

        // Tool: Split PDF
        composable<ToolSplit> { backStackEntry ->
            val toolSplit: ToolSplit = backStackEntry.toRoute()
            SplitScreen(
                navController = navController,
                initialFilePath = toolSplit.filePath
            )
        }

        // Tool: Compress PDF
        composable<ToolCompress> { backStackEntry ->
            val toolCompress: ToolCompress = backStackEntry.toRoute()
            CompressScreen(
                navController = navController,
                initialFilePath = toolCompress.filePath
            )
        }

        // Tool: Rotate Pages
        composable<ToolRotate> { backStackEntry ->
            val toolRotate: ToolRotate = backStackEntry.toRoute()
            RotateScreen(
                navController = navController,
                initialFilePath = toolRotate.filePath
            )
        }

        // Tool: Reorder Pages
        composable<ToolReorder> { backStackEntry ->
            val toolReorder: ToolReorder = backStackEntry.toRoute()
            ReorderScreen(
                navController = navController,
                initialFilePath = toolReorder.filePath
            )
        }

        // Tool: Lock PDF
        composable<ToolLock> { backStackEntry ->
            val toolLock: ToolLock = backStackEntry.toRoute()
            LockScreen(
                navController = navController,
                initialFilePath = toolLock.filePath
            )
        }

        // Tool: Unlock PDF
        composable<ToolUnlock> { backStackEntry ->
            val toolUnlock: ToolUnlock = backStackEntry.toRoute()
            UnlockScreen(
                navController = navController,
                initialFilePath = toolUnlock.filePath
            )
        }

        // Tool: Remove Pages
        composable<ToolRemovePages> { backStackEntry ->
            val toolRemovePages: ToolRemovePages = backStackEntry.toRoute()
            RemovePagesScreen(
                navController = navController,
                initialFilePath = toolRemovePages.filePath
            )
        }

        // Tool: Watermark
        composable<ToolWatermark> { backStackEntry ->
            val toolWatermark: ToolWatermark = backStackEntry.toRoute()
            WatermarkScreen(
                navController = navController,
                initialFilePath = toolWatermark.filePath
            )
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

/**
 * Navigate to tool rotate screen.
 */
fun NavController.navigateToRotateTool(filePath: String) {
    navigate(ToolRotate(filePath = filePath))
}

/**
 * Navigate to tool reorder screen.
 */
fun NavController.navigateToReorderTool(filePath: String) {
    navigate(ToolReorder(filePath = filePath))
}

/**
 * Navigate to tool lock screen.
 */
fun NavController.navigateToLockTool(filePath: String) {
    navigate(ToolLock(filePath = filePath))
}

/**
 * Navigate to tool unlock screen.
 */
fun NavController.navigateToUnlockTool(filePath: String) {
    navigate(ToolUnlock(filePath = filePath))
}

/**
 * Navigate to tool remove pages screen.
 */
fun NavController.navigateToRemovePagesTool(filePath: String) {
    navigate(ToolRemovePages(filePath = filePath))
}

/**
 * Navigate to tool watermark screen.
 */
fun NavController.navigateToWatermarkTool(filePath: String) {
    navigate(ToolWatermark(filePath = filePath))
}
