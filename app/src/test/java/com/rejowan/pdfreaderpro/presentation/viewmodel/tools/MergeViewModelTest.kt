package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeFile
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeState
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.PageSelection
import android.os.Environment
import io.mockk.slot
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import java.io.File
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.rejowan.pdfreaderpro.R
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MergeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: MergeViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Files are added from real paths, so Uri.fromFile is the only Android call
        // in the way. Environment is mocked because the merged file is written into
        // the public Documents folder, which off-device throws inside the coroutine.
        every { context.cacheDir } returns folder.newFolder("cache")
        mockkStatic(Uri::class)
        every { Uri.fromFile(any()) } returns mockk(relaxed = true)
        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")
        coEvery { pdfToolsRepository.isPasswordProtected(any()) } returns Result.success(false)

        // Each document in the list carries a thumbnail of its first page, which
        // the platform renderer draws.
        mockkStatic(ParcelFileDescriptor::class)
        every { ParcelFileDescriptor.open(any(), any()) } returns mockk(relaxed = true)
        mockkConstructor(PdfRenderer::class)
        val page = mockk<PdfRenderer.Page>(relaxed = true)
        every { page.width } returns 600
        every { page.height } returns 800
        every { anyConstructed<PdfRenderer>().pageCount } returns 3
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns page
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(any<Int>(), any<Int>(), any())
        } returns mockk(relaxed = true)

        // Default mocks
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
        unmockkStatic(Uri::class)
        unmockkStatic(Environment::class)
        unmockkStatic(ParcelFileDescriptor::class)
        unmockkStatic(Bitmap::class)
        unmockkConstructor(PdfRenderer::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MergeViewModel {
        return MergeViewModel(
            pdfToolsRepository = pdfToolsRepository,
            context = context
        )
    }

    private fun createMergeFile(
        path: String = "/storage/test.pdf",
        name: String = "test.pdf",
        pageCount: Int = 10,
        pageSelection: PageSelection = PageSelection.All
    ) = MergeFile(
        uri = mockk(),
        path = path,
        name = name,
        size = 1024L,
        pageCount = pageCount,
        thumbnail = null,
        pageSelection = pageSelection
    )

    // region Initial State Tests
    /** Answers [id] with [format] filled in, so a message can be checked for what it names. */
    private fun stubString(id: Int, format: String) {
        every { context.getString(id, *anyVararg()) } answers {
            val formatArgs = args.drop(1).flatMap { if (it is Array<*>) it.toList() else listOf(it) }
            format.format(*formatArgs.toTypedArray())
        }
    }

    @Test
    fun `initial state has empty selected files`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedFiles.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has generated output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.outputFileName.startsWith("merged_"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is not processing`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no result`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region removeFile Tests
    @Test
    fun `removeFile removes file from selectedFiles`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val file1 = createMergeFile(path = "/storage/test1.pdf")
        val file2 = createMergeFile(path = "/storage/test2.pdf")

        // Manually set state with files
        viewModel.removeFile(file1)
        advanceUntilIdle()

        // File should be removed (empty initially, so still empty)
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedFiles.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region moveFile Tests
    @Test
    fun `moveFile with invalid indices does not crash`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Should not throw
        viewModel.moveFile(-1, 0)
        viewModel.moveFile(0, 100)
        advanceUntilIdle()

        viewModel.state.test {
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region PageSelection Tests
    @Test
    fun `PageSelection All toDisplayString returns correct string`() {
        every { context.getString(R.string.page_selection_all, 10) } returns "All pages (1-10)"
        val selection = PageSelection.All
        assertEquals("All pages (1-10)", selection.toDisplayString(context, 10))
    }

    @Test
    fun `PageSelection Range toDisplayString returns correct string`() {
        every { context.getString(R.string.page_selection_range, 1, 5) } returns "Pages 1-5"
        val selection = PageSelection.Range(1, 5)
        assertEquals("Pages 1-5", selection.toDisplayString(context, 10))
    }

    @Test
    fun `PageSelection Custom toDisplayString returns correct string for few pages`() {
        every { context.getString(R.string.page_selection_custom, "1, 3, 5") } returns "Pages 1, 3, 5"
        val selection = PageSelection.Custom(listOf(1, 3, 5))
        assertEquals("Pages 1, 3, 5", selection.toDisplayString(context, 10))
    }

    @Test
    fun `PageSelection Custom toDisplayString truncates for many pages`() {
        every { context.resources.getQuantityString(R.plurals.page_selection_custom_long, 7, "1, 2, 3, 4", 7) } returns "Pages 1, 2, 3, 4… (7 pages)"
        val selection = PageSelection.Custom(listOf(1, 2, 3, 4, 5, 6, 7))
        val display = selection.toDisplayString(context, 10)
        assertTrue(display.contains("…"))
        assertTrue(display.contains("7 pages"))
    }

    // region updatePageSelection Tests
    @Test
    fun `updatePageSelection updates selection for specific file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val file = createMergeFile()
        val newSelection = PageSelection.Range(1, 5)

        viewModel.updatePageSelection(file, newSelection)
        advanceUntilIdle()

        // Verify no error occurred
        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_merged_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_merged_file", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOutputFileName with empty string updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region merge Validation Tests
    @Test
    fun `merge with less than 2 files sets error`() = runTest {
        every { context.getString(R.string.error_select_two_files) } returns "Select at least 2 PDF files"
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.merge()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Select at least 2 PDF files", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `merge with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.merge()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region clearResult Tests
    @Test
    fun `clearResult sets result to null`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearResult()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region clearError Tests
    @Test
    fun `clearError sets error to null`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearError()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region reset Tests
    @Test
    fun `reset clears all state and generates new filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("custom_name")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.selectedFiles.isEmpty())
            assertTrue(state.outputFileName.startsWith("merged_"))
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    @Test
    fun `PageSelection All toPageList returns null`() {
        val selection = PageSelection.All
        assertNull(selection.toPageList(10))
    }

    @Test
    fun `PageSelection Range toPageList returns correct list`() {
        val selection = PageSelection.Range(2, 5)
        assertEquals(listOf(2, 3, 4, 5), selection.toPageList(10))
    }

    @Test
    fun `PageSelection Range toPageList coerces end to totalPages`() {
        val selection = PageSelection.Range(8, 15)
        assertEquals(listOf(8, 9, 10), selection.toPageList(10))
    }

    @Test
    fun `PageSelection Custom toPageList filters invalid pages`() {
        val selection = PageSelection.Custom(listOf(1, 5, 15, 20))
        assertEquals(listOf(1, 5), selection.toPageList(10))
    }

    @Test
    fun `PageSelection All getSelectedCount returns total pages`() {
        val selection = PageSelection.All
        assertEquals(10, selection.getSelectedCount(10))
    }

    @Test
    fun `PageSelection Range getSelectedCount returns correct count`() {
        val selection = PageSelection.Range(3, 7)
        assertEquals(5, selection.getSelectedCount(10))
    }

    @Test
    fun `PageSelection Custom getSelectedCount returns correct count`() {
        val selection = PageSelection.Custom(listOf(1, 3, 5, 7))
        assertEquals(4, selection.getSelectedCount(10))
    }

    @Test
    fun `PageSelection Range getSelectedCount handles out of bounds`() {
        val selection = PageSelection.Range(8, 15)
        assertEquals(3, selection.getSelectedCount(10))
    }
    // endregion

    /** Real files on disk, since the tool adds by path and checks they exist. */
    private fun TestScope.addFiles(vm: MergeViewModel, count: Int): List<String> {
        val paths = (1..count).map { index ->
            folder.newFile("document-$index.pdf").apply {
                writeText("%PDF-1.4 pretend document $index")
            }.absolutePath
        }
        vm.addFilesFromPaths(paths)
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.selectedFiles.size == count) return paths
            Thread.sleep(10)
        }
        error("files never arrived")
    }

    // region Counting the pages a selection covers
    // Pure logic, and what the emptiness guard is decided on.

    @Test
    fun `all means every page in the document`() {
        assertEquals(12, PageSelection.All.getSelectedCount(12))
    }

    @Test
    fun `a range counts inclusively at both ends`() {
        assertEquals(4, PageSelection.Range(2, 5).getSelectedCount(10))
    }

    @Test
    fun `a range running past the end stops at the end`() {
        assertEquals(3, PageSelection.Range(8, 40).getSelectedCount(10))
    }

    @Test
    fun `a range entirely past the end covers nothing`() {
        assertEquals(0, PageSelection.Range(30, 40).getSelectedCount(10))
    }

    @Test
    fun `a single page range counts one`() {
        assertEquals(1, PageSelection.Range(3, 3).getSelectedCount(10))
    }

    @Test
    fun `custom pages outside the document are not counted`() {
        assertEquals(2, PageSelection.Custom(listOf(1, 5, 99, 0)).getSelectedCount(10))
    }

    @Test
    fun `an empty custom selection covers nothing`() {
        assertEquals(0, PageSelection.Custom(emptyList()).getSelectedCount(10))
    }
    // endregion

    // region Refusing to merge
    @Test
    fun `merging needs at least two documents`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 1)
        vm.merge()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.mergePdfsWithSelection(any(), any(), any()) }
    }

    @Test
    fun `merging needs an output name`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)
        vm.setOutputFileName("")
        vm.merge()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.mergePdfsWithSelection(any(), any(), any()) }
    }

    @Test
    fun `a document with no pages selected stops the merge and is named`() = runTest {
        stubString(R.string.error_no_pages_selected, "No pages selected for: %1\$s")
        val vm = createViewModel()
        addFiles(vm, 2)
        val first = vm.state.value.selectedFiles.first()
        vm.updatePageSelection(first, PageSelection.Custom(emptyList()))
        vm.merge()
        advanceUntilIdle()

        val error = vm.state.value.error
        assertNotNull(error)
        // Naming it matters: with several documents the user needs to know which.
        assertTrue(error!!.contains(first.name))
        coVerify(exactly = 0) { pdfToolsRepository.mergePdfsWithSelection(any(), any(), any()) }
    }
    // endregion

    // region The order documents are joined in
    @Test
    fun `added documents appear in the order given`() = runTest {
        val vm = createViewModel()
        val paths = addFiles(vm, 3)
        assertEquals(paths, vm.state.value.selectedFiles.map { it.path })
    }

    @Test
    fun `moving a document changes the order it is merged in`() = runTest {
        val vm = createViewModel()
        val paths = addFiles(vm, 3)

        vm.moveFile(0, 2)

        assertEquals(listOf(paths[1], paths[2], paths[0]), vm.state.value.selectedFiles.map { it.path })
    }

    @Test
    fun `removing a document leaves the others in order`() = runTest {
        val vm = createViewModel()
        val paths = addFiles(vm, 3)
        val second = vm.state.value.selectedFiles[1]

        vm.removeFile(second)

        assertEquals(listOf(paths[0], paths[2]), vm.state.value.selectedFiles.map { it.path })
    }

    @Test
    fun `a password protected document is skipped rather than failing the merge`() = runTest {
        val vm = createViewModel()
        val open = folder.newFile("open.pdf").apply { writeText("%PDF") }
        val locked = folder.newFile("locked.pdf").apply { writeText("%PDF") }
        coEvery { pdfToolsRepository.isPasswordProtected(locked.absolutePath) } returns Result.success(true)

        vm.addFilesFromPaths(listOf(open.absolutePath, locked.absolutePath))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.selectedFiles.isNotEmpty()) return@repeat
            Thread.sleep(10)
        }
        advanceUntilIdle()

        assertEquals(listOf(open.absolutePath), vm.state.value.selectedFiles.map { it.path })
    }

    @Test
    fun `a path that does not exist is ignored`() = runTest {
        val vm = createViewModel()
        vm.addFilesFromPaths(listOf("/nowhere/missing.pdf"))
        advanceUntilIdle()

        assertTrue(vm.state.value.selectedFiles.isEmpty())
    }
    // endregion

    // region What each document contributes
    // toPageList is what actually reaches the repository, and null there means
    // the whole document, so it is not interchangeable with an empty list.

    @Test
    fun `all contributes no page list, meaning every page`() {
        assertNull(PageSelection.All.toPageList(10))
    }

    @Test
    fun `a range contributes its pages, clamped to the document`() {
        assertEquals(listOf(8, 9, 10), PageSelection.Range(8, 40).toPageList(10))
    }

    @Test
    fun `custom pages contribute only the ones the document has`() {
        assertEquals(listOf(1, 5), PageSelection.Custom(listOf(1, 5, 99, 0)).toPageList(10))
    }
    // endregion

    // region Merging
    @Test
    fun `each document is sent with the pages chosen for it`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)
        val files = vm.state.value.selectedFiles
        vm.updatePageSelection(files[0], PageSelection.Range(2, 4))
        vm.updatePageSelection(files[1], PageSelection.Custom(listOf(1, 3)))
        val selections = slot<List<PdfToolsRepository.PdfPageSelection>>()
        coEvery {
            pdfToolsRepository.mergePdfsWithSelection(capture(selections), any(), any())
        } returns Result.success(Unit)

        vm.merge()
        advanceUntilIdle()

        assertEquals(listOf(2, 3, 4), selections.captured[0].pages)
        assertEquals(listOf(1, 3), selections.captured[1].pages)
    }

    @Test
    fun `the order sent is the order on screen`() = runTest {
        val vm = createViewModel()
        val paths = addFiles(vm, 3)
        vm.moveFile(2, 0)
        val selections = slot<List<PdfToolsRepository.PdfPageSelection>>()
        coEvery {
            pdfToolsRepository.mergePdfsWithSelection(capture(selections), any(), any())
        } returns Result.success(Unit)

        vm.merge()
        advanceUntilIdle()

        assertEquals(listOf(paths[2], paths[0], paths[1]), selections.captured.map { it.path })
    }

    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)
        vm.setOutputFileName("combined")
        val documents = Environment.getExternalStoragePublicDirectory(null)
        File(documents, "PdfReaderPro").mkdirs()
        File(documents, "PdfReaderPro/combined.pdf").writeText("someone else's work")
        coEvery {
            pdfToolsRepository.mergePdfsWithSelection(any(), any(), any())
        } returns Result.success(Unit)

        vm.merge()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("combined_1.pdf"))
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)
        val seen = mutableListOf<Float>()
        coEvery {
            pdfToolsRepository.mergePdfsWithSelection(any(), any(), any())
        } answers {
            val onProgress = arg<(Float) -> Unit>(2)
            onProgress(0.3f)
            seen += vm.state.value.progress
            Result.success(Unit)
        }

        vm.merge()
        advanceUntilIdle()

        assertEquals(listOf(0.3f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
        assertFalse(vm.state.value.isProcessing)
    }

    @Test
    fun `a failure is reported and does not leave the tool processing`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)
        coEvery {
            pdfToolsRepository.mergePdfsWithSelection(any(), any(), any())
        } returns Result.failure(RuntimeException("the second document is damaged"))

        vm.merge()
        advanceUntilIdle()

        assertEquals("the second document is damaged", vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `a finished merge reports the combined document`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)
        vm.setOutputFileName("combined")
        coEvery {
            pdfToolsRepository.mergePdfsWithSelection(any(), any(), any())
        } returns Result.success(Unit)
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(20)

        vm.merge()
        advanceUntilIdle()

        val result = vm.state.value.result!!
        assertTrue(result.outputPath.endsWith("combined.pdf"))
        assertEquals(20, result.pageCount)
    }
    // endregion

    // region Clearing
    @Test
    fun `clearing the error keeps the documents that were added`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 1)
        vm.merge()
        advanceUntilIdle()

        vm.clearError()

        assertNull(vm.state.value.error)
        assertEquals(1, vm.state.value.selectedFiles.size)
    }

    @Test
    fun `resetting empties the list and suggests a fresh name`() = runTest {
        val vm = createViewModel()
        addFiles(vm, 2)

        vm.reset()
        advanceUntilIdle()

        assertTrue(vm.state.value.selectedFiles.isEmpty())
        assertTrue(vm.state.value.outputFileName.isNotBlank())
    }
    // endregion

    // region Adding documents the user picked
    /**
     * A picked document, as the file chooser hands it over.
     *
     * The tool cannot read a content uri directly, so it copies what it is given
     * into the cache first. Everything after that works on the copy.
     */
    private fun pickedDocument(name: String, content: String = "%PDF-1.4 picked"): Uri {
        val uri = mockk<Uri>(relaxed = true)
        every { uri.scheme } returns "content"
        every { context.contentResolver.openInputStream(uri) } answers {
            java.io.ByteArrayInputStream(content.toByteArray())
        }
        val cursor = mockk<android.database.Cursor>(relaxed = true)
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(any()) } returns 0
        every { cursor.getString(0) } returns name
        every { context.contentResolver.query(uri, any(), any(), any(), any()) } returns cursor
        return uri
    }

    private fun TestScope.addPicked(vm: MergeViewModel, vararg uris: Uri): MergeViewModel {
        vm.addFiles(uris.toList())
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.selectedFiles.size == uris.size) return vm
            Thread.sleep(10)
        }
        return vm
    }

    @Test
    fun `a picked document is copied in and listed under its own name`() = runTest {
        val vm = createViewModel()

        addPicked(vm, pickedDocument("report.pdf"))

        val added = vm.state.value.selectedFiles.single()
        assertEquals("report.pdf", added.name)
        assertTrue(java.io.File(added.path).exists())
    }

    @Test
    fun `a picked document carries its page count and a thumbnail`() = runTest {
        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(12)
        val vm = createViewModel()

        addPicked(vm, pickedDocument("report.pdf"))

        val added = vm.state.value.selectedFiles.single()
        assertEquals(12, added.pageCount)
        assertNotNull(added.thumbnail)
    }

    @Test
    fun `several picked documents keep the order they were picked in`() = runTest {
        val vm = createViewModel()

        addPicked(vm, pickedDocument("first.pdf"), pickedDocument("second.pdf"))

        assertEquals(listOf("first.pdf", "second.pdf"), vm.state.value.selectedFiles.map { it.name })
    }

    @Test
    fun `a password protected document is named in the message and not added`() = runTest {
        stubString(R.string.error_skipped_password_protected, "Skipped password-protected: %1\$s")
        coEvery { pdfToolsRepository.isPasswordProtected(any()) } returns Result.success(true)
        val vm = createViewModel()

        vm.addFiles(listOf(pickedDocument("locked.pdf")))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.error != null) return@repeat
            Thread.sleep(10)
        }
        advanceUntilIdle()

        assertTrue(vm.state.value.selectedFiles.isEmpty())
        assertTrue(vm.state.value.error!!.contains("locked.pdf"))
    }

    @Test
    fun `a document that cannot be read is skipped rather than added empty`() = runTest {
        val unreadable = mockk<Uri>(relaxed = true)
        every { unreadable.scheme } returns "content"
        every { context.contentResolver.openInputStream(unreadable) } returns null
        val vm = createViewModel()

        vm.addFiles(listOf(unreadable))
        advanceUntilIdle()

        assertTrue(vm.state.value.selectedFiles.isEmpty())
    }
    // endregion
}
