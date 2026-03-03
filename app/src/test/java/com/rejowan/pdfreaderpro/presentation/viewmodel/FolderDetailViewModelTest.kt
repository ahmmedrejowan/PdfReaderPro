package com.rejowan.pdfreaderpro.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.SortOption
import com.rejowan.pdfreaderpro.domain.model.ViewMode
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.presentation.screens.folder.FolderDetailViewModel
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
class FolderDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfFileRepository: PdfFileRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: FolderDetailViewModel

    private val mockUri: Uri = mockk(relaxed = true)

    private val testPdfFiles = listOf(
        PdfFile(
            id = 1L,
            name = "Alpha.pdf",
            path = "/storage/docs/Alpha.pdf",
            uri = mockUri,
            size = 1000L,
            dateModified = 1000L,
            dateAdded = 900L,
            parentFolder = "/storage/docs"
        ),
        PdfFile(
            id = 2L,
            name = "Beta.pdf",
            path = "/storage/docs/Beta.pdf",
            uri = mockUri,
            size = 2000L,
            dateModified = 2000L,
            dateAdded = 1900L,
            parentFolder = "/storage/docs"
        ),
        PdfFile(
            id = 3L,
            name = "Charlie.pdf",
            path = "/storage/docs/Charlie.pdf",
            uri = mockUri,
            size = 500L,
            dateModified = 1500L,
            dateAdded = 1400L,
            parentFolder = "/storage/docs"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfFileRepository = mockk(relaxed = true)
        favoriteRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)

        every { preferencesRepository.preferences } returns flowOf(AppPreferences())
        every { pdfFileRepository.getPdfsByFolder(any()) } returns flowOf(testPdfFiles)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): FolderDetailViewModel {
        return FolderDetailViewModel(
            pdfFileRepository = pdfFileRepository,
            favoriteRepository = favoriteRepository,
            preferencesRepository = preferencesRepository
        )
    }

    // region Initial State Tests
    @Test
    fun `initial state has empty files list`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertTrue(files.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has isLoading true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.isLoading.test {
            val isLoading = awaitItem()
            assertTrue(isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state loads viewMode from preferences`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.viewMode.test {
            val mode = awaitItem()
            assertEquals(ViewMode.LIST, mode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state loads sortOption from preferences`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sortOption.test {
            val option = awaitItem()
            assertEquals(SortOption.NAME_ASC, option)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region loadFilesForFolder Tests
    @Test
    fun `loadFilesForFolder loads files from repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals(3, files.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadFilesForFolder sets isLoading to false after loading`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.isLoading.test {
            val isLoading = awaitItem()
            assertFalse(isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadFilesForFolder sorts files by name ascending by default`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals("Alpha.pdf", files[0].name)
            assertEquals("Beta.pdf", files[1].name)
            assertEquals("Charlie.pdf", files[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setViewMode Tests
    @Test
    fun `setViewMode updates to GRID`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setViewMode(ViewMode.GRID)
        advanceUntilIdle()

        viewModel.viewMode.test {
            val mode = awaitItem()
            assertEquals(ViewMode.GRID, mode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setViewMode updates to LIST`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setViewMode(ViewMode.LIST)
        advanceUntilIdle()

        viewModel.viewMode.test {
            val mode = awaitItem()
            assertEquals(ViewMode.LIST, mode)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setSortOption Tests
    @Test
    fun `setSortOption updates to NAME_DESC and resorts files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.NAME_DESC)
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals("Charlie.pdf", files[0].name)
            assertEquals("Beta.pdf", files[1].name)
            assertEquals("Alpha.pdf", files[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSortOption updates to DATE_DESC and resorts files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.DATE_DESC)
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals("Beta.pdf", files[0].name) // dateModified 2000
            assertEquals("Charlie.pdf", files[1].name) // dateModified 1500
            assertEquals("Alpha.pdf", files[2].name) // dateModified 1000
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSortOption updates to DATE_ASC and resorts files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.DATE_ASC)
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals("Alpha.pdf", files[0].name) // dateModified 1000
            assertEquals("Charlie.pdf", files[1].name) // dateModified 1500
            assertEquals("Beta.pdf", files[2].name) // dateModified 2000
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSortOption updates to SIZE_DESC and resorts files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.SIZE_DESC)
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals("Beta.pdf", files[0].name) // size 2000
            assertEquals("Alpha.pdf", files[1].name) // size 1000
            assertEquals("Charlie.pdf", files[2].name) // size 500
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSortOption updates to SIZE_ASC and resorts files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadFilesForFolder("/storage/docs")
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.SIZE_ASC)
        advanceUntilIdle()

        viewModel.files.test {
            val files = awaitItem()
            assertEquals("Charlie.pdf", files[0].name) // size 500
            assertEquals("Alpha.pdf", files[1].name) // size 1000
            assertEquals("Beta.pdf", files[2].name) // size 2000
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSortOption updates sortOption state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.DATE_DESC)
        advanceUntilIdle()

        viewModel.sortOption.test {
            val option = awaitItem()
            assertEquals(SortOption.DATE_DESC, option)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region toggleFavorite Tests
    @Test
    fun `toggleFavorite calls repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val pdfFile = testPdfFiles[0]
        viewModel.toggleFavorite(pdfFile)
        advanceUntilIdle()

        coVerify { favoriteRepository.toggleFavorite(pdfFile) }
    }
    // endregion

    // region isFavorite Tests
    @Test
    fun `isFavorite returns true when file is favorite`() = runTest {
        coEvery { favoriteRepository.isFavorite("/storage/docs/Alpha.pdf") } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.isFavorite("/storage/docs/Alpha.pdf")
        assertTrue(result)
    }

    @Test
    fun `isFavorite returns false when file is not favorite`() = runTest {
        coEvery { favoriteRepository.isFavorite("/storage/docs/Alpha.pdf") } returns false

        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.isFavorite("/storage/docs/Alpha.pdf")
        assertFalse(result)
    }
    // endregion

    // region Preferences Integration Tests
    @Test
    fun `viewModel loads GRID mode from preferences`() = runTest {
        every { preferencesRepository.preferences } returns flowOf(
            AppPreferences(defaultViewMode = ViewMode.GRID)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.viewMode.test {
            val mode = awaitItem()
            assertEquals(ViewMode.GRID, mode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `viewModel loads DATE_DESC sort from preferences`() = runTest {
        every { preferencesRepository.preferences } returns flowOf(
            AppPreferences(defaultSortOption = SortOption.DATE_DESC)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sortOption.test {
            val option = awaitItem()
            assertEquals(SortOption.DATE_DESC, option)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion
}
