package com.rejowan.pdfreaderpro.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.PasswordStorage
import com.rejowan.pdfreaderpro.data.local.database.entity.AnnotationEntity
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.PdfQuad
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.TappedHighlight
import com.rejowan.pdfreaderpro.presentation.components.pdf.model.TextSelection
import com.rejowan.pdfreaderpro.presentation.screens.reader.HighlightColors
import kotlinx.coroutines.flow.MutableStateFlow
import com.rejowan.pdfreaderpro.data.local.database.dao.FilePreferenceDao
import com.rejowan.pdfreaderpro.data.local.database.entity.FilePreferenceEntity
import com.rejowan.pdfreaderpro.data.local.database.dao.AnnotationDao
import com.rejowan.pdfreaderpro.data.local.database.dao.BookmarkDao
import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import com.rejowan.pdfreaderpro.domain.model.AppPreferences
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderAction
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderEvent
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderViewModel
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReadingTheme
import com.rejowan.pdfreaderpro.presentation.screens.reader.ScrollMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var recentRepository: RecentRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var annotationDao: AnnotationDao
    private lateinit var filePreferenceDao: FilePreferenceDao
    private lateinit var applicationContext: Application
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var passwordStorage: PasswordStorage
    private lateinit var viewModel: ReaderViewModel

    private val testPdfPath = "/storage/test.pdf"
    private val mockUri: Uri = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        recentRepository = mockk(relaxed = true)
        favoriteRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        bookmarkDao = mockk(relaxed = true)
        annotationDao = mockk(relaxed = true)
        filePreferenceDao = mockk(relaxed = true)
        applicationContext = mockk(relaxed = true)
        passwordStorage = mockk(relaxed = true)

        savedStateHandle = SavedStateHandle(mapOf("path" to testPdfPath, "initialPage" to 0))

        // Mock Uri.fromFile static method
        mockkStatic(Uri::class)
        every { Uri.fromFile(any()) } returns mockUri

        // Default mocks
        every { preferencesRepository.preferences } returns flowOf(AppPreferences())
        every { bookmarkDao.getBookmarksForPdf(any()) } returns flowOf(emptyList())
        every { annotationDao.getHighlightsForPdf(any()) } returns flowOf(emptyList())
        every { filePreferenceDao.observe(any()) } returns flowOf(null)
        coEvery { filePreferenceDao.get(any()) } returns null
        coEvery { favoriteRepository.isFavorite(any()) } returns false
        coEvery { recentRepository.getLastPage(any()) } returns null
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }

    private fun createViewModel(): ReaderViewModel {
        return ReaderViewModel(
            recentRepository = recentRepository,
            favoriteRepository = favoriteRepository,
            preferencesRepository = preferencesRepository,
            bookmarkDao = bookmarkDao,
            annotationDao = annotationDao,
            filePreferenceDao = filePreferenceDao,
            applicationContext = applicationContext,
            savedStateHandle = savedStateHandle,
            passwordStorage = passwordStorage
        )
    }

    // region Test Data
    private fun createBookmark(
        id: Long = 1L,
        pdfPath: String = testPdfPath,
        pageNumber: Int = 0,
        title: String = "Bookmark"
    ) = BookmarkEntity(
        id = id,
        pdfPath = pdfPath,
        pageNumber = pageNumber,
        title = title
    )
    // endregion

    // region Initial State Tests
    @Test
    fun `initial state has correct document path`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(testPdfPath, viewModel.pdfPath)
        assertEquals(testPdfPath, viewModel.state.value.documentPath)
    }

    @Test
    fun `initial state extracts document title from path`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("test", viewModel.state.value.documentTitle)
    }

    @Test
    fun `initial state loads preferences`() = runTest {
        val customPrefs = AppPreferences(
            readerBrightness = 0.8f,
            readerTheme = com.rejowan.pdfreaderpro.domain.model.ReadingTheme.DARK
        )
        every { preferencesRepository.preferences } returns flowOf(customPrefs)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0.8f, viewModel.state.value.brightness)
        assertEquals(ReadingTheme.DARK, viewModel.state.value.readingTheme)
    }

    @Test
    fun `initial state loads favorite status`() = runTest {
        coEvery { favoriteRepository.isFavorite(testPdfPath) } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = createViewModel()

        assertTrue(viewModel.state.value.isLoading)
    }
    // endregion

    // region Bookmark Tests
    @Test
    fun `bookmarks flow updates state`() = runTest {
        val bookmarks = listOf(
            createBookmark(pageNumber = 0),
            createBookmark(pageNumber = 5)
        )
        every { bookmarkDao.getBookmarksForPdf(testPdfPath) } returns flowOf(bookmarks)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.bookmarks.size)
    }

    @Test
    fun `current page bookmark status is updated`() = runTest {
        val bookmarks = listOf(createBookmark(pageNumber = 0))
        every { bookmarkDao.getBookmarksForPdf(testPdfPath) } returns flowOf(bookmarks)

        viewModel = createViewModel()
        advanceUntilIdle()

        // Current page is 0, which is bookmarked
        assertTrue(viewModel.state.value.isCurrentPageBookmarked)
    }

    @Test
    fun `toggle page bookmark adds bookmark when not bookmarked`() = runTest {
        every { bookmarkDao.getBookmarksForPdf(testPdfPath) } returns flowOf(emptyList())
        val bookmarkSlot = slot<BookmarkEntity>()
        coEvery { bookmarkDao.insert(capture(bookmarkSlot)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TogglePageBookmark)
        advanceUntilIdle()

        coVerify { bookmarkDao.insert(any()) }
        assertEquals(testPdfPath, bookmarkSlot.captured.pdfPath)
        assertEquals(0, bookmarkSlot.captured.pageNumber)
    }

    @Test
    fun `toggle page bookmark removes bookmark when bookmarked`() = runTest {
        val bookmark = createBookmark(pageNumber = 0)
        every { bookmarkDao.getBookmarksForPdf(testPdfPath) } returns flowOf(listOf(bookmark))

        viewModel = createViewModel()
        advanceUntilIdle()

        // State should show page is bookmarked
        assertTrue(viewModel.state.value.isCurrentPageBookmarked)

        viewModel.onAction(ReaderAction.TogglePageBookmark)
        advanceUntilIdle()

        coVerify { bookmarkDao.deleteByPage(testPdfPath, 0) }
    }

    @Test
    fun `delete bookmark calls dao delete`() = runTest {
        val bookmark = createBookmark()
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.DeleteBookmark(bookmark))
        advanceUntilIdle()

        coVerify { bookmarkDao.delete(bookmark) }
    }
    // endregion

    // region Navigation Tests
    @Test
    fun `go to page updates current page state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.GoToPage(5))
        advanceUntilIdle()

        assertEquals(5, viewModel.state.value.currentPage)
    }
    // endregion

    // region Toolbar Tests
    @Test
    fun `toggle toolbar flips visibility`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialVisibility = viewModel.state.value.isToolbarVisible

        viewModel.onAction(ReaderAction.ToggleToolbar)
        advanceUntilIdle()

        assertEquals(!initialVisibility, viewModel.state.value.isToolbarVisible)
    }

    @Test
    fun `toggle toolbar exits full screen mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Enter full screen
        viewModel.onAction(ReaderAction.ToggleFullScreen)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isFullScreen)

        // Toggle toolbar should exit full screen and show toolbar
        viewModel.onAction(ReaderAction.ToggleToolbar)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isFullScreen)
        assertTrue(viewModel.state.value.isToolbarVisible)
    }
    // endregion

    // region Display Settings Tests
    @Test
    fun `set brightness updates state and persists`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetBrightness(0.7f))
        advanceUntilIdle()

        assertEquals(0.7f, viewModel.state.value.brightness)
        coVerify { preferencesRepository.setReaderBrightness(0.7f) }
    }

    @Test
    fun `set scroll mode updates state and persists`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetScrollMode(ScrollMode.HORIZONTAL))
        advanceUntilIdle()

        assertEquals(ScrollMode.HORIZONTAL, viewModel.state.value.scrollMode)
        coVerify { preferencesRepository.setReaderScrollMode(any()) }
    }

    @Test
    fun `set reading theme updates state and persists`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetReadingTheme(ReadingTheme.SEPIA))
        advanceUntilIdle()

        assertEquals(ReadingTheme.SEPIA, viewModel.state.value.readingTheme)
        coVerify { preferencesRepository.setReaderTheme(any()) }
    }

    @Test
    fun `set keep screen on updates state and persists`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetKeepScreenOn(true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.keepScreenOn)
        coVerify { preferencesRepository.setReaderKeepScreenOn(true) }
    }
    // endregion

    // region Sheet Visibility Tests
    @Test
    fun `show page jump dialog updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowPageJumpDialog)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isPageJumpDialogVisible)
    }

    @Test
    fun `hide page jump dialog updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowPageJumpDialog)
        viewModel.onAction(ReaderAction.HidePageJumpDialog)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPageJumpDialogVisible)
    }

    @Test
    fun `show bookmarks sheet updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowBookmarksSheet)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isBookmarksSheetVisible)
    }

    @Test
    fun `show zoom sheet updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowZoomSheet)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isZoomSheetVisible)
    }

    @Test
    fun `show display sheet updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowDisplaySheet)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isDisplaySheetVisible)
    }
    // endregion

    // region Favorite Tests
    @Test
    fun `add to favorite calls repository and updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.AddToFavorite)
        advanceUntilIdle()

        coVerify { favoriteRepository.addFavorite(any()) }
    }

    @Test
    fun `confirm remove favorite calls repository`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ConfirmRemoveFavorite)
        advanceUntilIdle()

        coVerify { favoriteRepository.removeFavorite(testPdfPath) }
    }

    @Test
    fun `isFavorite returns repository value`() = runTest {
        coEvery { favoriteRepository.isFavorite(testPdfPath) } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.isFavorite()

        assertTrue(result)
    }
    // endregion

    // region Search Tests
    @Test
    fun `search action updates search query state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.Search("test query"))
        advanceUntilIdle()

        assertEquals("test query", viewModel.state.value.searchQuery)
    }

    @Test
    fun `clear search resets search state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.Search("test"))
        viewModel.onAction(ReaderAction.ClearSearch)
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.searchQuery)
        assertEquals(0, viewModel.state.value.searchResultCount)
    }

    @Test
    fun `toggle search updates search active state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ToggleSearch)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSearchActive)
    }
    // endregion

    // region Auto Scroll Tests
    @Test
    fun `start auto scroll updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.StartAutoScroll(50f))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAutoScrollActive)
        assertEquals(50f, viewModel.state.value.autoScrollSpeed)
        assertFalse(viewModel.state.value.isToolbarVisible)
    }

    @Test
    fun `stop auto scroll updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.StartAutoScroll(50f))
        viewModel.onAction(ReaderAction.StopAutoScroll)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isAutoScrollActive)
    }

    @Test
    fun `toggle auto scroll pause updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.StartAutoScroll(50f))
        viewModel.onAction(ReaderAction.ToggleAutoScrollPause)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAutoScrollPaused)
    }

    @Test
    fun `set auto scroll speed updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetAutoScrollSpeed(75f))
        advanceUntilIdle()

        assertEquals(75f, viewModel.state.value.autoScrollSpeed)
    }
    // endregion

    // region Page Rotation Tests
    @Test
    fun `rotate clockwise increases rotation by 90`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialRotation = viewModel.state.value.pageRotation

        viewModel.onAction(ReaderAction.RotateClockwise)
        advanceUntilIdle()

        assertEquals((initialRotation + 90) % 360, viewModel.state.value.pageRotation)
    }

    @Test
    fun `rotate counter clockwise decreases rotation by 90`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Start at 90 to avoid negative
        viewModel.onAction(ReaderAction.RotateClockwise)
        advanceUntilIdle()
        assertEquals(90, viewModel.state.value.pageRotation)

        viewModel.onAction(ReaderAction.RotateCounterClockwise)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.pageRotation)
    }
    // endregion

    // region Zoom Tests
    @Test
    fun `set zoom updates state within bounds`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetZoom(2.0f))
        advanceUntilIdle()

        assertEquals(2.0f, viewModel.state.value.zoom)
    }

    @Test
    fun `set zoom clamps to max zoom`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val maxZoom = viewModel.state.value.maxZoom
        viewModel.onAction(ReaderAction.SetZoom(maxZoom + 10f))
        advanceUntilIdle()

        assertEquals(maxZoom, viewModel.state.value.zoom)
    }

    @Test
    fun `set zoom clamps to min zoom`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val minZoom = viewModel.state.value.minZoom
        viewModel.onAction(ReaderAction.SetZoom(minZoom - 1f))
        advanceUntilIdle()

        assertEquals(minZoom, viewModel.state.value.zoom)
    }
    // endregion

    // region Event Tests
    @Test
    fun `share document sends event`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ReaderAction.ShareDocument)
            advanceUntilIdle()

            assertEquals(ReaderEvent.ShareDocument, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `close document sends event`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ReaderAction.CloseDocument)
            advanceUntilIdle()

            assertEquals(ReaderEvent.DocumentClosed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Utility Method Tests
    @Test
    fun `getDocumentFileName returns correct filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.getDocumentFileName()

        assertEquals("test.pdf", result)
    }
    // endregion

    // region Highlights

    private fun tapped(id: Long) = TappedHighlight(id = id, x = 10f, y = 20f, w = 100f, h = 18f)

    private fun selection(
        page: Int = 5,
        text: String = "concentration gradient"
    ) = TextSelection(
        pageNumber = page,
        text = text,
        quads = listOf(PdfQuad(0.1f, 0.2f, 0.3f, 0.02f))
    )

    @Test
    fun `text selection change is tracked in state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        advanceUntilIdle()

        assertEquals("concentration gradient", viewModel.state.value.pendingSelection?.text)
    }

    @Test
    fun `start highlight opens the picker and captures the selection`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isHighlightPickerVisible)
        assertEquals("concentration gradient", viewModel.state.value.capturedSelection?.text)
    }

    @Test
    fun `start highlight does nothing without a selection`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.StartHighlight)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isHighlightPickerVisible)
    }

    /**
     * Dismissing the selection action mode clears the underlying selection, so the
     * picker must survive losing it or it would close the instant it opened.
     */
    @Test
    fun `picker survives the selection being cleared`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        viewModel.onAction(ReaderAction.TextSelectionChanged(null))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isHighlightPickerVisible)
        assertEquals("concentration gradient", viewModel.state.value.capturedSelection?.text)
    }

    @Test
    fun `applying a colour inserts a highlight from the captured selection`() = runTest {
        coEvery { annotationDao.getMaxSortIndexForPage(any(), any()) } returns null
        val inserted = slot<AnnotationEntity>()
        coEvery { annotationDao.insert(capture(inserted)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        viewModel.onAction(ReaderAction.ApplyHighlightColor(HighlightColors.GREEN))
        advanceUntilIdle()

        assertEquals("concentration gradient", inserted.captured.selectedText)
        assertEquals(HighlightColors.GREEN, inserted.captured.color)
        // The viewer reports 1-based pages, storage is 0-based.
        assertEquals(4, inserted.captured.pageNumber)
        assertEquals("highlight", inserted.captured.type)
    }

    @Test
    fun `a new highlight is appended after existing ones on the page`() = runTest {
        coEvery { annotationDao.getMaxSortIndexForPage(any(), any()) } returns 3
        val inserted = slot<AnnotationEntity>()
        coEvery { annotationDao.insert(capture(inserted)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        viewModel.onAction(ReaderAction.ApplyHighlightColor(HighlightColors.YELLOW))
        advanceUntilIdle()

        assertEquals(4, inserted.captured.sortIndex)
    }

    @Test
    fun `the first highlight on a page starts at sort index zero`() = runTest {
        coEvery { annotationDao.getMaxSortIndexForPage(any(), any()) } returns null
        val inserted = slot<AnnotationEntity>()
        coEvery { annotationDao.insert(capture(inserted)) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        viewModel.onAction(ReaderAction.ApplyHighlightColor(HighlightColors.YELLOW))
        advanceUntilIdle()

        assertEquals(0, inserted.captured.sortIndex)
    }

    @Test
    fun `applying a colour closes the picker and drops the captured selection`() = runTest {
        coEvery { annotationDao.getMaxSortIndexForPage(any(), any()) } returns null
        coEvery { annotationDao.insert(any()) } returns 1L

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        viewModel.onAction(ReaderAction.ApplyHighlightColor(HighlightColors.YELLOW))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isHighlightPickerVisible)
        assertNull(viewModel.state.value.capturedSelection)
    }

    @Test
    fun `tapping a highlight opens the picker in editing mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.HighlightTapped(tapped(42L)))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isHighlightPickerVisible)
        assertEquals(42L, viewModel.state.value.editingHighlightId)
    }

    @Test
    fun `applying a colour while editing updates instead of inserting`() = runTest {
        val existing = AnnotationEntity(
            id = 42L,
            pdfPath = testPdfPath,
            pageNumber = 3,
            type = "highlight",
            content = null,
            color = HighlightColors.YELLOW,
            selectedText = "existing"
        )
        coEvery { annotationDao.getById(42L) } returns existing
        val updated = slot<AnnotationEntity>()
        coEvery { annotationDao.update(capture(updated)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.HighlightTapped(tapped(42L)))
        viewModel.onAction(ReaderAction.ApplyHighlightColor(HighlightColors.BLUE))
        advanceUntilIdle()

        assertEquals(HighlightColors.BLUE, updated.captured.color)
        assertEquals("existing", updated.captured.selectedText)
        coVerify(exactly = 0) { annotationDao.insert(any()) }
    }

    @Test
    fun `deleting a highlight removes it and closes the picker`() = runTest {
        coEvery { annotationDao.deleteById(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.HighlightTapped(tapped(42L)))
        viewModel.onAction(ReaderAction.DeleteHighlight(42L))
        advanceUntilIdle()

        coVerify { annotationDao.deleteById(42L) }
        assertFalse(viewModel.state.value.isHighlightPickerVisible)
        assertNull(viewModel.state.value.editingHighlightId)
    }

    @Test
    fun `dismissing the picker clears editing and captured state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.TextSelectionChanged(selection()))
        viewModel.onAction(ReaderAction.StartHighlight)
        viewModel.onAction(ReaderAction.DismissHighlightPicker)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isHighlightPickerVisible)
        assertNull(viewModel.state.value.capturedSelection)
        assertNull(viewModel.state.value.editingHighlightId)
    }

    @Test
    fun `highlights from the database land in state`() = runTest {
        every { annotationDao.getHighlightsForPdf(any()) } returns flowOf(
            listOf(
                AnnotationEntity(
                    id = 1L,
                    pdfPath = testPdfPath,
                    pageNumber = 2,
                    type = "highlight",
                    content = null,
                    color = HighlightColors.PINK,
                    selectedText = "osmosis",
                    quads = """[{"x":0.1,"y":0.2,"w":0.3,"h":0.02}]"""
                )
            )
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        val highlights = viewModel.state.value.highlights
        assertEquals(1, highlights.size)
        assertEquals("osmosis", highlights[0].text)
        assertEquals(1, highlights[0].quads.size)
    }
    // endregion

    // region Highlight navigation

    private fun highlightEntity(id: Long, page: Int, sortIndex: Int = 0) = AnnotationEntity(
        id = id,
        pdfPath = testPdfPath,
        pageNumber = page,
        type = "highlight",
        content = null,
        color = HighlightColors.YELLOW,
        selectedText = "text $id",
        quads = """[{"x":0.1,"y":0.2,"w":0.3,"h":0.02}]""",
        sortIndex = sortIndex
    )

    private fun withHighlights(vararg entities: AnnotationEntity) {
        every { annotationDao.getHighlightsForPdf(any()) } returns flowOf(entities.toList())
    }

    @Test
    fun `next highlight starts at the first one`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3), highlightEntity(3, 7))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.currentHighlightIndex)
        assertTrue(viewModel.state.value.isHighlightNavVisible)
    }

    @Test
    fun `previous highlight from nothing starts at the last one`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3), highlightEntity(3, 7))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.PreviousHighlight)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.currentHighlightIndex)
    }

    @Test
    fun `next highlight advances through the list`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3), highlightEntity(3, 7))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        viewModel.onAction(ReaderAction.NextHighlight)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentHighlightIndex)
    }

    @Test
    fun `next highlight wraps around at the end`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3))

        viewModel = createViewModel()
        advanceUntilIdle()

        repeat(3) { viewModel.onAction(ReaderAction.NextHighlight) }
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.currentHighlightIndex)
    }

    @Test
    fun `previous highlight wraps around at the start`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        viewModel.onAction(ReaderAction.PreviousHighlight)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.currentHighlightIndex)
    }

    @Test
    fun `stepping does nothing when there are no highlights`() = runTest {
        withHighlights()

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        advanceUntilIdle()

        assertEquals(-1, viewModel.state.value.currentHighlightIndex)
        assertFalse(viewModel.state.value.isHighlightNavVisible)
    }

    @Test
    fun `position label is one-based`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3), highlightEntity(3, 7))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        advanceUntilIdle()

        assertEquals("1 / 3", viewModel.state.value.highlightPositionLabel)
    }

    @Test
    fun `closing the nav strip clears the position`() = runTest {
        withHighlights(highlightEntity(1, 0), highlightEntity(2, 3))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        viewModel.onAction(ReaderAction.HideHighlightNav)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isHighlightNavVisible)
        assertEquals(-1, viewModel.state.value.currentHighlightIndex)
    }

    /**
     * The list shrinks under the navigation strip when a highlight is deleted, so
     * the index must not be left pointing past the end.
     */
    @Test
    fun `index is clamped when the highlight list shrinks`() = runTest {
        val flow = MutableStateFlow(
            listOf(highlightEntity(1, 0), highlightEntity(2, 3), highlightEntity(3, 7))
        )
        every { annotationDao.getHighlightsForPdf(any()) } returns flow

        viewModel = createViewModel()
        advanceUntilIdle()

        repeat(3) { viewModel.onAction(ReaderAction.NextHighlight) }
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.currentHighlightIndex)

        flow.value = listOf(highlightEntity(1, 0))
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.currentHighlightIndex)
    }

    @Test
    fun `nav strip hides when the last highlight is deleted`() = runTest {
        val flow = MutableStateFlow(listOf(highlightEntity(1, 0)))
        every { annotationDao.getHighlightsForPdf(any()) } returns flow

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.NextHighlight)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isHighlightNavVisible)

        flow.value = emptyList()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isHighlightNavVisible)
    }
    // endregion

    // region Search and highlight integration

    @Test
    fun `no highlight matches when the search is empty`() = runTest {
        withHighlights(highlightEntity(1, 0))

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.highlightsMatchingSearch.isEmpty())
    }

    @Test
    fun `highlights matching the search are surfaced`() = runTest {
        every { annotationDao.getHighlightsForPdf(any()) } returns flowOf(
            listOf(
                highlightEntity(1, 0).copy(selectedText = "osmotic pressure"),
                highlightEntity(2, 1).copy(selectedText = "concentration gradient"),
                highlightEntity(3, 2).copy(selectedText = "cell membrane")
            )
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.Search("pressure"))
        advanceUntilIdle()

        val matches = viewModel.state.value.highlightsMatchingSearch
        assertEquals(1, matches.size)
        assertEquals("osmotic pressure", matches[0].text)
    }

    @Test
    fun `highlight search ignores case`() = runTest {
        every { annotationDao.getHighlightsForPdf(any()) } returns flowOf(
            listOf(highlightEntity(1, 0).copy(selectedText = "Osmotic Pressure"))
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.Search("OSMOTIC"))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.highlightsMatchingSearch.size)
    }

    @Test
    fun `opening the panel from search carries the query over`() = runTest {
        withHighlights(highlightEntity(1, 0))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowHighlightsSheet("gradient"))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isHighlightsSheetVisible)
        assertEquals("gradient", viewModel.state.value.highlightsSheetQuery)
    }

    @Test
    fun `opening the panel normally leaves the query empty`() = runTest {
        withHighlights(highlightEntity(1, 0))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.ShowHighlightsSheet())
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.highlightsSheetQuery)
    }
    // endregion

    // region Horizontal scroll lock (#74)

    @Test
    fun `horizontal lock is off by default`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.lockHorizontalScroll)
    }

    @Test
    fun `a stored lock is restored when the document opens`() = runTest {
        every { filePreferenceDao.observe(any()) } returns flowOf(
            FilePreferenceEntity(pdfPath = testPdfPath, lockHorizontalScroll = true)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.lockHorizontalScroll)
    }

    @Test
    fun `enabling the lock persists it for this document`() = runTest {
        val saved = slot<FilePreferenceEntity>()
        coEvery { filePreferenceDao.save(capture(saved)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.lockHorizontalScroll)
        assertTrue(saved.captured.lockHorizontalScroll)
        assertEquals(testPdfPath, saved.captured.pdfPath)
    }

    @Test
    fun `the lock is available in vertical scroll mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetScrollMode(ScrollMode.VERTICAL))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.canLockHorizontalScroll)
    }

    /**
     * Horizontal scroll mode needs that axis to move between pages, so locking it
     * would strand the reader on one page.
     */
    @Test
    fun `the lock is unavailable in horizontal scroll mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetScrollMode(ScrollMode.HORIZONTAL))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.canLockHorizontalScroll)
    }

    @Test
    fun `the lock cannot be turned on in horizontal scroll mode`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetScrollMode(ScrollMode.HORIZONTAL))
        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(true))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.lockHorizontalScroll)
    }

    @Test
    fun `switching to horizontal scroll mode releases an active lock`() = runTest {
        coEvery { filePreferenceDao.save(any()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.lockHorizontalScroll)

        viewModel.onAction(ReaderAction.SetScrollMode(ScrollMode.HORIZONTAL))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.lockHorizontalScroll)
    }

    @Test
    fun `releasing the lock on a mode switch is persisted too`() = runTest {
        val saved = mutableListOf<FilePreferenceEntity>()
        coEvery { filePreferenceDao.save(capture(saved)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(true))
        advanceUntilIdle()
        viewModel.onAction(ReaderAction.SetScrollMode(ScrollMode.HORIZONTAL))
        advanceUntilIdle()

        assertFalse(saved.last().lockHorizontalScroll)
    }

    @Test
    fun `turning the lock off persists that too`() = runTest {
        val saved = mutableListOf<FilePreferenceEntity>()
        coEvery { filePreferenceDao.save(capture(saved)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(true))
        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(false))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.lockHorizontalScroll)
        assertFalse(saved.last().lockHorizontalScroll)
    }

    @Test
    fun `an existing preference row is updated rather than replaced wholesale`() = runTest {
        coEvery { filePreferenceDao.get(testPdfPath) } returns FilePreferenceEntity(
            pdfPath = testPdfPath,
            lockHorizontalScroll = false,
            updatedAt = 1000L
        )
        val saved = slot<FilePreferenceEntity>()
        coEvery { filePreferenceDao.save(capture(saved)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onAction(ReaderAction.SetLockHorizontalScroll(true))
        advanceUntilIdle()

        assertEquals(testPdfPath, saved.captured.pdfPath)
        assertTrue(saved.captured.lockHorizontalScroll)
        assertTrue("updatedAt should move forward", saved.captured.updatedAt > 1000L)
    }
    // endregion
}
