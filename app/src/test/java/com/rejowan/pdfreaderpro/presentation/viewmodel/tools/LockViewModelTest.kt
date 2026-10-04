package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.lock.LockViewModel
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.os.Environment
import io.mockk.every
import io.mockk.slot
import io.mockk.coVerify
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.ByteArrayInputStream
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: LockViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Loading a document is what the tool's guards are written against, and the
        // copy behind it fails silently against a relaxed mock. Environment is
        // mocked because the output directory is the public Documents folder, which
        // off-device throws inside the coroutine and surfaces in the next test.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null

        // The tool shows the first page next to its settings, drawn by the
        // platform renderer, which has no JVM implementation.
        mockkStatic(ParcelFileDescriptor::class)
        every { ParcelFileDescriptor.open(any(), any()) } returns mockk(relaxed = true)
        mockkConstructor(PdfRenderer::class)
        val previewPage = mockk<PdfRenderer.Page>(relaxed = true)
        every { previewPage.width } returns 600
        every { previewPage.height } returns 800
        every { anyConstructed<PdfRenderer>().pageCount } returns 5
        every { anyConstructed<PdfRenderer>().openPage(any()) } returns previewPage
        mockkStatic(Bitmap::class)
        every {
            Bitmap.createBitmap(any<Int>(), any<Int>(), any())
        } returns mockk(relaxed = true)

        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
        unmockkStatic(ParcelFileDescriptor::class)
        unmockkStatic(Bitmap::class)
        unmockkConstructor(PdfRenderer::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LockViewModel {
        return LockViewModel(
            pdfToolsRepository = pdfToolsRepository,
            context = context
        )
    }

    // region Initial State Tests
    @Test
    fun `initial state has no source file`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty passwords`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.userPassword)
            assertEquals("", state.ownerPassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has all permissions disabled`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.allowPrinting)
            assertFalse(state.allowCopying)
            assertFalse(state.allowModifying)
            assertFalse(state.allowAnnotations)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has empty output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is not loading`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
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

    // region Password Tests
    @Test
    fun `setUserPassword updates user password`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setUserPassword("mypassword")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("mypassword", state.userPassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOwnerPassword updates owner password`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOwnerPassword("ownerpass")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("ownerpass", state.ownerPassword)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Permission Tests
    @Test
    fun `setAllowPrinting updates permission`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAllowPrinting(true)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.allowPrinting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setAllowCopying updates permission`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAllowCopying(true)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.allowCopying)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setAllowModifying updates permission`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAllowModifying(true)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.allowModifying)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setAllowAnnotations updates permission`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setAllowAnnotations(true)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.allowAnnotations)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_locked_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_locked_file", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region lock Validation Tests
    @Test
    fun `lock without source file sets error`() = runTest {
        every { context.getString(R.string.error_select_pdf_first) } returns "Please select a PDF file first"
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.lock()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `lock with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.lock()
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
    fun `reset clears all state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("custom_name")
        viewModel.setUserPassword("password")
        viewModel.setOwnerPassword("ownerpass")
        viewModel.setAllowPrinting(true)
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals("", state.userPassword)
            assertEquals("", state.ownerPassword)
            assertFalse(state.allowPrinting)
            assertEquals("", state.outputFileName)
            assertFalse(state.isLoading)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    /**
     * Loads a document and waits for it to arrive.
     *
     * Loading starts on the main dispatcher, which only runs when the scheduler is
     * advanced, and then does file work off it. Neither wait alone is enough.
     */
    private fun TestScope.loadDocument(vm: LockViewModel) {
        vm.setSourceFile(mockk(relaxed = true))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile != null) return
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    // region Refusing to lock
    // Every one of these returns before the repository is touched. That matters:
    // the tool can overwrite the original, so a half specified lock must not run.

    private fun TestScope.lockedWith(configure: (LockViewModel) -> Unit): LockViewModel {
        coEvery {
            pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any())
        } returns Result.success(Unit)
        val vm = createViewModel()
        loadDocument(vm)
        configure(vm)
        vm.lock()
        advanceUntilIdle()
        return vm
    }

    @Test
    fun `without a document it asks for one and locks nothing`() = runTest {
        val vm = createViewModel()
        vm.setOwnerPassword("secret")
        vm.lock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `an owner password is required`() = runTest {
        val vm = lockedWith { it.setOwnerPassword("") }

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `a short owner password is refused rather than silently accepted`() = runTest {
        val vm = lockedWith { it.setOwnerPassword("abc") }

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `an owner password of exactly four characters is allowed`() = runTest {
        lockedWith { it.setOwnerPassword("abcd") }

        coVerify { pdfToolsRepository.lockPdf(any(), any(), any(), "abcd", any(), any()) }
    }

    @Test
    fun `a short user password is refused`() = runTest {
        val vm = lockedWith {
            it.setOwnerPassword("secret")
            it.setUserPassword("ab")
        }

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `no user password at all is fine, it only restricts editing`() = runTest {
        lockedWith {
            it.setOwnerPassword("secret")
            it.setUserPassword("")
        }

        coVerify { pdfToolsRepository.lockPdf(any(), any(), "", "secret", any(), any()) }
    }

    @Test
    fun `without an output name it asks for one and locks nothing`() = runTest {
        val vm = lockedWith {
            it.setOwnerPassword("secret")
            it.setOutputFileName("")
        }

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) }
    }
    // endregion

    // region What gets locked
    @Test
    fun `both passwords reach the repository as given`() = runTest {
        lockedWith {
            it.setOwnerPassword("owner-pass")
            it.setUserPassword("user-pass")
        }

        coVerify {
            pdfToolsRepository.lockPdf(any(), any(), "user-pass", "owner-pass", any(), any())
        }
    }

    @Test
    fun `the permissions chosen are the permissions applied`() = runTest {
        val permissions = slot<PdfToolsRepository.PdfPermissions>()
        coEvery {
            pdfToolsRepository.lockPdf(any(), any(), any(), any(), capture(permissions), any())
        } returns Result.success(Unit)

        val vm = createViewModel()
        loadDocument(vm)
        vm.setOwnerPassword("secret")
        vm.setAllowPrinting(true)
        vm.setAllowCopying(false)
        vm.setAllowModifying(false)
        vm.setAllowAnnotations(true)
        vm.lock()
        advanceUntilIdle()

        assertTrue(permissions.captured.allowPrinting)
        assertFalse(permissions.captured.allowCopying)
        assertFalse(permissions.captured.allowModifying)
        assertTrue(permissions.captured.allowAnnotations)
    }

    @Test
    fun `the document that was loaded is the one locked`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        val path = vm.state.value.sourceFile?.path
        coEvery {
            pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        vm.setOwnerPassword("secret")
        vm.lock()
        advanceUntilIdle()

        coVerify { pdfToolsRepository.lockPdf(path!!, any(), any(), any(), any(), any()) }
    }

    @Test
    fun `a failure is surfaced and processing stops`() = runTest {
        coEvery {
            pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("no space"))

        val vm = createViewModel()
        loadDocument(vm)
        vm.setOwnerPassword("secret")
        vm.lock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
    }
    // endregion

    // region Where the finished document goes
    @Test
    fun `an existing file is not written over, a numbered one is used instead`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setOwnerPassword("owner-pass")
        vm.setOutputFileName("locked")
        val documents = Environment.getExternalStoragePublicDirectory(null)
        java.io.File(documents, "PdfReaderPro").mkdirs()
        java.io.File(documents, "PdfReaderPro/locked.pdf").writeText("someone else's work")
        coEvery { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) } returns Result.success(Unit)

        vm.lock()
        advanceUntilIdle()

        assertTrue(vm.state.value.result!!.outputPath.endsWith("locked_1.pdf"))
    }

    @Test
    fun `overwriting writes through a temporary file, then replaces the original`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setOwnerPassword("owner-pass")
        vm.setOverwriteOriginal(true)
        val sourcePath = vm.state.value.sourceFile!!.path
        val target = slot<String>()
        coEvery {
            pdfToolsRepository.lockPdf(any(), capture(target), any(), any(), any(), any())
        } answers {
            java.io.File(target.captured).writeText("finished bytes")
            Result.success(Unit)
        }

        vm.lock()
        advanceUntilIdle()

        assertNotEquals(sourcePath, target.captured)
        assertFalse(java.io.File(target.captured).exists())
        assertEquals("finished bytes", java.io.File(sourcePath).readText())
    }

    @Test
    fun `progress from the repository reaches the screen`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setOwnerPassword("owner-pass")
        val seen = mutableListOf<Float>()
        coEvery { pdfToolsRepository.lockPdf(any(), any(), any(), any(), any(), any()) } answers {
            val onProgress = arg<(Float) -> Unit>(5)
            onProgress(0.4f)
            seen += vm.state.value.progress
            Result.success(Unit)
        }

        vm.lock()
        advanceUntilIdle()

        assertEquals(listOf(0.4f), seen)
        assertEquals(1f, vm.state.value.progress, 0.001f)
    }
    // endregion
}
