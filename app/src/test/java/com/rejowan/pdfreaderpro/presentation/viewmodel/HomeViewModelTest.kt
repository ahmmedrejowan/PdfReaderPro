package com.rejowan.pdfreaderpro.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.FolderSortOption
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.domain.model.RecentFile
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.presentation.screens.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfFileRepository: PdfFileRepository
    private lateinit var recentRepository: RecentRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfFileRepository = mockk(relaxed = true)
        recentRepository = mockk(relaxed = true)
        favoriteRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)

        // Default mocks
        every { pdfFileRepository.getAllPdfFiles() } returns flowOf(emptyList())
        every { pdfFileRepository.getPdfFolders() } returns flowOf(emptyList())
        every { pdfFileRepository.searchPdfs(any()) } returns flowOf(emptyList())
        every { recentRepository.getRecentFiles() } returns flowOf(emptyList())
        every { favoriteRepository.getFavorites() } returns flowOf(emptyList())
        every { preferencesRepository.preferences } returns flowOf(AppPreferences())
        coEvery { pdfFileRepository.refreshPdfs() } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            pdfFileRepository = pdfFileRepository,
            recentRepository = recentRepository,
            favoriteRepository = favoriteRepository,
            preferencesRepository = preferencesRepository
        )
    }

    // region Test Data
    private fun createPdfFile(
        id: Long = 1L,
        name: String = "test.pdf",
        path: String = "/storage/test.pdf",
        size: Long = 1024L,
        dateModified: Long = System.currentTimeMillis()
    ) = PdfFile(
        id = id,
        name = name,
        path = path,
        uri = mockk<Uri>(),
        size = size,
        dateModified = dateModified,
        dateAdded = dateModified,
        parentFolder = "/storage"
    )

    private fun createRecentFile(
        id: Long = 1L,
        name: String = "recent.pdf",
        path: String = "/storage/recent.pdf"
    ) = RecentFile(
        id = id,
        name = name,
        path = path,
        size = 1024L,
        lastOpened = System.currentTimeMillis(),
        totalPages = 10,
        lastPage = 0
    )

    private fun createPdfFolder(
        path: String = "/storage/folder",
        name: String = "folder",
        pdfCount: Int = 5
    ) = PdfFolder(
        path = path,
        name = name,
        pdfCount = pdfCount
    )
    // endregion

    // region Initial State Tests
    @Test
    fun `initial state has correct default values`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ViewMode.LIST, viewModel.viewMode.value)
        assertEquals(SortOption.NAME_ASC, viewModel.sortOption.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(FolderSortOption.NAME_ASC, viewModel.folderSortOption.value)
        assertEquals("", viewModel.folderSearchQuery.value)
    }

    @Test
    fun `isLoading becomes false after initial load`() = runTest {
        viewModel = createViewModel()

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loads preferences on init`() = runTest {
        val customPrefs = AppPreferences(
            defaultViewMode = ViewMode.GRID,
            defaultSortOption = SortOption.DATE_DESC
        )
        every { preferencesRepository.preferences } returns flowOf(customPrefs)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(ViewMode.GRID, viewModel.viewMode.value)
        assertEquals(SortOption.DATE_DESC, viewModel.sortOption.value)
    }

    @Test
    fun `calls refreshPdfs on init`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coVerify { pdfFileRepository.refreshPdfs() }
    }
    // endregion

    // region allFiles Tests
    @Test
    fun `allFiles emits files from repository`() = runTest {
        val files = listOf(
            createPdfFile(name = "file1.pdf"),
            createPdfFile(name = "file2.pdf")
        )
        every { pdfFileRepository.getAllPdfFiles() } returns flowOf(files)

        viewModel = createViewModel()

        // Subscribe first (triggers SharingStarted.Lazily), then advance
        viewModel.allFiles.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `allFiles sorts by name ascending`() = runTest {
        val files = listOf(
            createPdfFile(name = "charlie.pdf"),
            createPdfFile(name = "alpha.pdf"),
            createPdfFile(name = "bravo.pdf")
        )
        every { pdfFileRepository.getAllPdfFiles() } returns flowOf(files)

        viewModel = createViewModel()

        viewModel.allFiles.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(3, result.size)
            assertEquals("alpha.pdf", result[0].name)
            assertEquals("bravo.pdf", result[1].name)
            assertEquals("charlie.pdf", result[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `allFiles sorts by name descending when set`() = runTest {
        val files = listOf(
            createPdfFile(name = "alpha.pdf"),
            createPdfFile(name = "charlie.pdf"),
            createPdfFile(name = "bravo.pdf")
        )
        every { pdfFileRepository.getAllPdfFiles() } returns flowOf(files)

        viewModel = createViewModel()

        // Subscribe first to trigger SharingStarted.Lazily
        viewModel.allFiles.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        // Set sort option and wait for flow to update
        viewModel.setSortOption(SortOption.NAME_DESC)
        advanceUntilIdle()

        // Check the sorted value
        val result = viewModel.allFiles.value
        assertEquals(3, result.size)
        assertEquals("charlie.pdf", result[0].name)
        assertEquals("bravo.pdf", result[1].name)
        assertEquals("alpha.pdf", result[2].name)
    }

    @Test
    fun `allFiles sorts by date descending when set`() = runTest {
        val now = System.currentTimeMillis()
        val files = listOf(
            createPdfFile(name = "old.pdf", dateModified = now - 2000),
            createPdfFile(name = "new.pdf", dateModified = now),
            createPdfFile(name = "middle.pdf", dateModified = now - 1000)
        )
        every { pdfFileRepository.getAllPdfFiles() } returns flowOf(files)

        viewModel = createViewModel()

        // Subscribe first to trigger SharingStarted.Lazily
        viewModel.allFiles.test {
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        // Set sort option and wait for flow to update
        viewModel.setSortOption(SortOption.DATE_DESC)
        advanceUntilIdle()

        // Check the sorted value
        val result = viewModel.allFiles.value
        assertEquals(3, result.size)
        assertEquals("new.pdf", result[0].name)
        assertEquals("middle.pdf", result[1].name)
        assertEquals("old.pdf", result[2].name)
    }

    @Test
    fun `allFiles sorts by size descending when set`() = runTest {
        val files = listOf(
            createPdfFile(name = "small.pdf", size = 1024),
            createPdfFile(name = "large.pdf", size = 10240),
            createPdfFile(name = "medium.pdf", size = 5120)
        )
        every { pdfFileRepository.getAllPdfFiles() } returns flowOf(files)

        viewModel = createViewModel()
        viewModel.setSortOption(SortOption.SIZE_DESC)

        viewModel.allFiles.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(3, result.size)
            assertEquals("large.pdf", result[0].name)
            assertEquals("medium.pdf", result[1].name)
            assertEquals("small.pdf", result[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region recentFiles Tests
    @Test
    fun `recentFiles emits files from repository`() = runTest {
        val recents = listOf(
            createRecentFile(name = "recent1.pdf"),
            createRecentFile(name = "recent2.pdf")
        )
        every { recentRepository.getRecentFiles() } returns flowOf(recents)

        viewModel = createViewModel()

        viewModel.recentFiles.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region favoriteFiles Tests
    @Test
    fun `favoriteFiles emits files from repository`() = runTest {
        val favorites = listOf(
            createPdfFile(name = "fav1.pdf"),
            createPdfFile(name = "fav2.pdf")
        )
        every { favoriteRepository.getFavorites() } returns flowOf(favorites)

        viewModel = createViewModel()

        viewModel.favoriteFiles.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(2, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region folders Tests
    @Test
    fun `folders emits sorted folders`() = runTest {
        val folders = listOf(
            createPdfFolder(name = "charlie"),
            createPdfFolder(name = "alpha"),
            createPdfFolder(name = "bravo")
        )
        every { pdfFileRepository.getPdfFolders() } returns flowOf(folders)

        viewModel = createViewModel()

        viewModel.folders.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(3, result.size)
            assertEquals("alpha", result[0].name)
            assertEquals("bravo", result[1].name)
            assertEquals("charlie", result[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `folders filters by search query`() = runTest {
        val folders = listOf(
            createPdfFolder(name = "Documents"),
            createPdfFolder(name = "Downloads"),
            createPdfFolder(name = "Pictures")
        )
        every { pdfFileRepository.getPdfFolders() } returns flowOf(folders)

        viewModel = createViewModel()
        viewModel.setFolderSearchQuery("Doc")

        viewModel.folders.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(1, result.size)
            assertEquals("Documents", result[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `folders sorts by count descending when set`() = runTest {
        val folders = listOf(
            createPdfFolder(name = "few", pdfCount = 2),
            createPdfFolder(name = "many", pdfCount = 10),
            createPdfFolder(name = "some", pdfCount = 5)
        )
        every { pdfFileRepository.getPdfFolders() } returns flowOf(folders)

        viewModel = createViewModel()
        viewModel.setFolderSortOption(FolderSortOption.COUNT_DESC)

        viewModel.folders.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(3, result.size)
            assertEquals("many", result[0].name)
            assertEquals("some", result[1].name)
            assertEquals("few", result[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setViewMode Tests
    @Test
    fun `setViewMode updates viewMode state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setViewMode(ViewMode.GRID)
        advanceUntilIdle()

        assertEquals(ViewMode.GRID, viewModel.viewMode.value)
    }

    @Test
    fun `setViewMode persists preference`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setViewMode(ViewMode.GRID)
        advanceUntilIdle()

        coVerify { preferencesRepository.setDefaultViewMode(ViewMode.GRID) }
    }
    // endregion

    // region setSortOption Tests
    @Test
    fun `setSortOption updates sortOption state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.DATE_DESC)
        advanceUntilIdle()

        assertEquals(SortOption.DATE_DESC, viewModel.sortOption.value)
    }

    @Test
    fun `setSortOption persists preference`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.SIZE_ASC)
        advanceUntilIdle()

        coVerify { preferencesRepository.setDefaultSortOption(SortOption.SIZE_ASC) }
    }
    // endregion

    // region setSearchQuery Tests
    @Test
    fun `setSearchQuery updates searchQuery state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSearchQuery("test query")

        assertEquals("test query", viewModel.searchQuery.value)
    }
    // endregion

    // region refresh Tests
    @Test
    fun `refresh sets isLoading false after completion`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // After initial load, isLoading should be false
        assertFalse(viewModel.isLoading.value)

        // Call refresh and wait for completion
        viewModel.refresh()
        advanceUntilIdle()

        // After refresh completes, isLoading should be false again
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `refresh calls repository refreshPdfs`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        // Called once on init and once on refresh
        coVerify(atLeast = 2) { pdfFileRepository.refreshPdfs() }
    }
    // endregion

    // region toggleFavorite Tests
    @Test
    fun `toggleFavorite calls repository toggleFavorite`() = runTest {
        val pdfFile = createPdfFile()
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite(pdfFile)
        advanceUntilIdle()

        coVerify { favoriteRepository.toggleFavorite(pdfFile) }
    }
    // endregion

    // region removeFromRecent Tests
    @Test
    fun `removeFromRecent calls repository removeRecent`() = runTest {
        val path = "/storage/test.pdf"
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.removeFromRecent(path)
        advanceUntilIdle()

        coVerify { recentRepository.removeRecent(path) }
    }
    // endregion

    // region clearAllRecent Tests
    @Test
    fun `clearAllRecent calls repository clearAllRecent`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearAllRecent()
        advanceUntilIdle()

        coVerify { recentRepository.clearAllRecent() }
    }
    // endregion

    // region clearAllFavorites Tests
    @Test
    fun `clearAllFavorites calls repository clearAllFavorites`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearAllFavorites()
        advanceUntilIdle()

        coVerify { favoriteRepository.clearAllFavorites() }
    }
    // endregion

    // region isFavorite Tests
    @Test
    fun `isFavorite returns value from repository`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { favoriteRepository.isFavorite(path) } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.isFavorite(path)

        assertTrue(result)
    }
    // endregion

    // region Error Handling Tests
    @Test
    fun `handles error in loadInitialData gracefully`() = runTest {
        coEvery { pdfFileRepository.refreshPdfs() } throws Exception("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        // Should complete without crashing
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `handles error in refresh gracefully`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { pdfFileRepository.refreshPdfs() } throws Exception("Network error")

        viewModel.refresh()
        advanceUntilIdle()

        // Should complete without crashing
        assertFalse(viewModel.isLoading.value)
    }
    // endregion
}
