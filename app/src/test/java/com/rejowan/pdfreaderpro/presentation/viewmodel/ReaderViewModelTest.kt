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
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.data.local.SignatureStore
import com.rejowan.pdfreaderpro.data.local.database.dao.SignatureDao
import com.rejowan.pdfreaderpro.data.local.database.entity.SignatureEntity
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfEditor
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfListener
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var recentRepository: RecentRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var annotationDao: AnnotationDao
    private lateinit var filePreferenceDao: FilePreferenceDao
    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var signatureStore: SignatureStore
    private lateinit var signatureDao: SignatureDao
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
        pdfToolsRepository = mockk(relaxed = true)
        signatureStore = mockk(relaxed = true)
        signatureDao = mockk(relaxed = true)
        applicationContext = mockk(relaxed = true)
        passwordStorage = mockk(relaxed = true)

        savedStateHandle = SavedStateHandle(mapOf("path" to testPdfPath, "initialPage" to 0))
        every { applicationContext.filesDir } returns folder.root
        // Saving into the open document is refused for a document the app copied
        // into its own cache, and a relaxed mock's empty path matches everything.
        every { applicationContext.cacheDir } returns folder.newFolder("app-cache")
        every { applicationContext.codeCacheDir } returns folder.newFolder("app-code-cache")

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
        // A relaxed mock would hand back an empty string, which reads as a stored
        // password and silently changes which branch the reader takes.
        coEvery { passwordStorage.getPassword(any()) } returns null
    }

    @After
    fun teardown() {
        // Some of this work encodes images off the main dispatcher and resumes back
        // on it. Giving those a moment to land keeps a stray continuation from
        // arriving after the main dispatcher has been taken away, which would
        // otherwise surface as a failure in whichever test ran next.
        Thread.sleep(100)
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
            pdfToolsRepository = pdfToolsRepository,
            signatureStore = signatureStore,
            signatureDao = signatureDao,
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

    // ===========================================
    // Signatures
    // ===========================================

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var editor: PdfEditor
    private lateinit var viewer: PdfViewer
    private var placementsAskedFor = 0

    /**
     * Waits for work that hops to the IO dispatcher.
     *
     * Placing a signature encodes the image off the main dispatcher, which virtual
     * time does not cover, so advancing alone would race the assertion.
     */
    private fun TestScope.waitFor(what: String, condition: () -> Boolean) {
        repeat(200) {
            advanceUntilIdle()
            if (condition()) {
                // Let whatever satisfied it finish resuming on the main dispatcher
                // as well, so nothing lands there after the test has reset it.
                repeat(3) {
                    Thread.sleep(10)
                    advanceUntilIdle()
                }
                return
            }
            Thread.sleep(10)
        }
        error("$what never happened")
    }

    /**
     * Attaches a viewer and hands back the listener it registered.
     *
     * The viewer drives most of this feature: it decides where a signature lands
     * and reports back, so simulating those callbacks is the only way to reach the
     * code that stores a placement.
     */
    private fun TestScope.attachViewer(vm: ReaderViewModel): PdfListener {
        editor = mockk(relaxed = true)
        viewer = mockk(relaxed = true)
        every { viewer.editor } returns editor
        every { applicationContext.filesDir } returns folder.root
        placementsAskedFor = 0
        every { editor.placeSignatureImage(any(), any()) } answers { placementsAskedFor++ }
        val listener = slot<PdfListener>()

        vm.setPdfViewer(viewer)
        advanceUntilIdle()

        verify { viewer.addListener(capture(listener)) }
        return listener.captured
    }

    /** Makes the viewer report [json] when asked what it has placed. */
    private fun viewerReports(json: String) {
        every { editor.getPlacedSignatures(any()) } answers {
            firstArg<(String) -> Unit>().invoke(json)
        }
    }

    private fun placedJson(
        key: String = "pdfjs_internal_editor_1",
        page: Int = 2,
        rect: String = "[100.0,200.0,300.0,400.0]"
    ) = """[{"key":"$key","pageIndex":$page,"rect":$rect}]"""

    private fun storedSignature(
        id: Long = 1L,
        page: Int = 0,
        imagePath: String = "/does/not/matter.png"
    ) = SignatureEntity(
        id = id,
        pdfPath = testPdfPath,
        pageIndex = page,
        rectLeft = 10f,
        rectBottom = 20f,
        rectRight = 30f,
        rectTop = 40f,
        savedSignatureId = null,
        imagePath = imagePath
    )

    private fun signatureBitmap(): android.graphics.Bitmap {
        val bitmap = mockk<android.graphics.Bitmap>(relaxed = true)
        every { bitmap.compress(any(), any(), any()) } answers {
            (thirdArg() as java.io.OutputStream).write("png-bytes".toByteArray())
            true
        }
        return bitmap
    }

    // region Making a signature
    @Test
    fun `a signature the user does not keep is placed without being saved`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)

        vm.onSignatureCaptured(signatureBitmap(), remember = false)
        waitFor("placement") { placementsAskedFor > 0 }

        coVerify(exactly = 0) { signatureStore.save(any()) }
    }

    @Test
    fun `a signature the user keeps is saved and appears in the list`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)
        val file = folder.newFile("kept.png")
        coEvery { signatureStore.save(any()) } returns
            SignatureStore.SavedSignature("kept", file, 1L)
        coEvery { signatureStore.list() } returns
            listOf(SignatureStore.SavedSignature("kept", file, 1L))

        vm.onSignatureCaptured(signatureBitmap(), remember = true)
        advanceUntilIdle()

        assertEquals(listOf("kept"), vm.state.value.savedSignatures.map { it.id })
    }

    @Test
    fun `placing a signature closes the sheet, so the document is visible`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)
        vm.onAction(ReaderAction.StartSigning)
        advanceUntilIdle()

        vm.onSignatureCaptured(signatureBitmap(), remember = false)
        waitFor("placement") { placementsAskedFor > 0 }

        assertFalse(vm.state.value.isSignatureSheetVisible)
    }

    @Test
    fun `placing a signature with no viewer attached is reported, not silent`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onSignatureCaptured(signatureBitmap(), remember = false)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
    }
    // endregion

    // region Storing where a signature landed
    @Test
    fun `a placement is stored at the position the viewer gave it`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        viewerReports(placedJson(page = 3, rect = "[12.5,25.0,112.5,75.0]"))
        vm.onSignatureCaptured(signatureBitmap(), remember = false)
        waitFor("placement") { placementsAskedFor > 0 }
        val stored = slot<SignatureEntity>()
        coEvery { signatureDao.insert(capture(stored)) } returns 1L

        listener.onSignaturePlaced(true)
        waitFor("the placement being stored") { stored.isCaptured }

        assertEquals(testPdfPath, stored.captured.pdfPath)
        assertEquals(3, stored.captured.pageIndex)
        assertEquals(12.5f, stored.captured.rectLeft)
        assertEquals(75f, stored.captured.rectTop)
    }

    @Test
    fun `a placement keeps its own copy of the image`() = runTest {
        // Deleting the saved signature it came from must not empty a document that
        // already uses it, so the row points at a copy rather than the original.
        val vm = createViewModel()
        val listener = attachViewer(vm)
        viewerReports(placedJson())
        vm.onSignatureCaptured(signatureBitmap(), remember = false)
        waitFor("placement") { placementsAskedFor > 0 }
        val stored = slot<SignatureEntity>()
        coEvery { signatureDao.insert(capture(stored)) } returns 1L

        listener.onSignaturePlaced(true)
        waitFor("the placement being stored") { stored.isCaptured }

        val copy = File(stored.captured.imagePath)
        assertTrue(copy.exists())
        assertEquals("png-bytes", copy.readText())
        assertTrue(copy.parentFile!!.name == "signature_placements")
    }

    @Test
    fun `the newest placement is the one stored, not an older one`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        viewerReports(
            """[{"key":"a","pageIndex":0,"rect":[1.0,2.0,3.0,4.0]},
                {"key":"b","pageIndex":5,"rect":[9.0,8.0,7.0,6.0]}]"""
        )
        vm.onSignatureCaptured(signatureBitmap(), remember = false)
        waitFor("placement") { placementsAskedFor > 0 }
        val stored = slot<SignatureEntity>()
        coEvery { signatureDao.insert(capture(stored)) } returns 1L

        listener.onSignaturePlaced(true)
        waitFor("the placement being stored") { stored.isCaptured }

        assertEquals(5, stored.captured.pageIndex)
    }

    @Test
    fun `a placement the viewer could not make is reported`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)

        vm.events.test {
            listener.onSignaturePlaced(false)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
    }

    @Test
    fun `an unreadable report from the viewer stores nothing rather than throwing`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        viewerReports("this is not json")
        vm.onSignatureCaptured(signatureBitmap(), remember = false)
        waitFor("placement") { placementsAskedFor > 0 }

        listener.onSignaturePlaced(true)
        advanceUntilIdle()

        coVerify(exactly = 0) { signatureDao.insert(any()) }
    }
    // endregion

    // region Listing and removing
    @Test
    fun `opening the sheet lists what is placed in this document`() = runTest {
        coEvery { signatureDao.get(testPdfPath) } returns
            listOf(storedSignature(id = 7L, page = 4))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onAction(ReaderAction.StartSigning)
        advanceUntilIdle()

        assertTrue(vm.state.value.isSignatureSheetVisible)
        assertEquals(listOf(7L), vm.state.value.placedSignatureList.map { it.id })
        assertEquals(4, vm.state.value.placedSignatureList.single().pageIndex)
        assertEquals(1, vm.state.value.placedSignatures)
    }

    @Test
    fun `removing a placement drops its row and rebuilds the document`() = runTest {
        coEvery { signatureDao.get(testPdfPath) } returns emptyList()
        val vm = createViewModel()
        attachViewer(vm)

        vm.onAction(ReaderAction.RemovePlacedSignature(7L))
        advanceUntilIdle()

        coVerify { signatureDao.deleteById(7L) }
        verify { viewer.loadFromFile(testPdfPath) }
    }

    @Test
    fun `discarding drops every placement in this document only`() = runTest {
        coEvery { signatureDao.get(testPdfPath) } returns emptyList()
        val vm = createViewModel()
        attachViewer(vm)

        vm.onAction(ReaderAction.DiscardSignatures)
        advanceUntilIdle()

        coVerify { signatureDao.deleteAllFor(testPdfPath) }
        assertEquals(0, vm.state.value.placedSignatures)
    }

    @Test
    fun `deleting a saved signature takes it out of the list`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        coEvery { signatureStore.list() } returns emptyList()

        vm.onAction(ReaderAction.DeleteSavedSignature("gone"))
        advanceUntilIdle()

        coVerify { signatureStore.delete("gone") }
        assertTrue(vm.state.value.savedSignatures.isEmpty())
    }

    @Test
    fun `a saved signature that will not load is reported rather than placed`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)
        coEvery { signatureStore.load("missing") } returns null

        vm.events.test {
            vm.onAction(ReaderAction.PlaceSavedSignature("missing"))
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
    }

    @Test
    fun `going to a placement closes the sheet and turns to its page`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)
        vm.onAction(ReaderAction.StartSigning)
        advanceUntilIdle()

        vm.onAction(ReaderAction.GoToPlacedSignature(4))
        advanceUntilIdle()

        assertFalse(vm.state.value.isSignatureSheetVisible)
        // The viewer counts pages from one, the state from zero.
        verify { viewer.goToPage(5) }
    }
    // endregion

    // region Replaying placements when the document reopens
    @Test
    fun `stored placements are put back when the document loads`() = runTest {
        val image = folder.newFile("placement.png").apply { writeBytes("png".toByteArray()) }
        coEvery { signatureDao.get(testPdfPath) } returns
            listOf(storedSignature(imagePath = image.absolutePath))
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onPageLoadSuccess(10)
        waitFor("the placement being replayed") { placementsAskedFor > 0 }
    }

    @Test
    fun `a replayed placement is moved to the position it was stored at`() = runTest {
        val image = folder.newFile("placement2.png").apply { writeBytes("png".toByteArray()) }
        coEvery { signatureDao.get(testPdfPath) } returns
            listOf(storedSignature(imagePath = image.absolutePath))
        val vm = createViewModel()
        val listener = attachViewer(vm)
        viewerReports(placedJson(key = "restored"))
        listener.onPageLoadSuccess(10)
        waitFor("the placement being replayed") { placementsAskedFor > 0 }

        listener.onSignaturePlaced(true)
        advanceUntilIdle()

        // Stored left and top, not whatever the viewer chose when creating it.
        verify { editor.moveSignatureTo("restored", 10f, 40f) }
        coVerify(exactly = 0) { signatureDao.insert(any()) }
    }

    @Test
    fun `a placement whose image has gone is dropped rather than left invisible`() = runTest {
        coEvery { signatureDao.get(testPdfPath) } returns
            listOf(storedSignature(id = 3L, imagePath = "/nowhere/gone.png"))
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onPageLoadSuccess(10)
        advanceUntilIdle()

        coVerify { signatureDao.deleteById(3L) }
        verify(exactly = 0) { editor.placeSignatureImage(any(), any()) }
    }
    // endregion

    // region Saving a signed document
    @Test
    fun `asking for a signed copy with no viewer is reported`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.requestSignedCopy(mockUri)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
    }

    @Test
    fun `asking for a signed copy has the viewer serialise the document`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)

        vm.requestSignedCopy(mockUri)
        advanceUntilIdle()

        verify { viewer.downloadFile() }
        assertTrue(vm.state.value.isSavingSignedCopy)
    }

    @Test
    fun `the suggested names say what the copy is, so it is not mistaken for the original`() =
        runTest {
            val vm = createViewModel()
            advanceUntilIdle()

            assertEquals("test.pdf", vm.getDocumentFileName())
            assertEquals("test-signed.pdf", vm.getSignedFileName())
            assertEquals("test-unlocked.pdf", vm.getDecryptedFileName())
            assertEquals("test-highlighted.pdf", vm.getHighlightedFileName())
        }
    // endregion

    // ===========================================
    // Tapping the page
    // ===========================================

    /** A reader with tap-to-turn on and a document of ten pages open. */
    private fun TestScope.readerOnPage(page: Int, horizontal: Boolean = false): ReaderViewModel {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onAction(ReaderAction.SetTapToTurnPage(true))
        if (horizontal) vm.onAction(ReaderAction.SetScrollMode(ScrollMode.HORIZONTAL))
        val listener = attachViewer(vm)
        listener.onPageLoadSuccess(10)
        advanceUntilIdle()
        vm.onAction(ReaderAction.GoToPage(page))
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `a tap near the top goes back a page when scrolling vertically`() = runTest {
        val vm = readerOnPage(5)

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 500f, y = 50f, width = 1000f, height = 1000f))
        advanceUntilIdle()

        verify { viewer.goToPreviousPage() }
    }

    @Test
    fun `a tap near the bottom goes on a page`() = runTest {
        val vm = readerOnPage(5)

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 500f, y = 950f, width = 1000f, height = 1000f))
        advanceUntilIdle()

        verify { viewer.goToNextPage() }
    }

    @Test
    fun `a tap in the middle leaves the page and works the toolbar instead`() = runTest {
        val vm = readerOnPage(5)
        val toolbarBefore = vm.state.value.isToolbarVisible

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 500f, y = 500f, width = 1000f, height = 1000f))
        advanceUntilIdle()

        verify(exactly = 0) { viewer.goToNextPage() }
        verify(exactly = 0) { viewer.goToPreviousPage() }
        assertNotEquals(toolbarBefore, vm.state.value.isToolbarVisible)
    }

    @Test
    fun `the turn zones follow the scroll direction`() = runTest {
        // Scrolling sideways, the edges that turn pages are the left and right ones,
        // so a tap near the top must not turn anything.
        val vm = readerOnPage(5, horizontal = true)

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 500f, y = 50f, width = 1000f, height = 1000f))
        advanceUntilIdle()
        verify(exactly = 0) { viewer.goToPreviousPage() }

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 50f, y = 500f, width = 1000f, height = 1000f))
        advanceUntilIdle()
        verify { viewer.goToPreviousPage() }
    }

    @Test
    fun `with tap to turn off, a tap at the edge only works the toolbar`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onAction(ReaderAction.SetTapToTurnPage(false))
        val listener = attachViewer(vm)
        listener.onPageLoadSuccess(10)
        vm.onAction(ReaderAction.GoToPage(5))
        advanceUntilIdle()

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 500f, y = 950f, width = 1000f, height = 1000f))
        advanceUntilIdle()

        verify(exactly = 0) { viewer.goToNextPage() }
    }

    @Test
    fun `a tap with no dimensions cannot be placed, so it works the toolbar`() = runTest {
        // Guards a divide by zero as much as anything.
        val vm = readerOnPage(5)

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 0f, y = 0f, width = 0f, height = 0f))
        advanceUntilIdle()

        verify(exactly = 0) { viewer.goToPreviousPage() }
    }

    @Test
    fun `while auto scrolling, a tap does not turn the page`() = runTest {
        val vm = readerOnPage(5)
        vm.onAction(ReaderAction.StartAutoScroll(1f))
        advanceUntilIdle()

        vm.onAction(ReaderAction.TapToTurnOrToggle(x = 500f, y = 950f, width = 1000f, height = 1000f))
        advanceUntilIdle()

        verify(exactly = 0) { viewer.goToNextPage() }
    }

    // ===========================================
    // What the viewer reports back
    // ===========================================

    @Test
    fun `the page the viewer moved to becomes the current page`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        listener.onPageLoadSuccess(10)
        advanceUntilIdle()

        // The viewer counts from one, the state from zero.
        listener.onPageChange(4)
        advanceUntilIdle()

        assertEquals(3, vm.state.value.currentPage)
    }

    @Test
    fun `a document that will not open leaves an error rather than a spinner`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onPageLoadFailed(Exception("this file is damaged"))
        advanceUntilIdle()

        assertEquals("this file is damaged", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `the outline is flattened with each level marked, so it can be indented`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        val child = com.rejowan.pdfreaderpro.presentation.components.pdf.model.SideBarTreeItem(
            id = "1.1", title = "Background", page = 3, children = emptyList(), dest = null
        )
        val parent = com.rejowan.pdfreaderpro.presentation.components.pdf.model.SideBarTreeItem(
            id = "1", title = "Introduction", page = 1, children = listOf(child), dest = null
        )

        listener.onLoadOutline(listOf(parent))
        advanceUntilIdle()

        val outline = vm.state.value.outline
        assertEquals(listOf("Introduction", "Background"), outline.map { it.title })
        assertEquals(listOf(0, 1), outline.map { it.level })
    }

    @Test
    fun `going to an outline entry turns to its page`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)

        vm.navigateToOutlineItem(
            com.rejowan.pdfreaderpro.presentation.screens.reader.components.OutlineItem(
                title = "Chapter 2", page = 11, level = 0, id = "2", dest = null
            )
        )
        advanceUntilIdle()

        assertEquals(11, vm.state.value.currentPage)
        verify { viewer.goToPage(12) }
    }

    @Test
    fun `printing hands the document title to the viewer`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)

        vm.printDocument()
        advanceUntilIdle()

        verify { viewer.printFile("test") }
    }

    @Test
    fun `print progress is shown while the pages are being prepared`() = runTest {
        // Preparing a long document takes tens of seconds, and without this the
        // reader looked frozen.
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onPrintProcessStart()
        advanceUntilIdle()
        assertEquals(0f, vm.state.value.printProgress)

        listener.onPrintProcessProgress(0.5f)
        advanceUntilIdle()
        assertEquals(0.5f, vm.state.value.printProgress)

        listener.onPrintProcessEnd()
        advanceUntilIdle()
        assertNull(vm.state.value.printProgress)
    }

    @Test
    fun `a cancelled print stops showing progress`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        listener.onPrintProcessProgress(0.5f)
        advanceUntilIdle()

        listener.onPrintCancelled()
        advanceUntilIdle()

        assertNull(vm.state.value.printProgress)
    }

    // ===========================================
    // Passwords
    // ===========================================

    @Test
    fun `the viewer asking for a password is surfaced to the user`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onPasswordDialogChange(true)
        advanceUntilIdle()

        assertTrue(vm.state.value.isPasswordRequired)
    }

    @Test
    fun `a submitted password is handed to the viewer`() = runTest {
        val vm = createViewModel()
        attachViewer(vm)

        vm.onAction(ReaderAction.SubmitPassword("hunter2", remember = false))
        advanceUntilIdle()

        assertTrue(vm.state.value.passwordSubmitted)
        assertFalse(vm.state.value.isPasswordRequired)
        verify { viewer.ui.passwordDialog.submitPassword("hunter2") }
    }

    @Test
    fun `a password is only kept when the user asked and the setting allows it`() = runTest {
        every { preferencesRepository.preferences } returns
            flowOf(AppPreferences(rememberPasswords = true))
        val vm = createViewModel()
        attachViewer(vm)

        vm.onAction(ReaderAction.SubmitPassword("hunter2", remember = true))
        advanceUntilIdle()

        coVerify { passwordStorage.savePassword(testPdfPath, "hunter2") }
    }

    @Test
    fun `a password is not kept when the user did not ask`() = runTest {
        every { preferencesRepository.preferences } returns
            flowOf(AppPreferences(rememberPasswords = true))
        val vm = createViewModel()
        attachViewer(vm)

        vm.onAction(ReaderAction.SubmitPassword("hunter2", remember = false))
        advanceUntilIdle()

        coVerify(exactly = 0) { passwordStorage.savePassword(any(), any()) }
    }

    @Test
    fun `a password is not kept when the setting is off, whatever the user ticked`() = runTest {
        every { preferencesRepository.preferences } returns
            flowOf(AppPreferences(rememberPasswords = false))
        val vm = createViewModel()
        attachViewer(vm)

        vm.onAction(ReaderAction.SubmitPassword("hunter2", remember = true))
        advanceUntilIdle()

        coVerify(exactly = 0) { passwordStorage.savePassword(any(), any()) }
    }

    // ===========================================
    // Acting on the file itself
    // ===========================================

    /** A reader open on a document that really exists. */
    private fun readerFor(file: File) = ReaderViewModel(
        recentRepository = recentRepository,
        favoriteRepository = favoriteRepository,
        preferencesRepository = preferencesRepository,
        bookmarkDao = bookmarkDao,
        annotationDao = annotationDao,
        filePreferenceDao = filePreferenceDao,
        pdfToolsRepository = pdfToolsRepository,
        signatureStore = signatureStore,
        signatureDao = signatureDao,
        applicationContext = applicationContext,
        savedStateHandle = SavedStateHandle(
            mapOf("path" to file.absolutePath, "initialPage" to 0)
        ),
        passwordStorage = passwordStorage
    )

    @Test
    fun `deleting the document removes it and everything the app remembers about it`() = runTest {
        val file = folder.newFile("doomed.pdf").apply { writeText("%PDF-1.4") }
        val vm = readerFor(file)
        advanceUntilIdle()

        vm.events.test {
            vm.onAction(ReaderAction.ConfirmDelete)
            advanceUntilIdle()

            assertTrue(awaitItem() is ReaderEvent.DocumentDeleted)
        }
        assertFalse(file.exists())
        coVerify { recentRepository.removeRecent(file.absolutePath) }
        coVerify { favoriteRepository.removeFavorite(file.absolutePath) }
        coVerify { passwordStorage.removePassword(file.absolutePath) }
    }

    @Test
    fun `deleting a document that is already gone is reported, not treated as done`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onAction(ReaderAction.ConfirmDelete)
            advanceUntilIdle()

            assertTrue(awaitItem() is ReaderEvent.Error)
        }
    }

    @Test
    fun `saving a copy writes the document to where the user chose`() = runTest {
        val source = folder.newFile("original.pdf").apply { writeText("%PDF-1.4 the contents") }
        val destination = folder.newFile("copy.pdf")
        every {
            applicationContext.contentResolver.openOutputStream(any())
        } returns destination.outputStream()
        val vm = readerFor(source)
        advanceUntilIdle()

        vm.saveToUri(mockUri)
        advanceUntilIdle()

        assertEquals("%PDF-1.4 the contents", destination.readText())
    }

    @Test
    fun `a copy that cannot be written is reported`() = runTest {
        every {
            applicationContext.contentResolver.openOutputStream(any())
        } throws java.io.IOException("no room")
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.saveToUri(mockUri)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
    }

    // ===========================================
    // Document details
    // ===========================================

    @Test
    fun `the details fall back to the file when the document says nothing`() = runTest {
        val file = folder.newFile("details.pdf").apply { writeText("%PDF-1.4 the contents") }
        val vm = readerFor(file)
        advanceUntilIdle()

        val info = vm.getPdfInfo()

        assertEquals(file.absolutePath, info.path)
        assertEquals("details", info.title)
        assertEquals(file.length(), info.fileSize)
        assertFalse(info.isEncrypted)
    }

    @Test
    fun `blank values in the document are treated as absent`() = runTest {
        // pdf.js reports empty strings for fields that were never filled in, and
        // showing an empty row is worse than showing none.
        val vm = createViewModel()
        attachViewer(vm)
        every { viewer.properties } returns mockk(relaxed = true) {
            every { author } returns ""
            every { subject } returns "  "
            every { creationDate } returns "null"
            every { title } returns "The Real Title"
        }

        val info = vm.getPdfInfo()

        assertNull(info.author)
        assertNull(info.subject)
        assertNull(info.creationDate)
        assertEquals("The Real Title", info.title)
    }

    @Test
    fun `a stored password is tried without troubling the user`() = runTest {
        every { preferencesRepository.preferences } returns
            flowOf(AppPreferences(rememberPasswords = true))
        coEvery { passwordStorage.getPassword(testPdfPath) } returns "remembered"
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onPasswordDialogChange(true)
        advanceUntilIdle()

        verify { viewer.ui.passwordDialog.submitPassword("remembered") }
        assertFalse(vm.state.value.isPasswordRequired)
    }

    @Test
    fun `a stored password that no longer works is forgotten and the user is asked`() = runTest {
        // The viewer asking a second time is the only signal that the silent
        // submission failed, and keeping a stale password would repeat it forever.
        every { preferencesRepository.preferences } returns
            flowOf(AppPreferences(rememberPasswords = true))
        coEvery { passwordStorage.getPassword(testPdfPath) } returns "stale"
        val vm = createViewModel()
        val listener = attachViewer(vm)
        listener.onPasswordDialogChange(true)
        advanceUntilIdle()

        listener.onPasswordDialogChange(true)
        advanceUntilIdle()

        coVerify { passwordStorage.removePassword(testPdfPath) }
        assertTrue(vm.state.value.isPasswordRequired)
        assertTrue(vm.state.value.isPasswordError)
    }

    @Test
    fun `a password the user typed that does not work is reported as wrong`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        listener.onPasswordDialogChange(true)
        advanceUntilIdle()
        vm.onAction(ReaderAction.SubmitPassword("wrong", remember = false))
        advanceUntilIdle()

        // The viewer asking again is how a wrong password makes itself known.
        listener.onPasswordDialogChange(true)
        advanceUntilIdle()

        assertTrue(vm.state.value.isPasswordError)
        assertTrue(vm.state.value.isPasswordRequired)
    }

    @Test
    fun `the document opening clears the password prompt`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)
        listener.onPasswordDialogChange(true)
        advanceUntilIdle()

        listener.onPasswordDialogChange(false)
        advanceUntilIdle()

        assertFalse(vm.state.value.isPasswordRequired)
        assertFalse(vm.state.value.isPasswordError)
    }

    // ===========================================
    // Writing copies of the document
    // ===========================================

    @Test
    fun `the signed document is written to where the user chose`() = runTest {
        val destination = folder.newFile("signed.pdf")
        every {
            applicationContext.contentResolver.openOutputStream(any())
        } returns destination.outputStream()
        val vm = createViewModel()
        val listener = attachViewer(vm)
        vm.requestSignedCopy(mockUri)
        advanceUntilIdle()

        // The viewer hands the serialised document back on the download callback.
        listener.onDownload("signed bytes".toByteArray(), "test.pdf", "application/pdf")
        waitFor("the copy being written") { destination.readText().isNotEmpty() }

        assertEquals("signed bytes", destination.readText())
        assertFalse(vm.state.value.isSavingSignedCopy)
    }

    @Test
    fun `a signed copy that cannot be written is reported`() = runTest {
        every { applicationContext.contentResolver.openOutputStream(any()) } returns null
        val vm = createViewModel()
        val listener = attachViewer(vm)
        vm.requestSignedCopy(mockUri)
        advanceUntilIdle()

        vm.events.test {
            listener.onDownload("signed bytes".toByteArray(), null, null)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
        assertFalse(vm.state.value.isSavingSignedCopy)
    }

    @Test
    fun `saving into the file itself replaces it and reloads the viewer`() = runTest {
        val document = folder.newFile("in-place.pdf").apply { writeText("%PDF-1.4 original") }
        val vm = readerFor(document)
        val listener = attachViewer(vm)
        vm.onAction(ReaderAction.ConfirmSaveSignedInPlace)
        vm.onAction(ReaderAction.SaveSignedInPlace)
        advanceUntilIdle()

        listener.onDownload("%PDF-1.4 signed".toByteArray(), null, null)
        waitFor("the document being replaced") { document.readText().contains("signed") }

        assertEquals("%PDF-1.4 signed", document.readText())
        // Nothing is left half written next to the original.
        assertFalse(File(document.parentFile, "${document.name}.signing").exists())
        verify { viewer.loadFromFile(document.absolutePath) }
    }

    @Test
    fun `saving into the file clears the pending placements, they belong to it now`() = runTest {
        val document = folder.newFile("in-place2.pdf").apply { writeText("%PDF-1.4 original") }
        val vm = readerFor(document)
        val listener = attachViewer(vm)
        vm.onAction(ReaderAction.SaveSignedInPlace)
        advanceUntilIdle()

        listener.onDownload("%PDF-1.4 signed".toByteArray(), null, null)
        waitFor("the document being replaced") { document.readText().contains("signed") }

        coVerify { signatureDao.deleteAllFor(document.absolutePath) }
    }

    @Test
    fun `a document that cannot be written to is not offered as a place to save`() = runTest {
        val document = folder.newFile("read-only.pdf").apply {
            writeText("%PDF-1.4 original")
            setWritable(false, false)
        }
        // Skipped where the filesystem or user cannot make a file read only.
        if (document.canWrite()) return@runTest
        val vm = readerFor(document)
        attachViewer(vm)
        advanceUntilIdle()

        assertFalse(vm.state.value.canSaveInPlace)
        vm.events.test {
            vm.onAction(ReaderAction.SaveSignedInPlace)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
        // Nothing was even asked of the viewer, so the document is untouched.
        verify(exactly = 0) { viewer.downloadFile() }
        assertEquals("%PDF-1.4 original", document.readText())
    }

    @Test
    fun `a document opened from the app's own cache cannot be saved into`() = runTest {
        // It is a copy, so writing to it would look like it worked and change
        // nothing the user can see.
        val cache = folder.newFolder("copy-cache")
        every { applicationContext.cacheDir } returns cache
        val copy = File(cache, "shared.pdf").apply { writeText("%PDF-1.4 copy") }
        val vm = readerFor(copy)
        advanceUntilIdle()

        assertFalse(vm.state.value.canSaveInPlace)
    }

    // region Highlights baked into a copy
    @Test
    fun `with nothing highlighted there is nothing to bake`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.bakeHighlightsToUri(mockUri)
        advanceUntilIdle()

        verify(exactly = 0) { applicationContext.contentResolver.openOutputStream(any()) }
    }

    @Test
    fun `baking a copy that cannot be written is reported`() = runTest {
        every { applicationContext.contentResolver.openOutputStream(any()) } returns null
        withHighlights(highlightEntity(1, 0))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.bakeHighlightsToUri(mockUri)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
        assertFalse(vm.state.value.isBakingHighlights)
    }
    // endregion

    // region A copy without the password
    @Test
    fun `a decrypted copy needs the password the document was opened with`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.saveDecryptedCopyToUri(mockUri)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
        coVerify(exactly = 0) { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `a decrypted copy is written with the password already given`() = runTest {
        val destination = folder.newFile("decrypted.pdf")
        every { applicationContext.cacheDir } returns folder.newFolder("cache")
        every {
            applicationContext.contentResolver.openOutputStream(any())
        } returns destination.outputStream()
        coEvery { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) } answers {
            File(secondArg<String>()).writeText("%PDF-1.4 no longer locked")
            Result.success(Unit)
        }
        val vm = createViewModel()
        attachViewer(vm)
        vm.onAction(ReaderAction.SubmitPassword("hunter2", remember = false))
        advanceUntilIdle()

        vm.saveDecryptedCopyToUri(mockUri)
        waitFor("the copy being written") { destination.readText().isNotEmpty() }

        coVerify { pdfToolsRepository.unlockPdf(testPdfPath, any(), "hunter2", any()) }
        assertEquals("%PDF-1.4 no longer locked", destination.readText())
        assertFalse(vm.state.value.isSavingDecryptedCopy)
    }

    @Test
    fun `a decryption that fails is reported and leaves nothing staged`() = runTest {
        val cache = folder.newFolder("cache2")
        every { applicationContext.cacheDir } returns cache
        coEvery {
            pdfToolsRepository.unlockPdf(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("wrong password"))
        val vm = createViewModel()
        attachViewer(vm)
        vm.onAction(ReaderAction.SubmitPassword("hunter2", remember = false))
        advanceUntilIdle()

        vm.events.test {
            vm.saveDecryptedCopyToUri(mockUri)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.Error)
        }
        assertTrue(cache.listFiles()!!.isEmpty())
        assertFalse(vm.state.value.isSavingDecryptedCopy)
    }
    // endregion

    // region Attachments
    @Test
    fun `the attachments in a document are listed`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onLoadAttachments(
            listOf(
                com.rejowan.pdfreaderpro.presentation.components.pdf.model.SideBarTreeItem(
                    id = "1", title = "spreadsheet.xlsx", page = 0,
                    children = emptyList(), dest = null
                )
            )
        )
        advanceUntilIdle()

        assertEquals(listOf("spreadsheet.xlsx"), vm.state.value.attachments.map { it.title })
    }

    @Test
    fun `an attachment with no name is still listed`() = runTest {
        val vm = createViewModel()
        val listener = attachViewer(vm)

        listener.onLoadAttachments(
            listOf(
                com.rejowan.pdfreaderpro.presentation.components.pdf.model.SideBarTreeItem(
                    id = "1", title = null, page = 0, children = emptyList(), dest = null
                )
            )
        )
        advanceUntilIdle()

        assertEquals(1, vm.state.value.attachments.size)
    }
    // endregion

    // region Favourites
    @Test
    fun `favouriting a document records it and says so`() = runTest {
        coEvery { favoriteRepository.isFavorite(any()) } returns false
        val vm = createViewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.onAction(ReaderAction.AddToFavorite)
            advanceUntilIdle()
            assertTrue(awaitItem() is ReaderEvent.FavoriteAdded)
        }
        assertTrue(vm.state.value.isFavorite)
    }

    @Test
    fun `removing a favourite clears it`() = runTest {
        coEvery { favoriteRepository.isFavorite(any()) } returns true
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onAction(ReaderAction.ConfirmRemoveFavorite)
        advanceUntilIdle()

        coVerify { favoriteRepository.removeFavorite(testPdfPath) }
        assertFalse(vm.state.value.isFavorite)
    }
    // endregion
}
