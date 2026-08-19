package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.unlock.UnlockViewModel
import android.os.Environment
import io.mockk.every
import io.mockk.coVerify
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
class UnlockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: UnlockViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // The guards are written against a loaded document, and the copy behind
        // loading fails silently on a relaxed mock. Environment is mocked because
        // the output directory is the public Documents folder, which off-device
        // throws inside the coroutine and surfaces in whichever test runs next.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("%PDF-1.4 pretend document".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null
        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
        coEvery { pdfToolsRepository.isPasswordProtected(any()) } returns Result.success(true)
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UnlockViewModel {
        return UnlockViewModel(
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
    fun `initial state has empty password`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.password)
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

    // region setPassword Tests
    @Test
    fun `setPassword updates password`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPassword("mypassword")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("mypassword", state.password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setPassword with empty string updates state`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPassword("")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.password)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_unlocked_file")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_unlocked_file", state.outputFileName)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region unlock Validation Tests
    @Test
    fun `unlock without source file sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.unlock()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please select a PDF file first", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unlock with blank password sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPassword("")
        viewModel.unlock()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unlock with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.unlock()
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
        viewModel.setPassword("password")
        viewModel.reset()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.sourceFile)
            assertEquals("", state.password)
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

    private fun TestScope.loadDocument(vm: UnlockViewModel) {
        vm.setSourceFile(mockk(relaxed = true))
        // Loading starts on the main dispatcher and then does file work off it, so
        // advancing the scheduler alone is not enough to see the result.
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.sourceFile != null) return
            Thread.sleep(10)
        }
        error("document never loaded")
    }

    // region Refusing to unlock
    @Test
    fun `without a document it asks for one and unlocks nothing`() = runTest {
        val vm = createViewModel()
        vm.setPassword("secret")
        vm.unlock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `a document that is not protected is left alone`() = runTest {
        coEvery { pdfToolsRepository.isPasswordProtected(any()) } returns Result.success(false)

        val vm = createViewModel()
        loadDocument(vm)
        vm.setPassword("secret")
        vm.unlock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `the password is required`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setPassword("")
        vm.unlock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `a blank password is not treated as a password`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setPassword("    ")
        vm.unlock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) }
    }

    @Test
    fun `without an output name it asks for one and unlocks nothing`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)
        vm.setPassword("secret")
        vm.setOutputFileName("")
        vm.unlock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) }
    }
    // endregion

    // region Unlocking
    @Test
    fun `the password typed is the password tried`() = runTest {
        coEvery { pdfToolsRepository.unlockPdf(any(), any(), any(), any()) } returns Result.success(Unit)

        val vm = createViewModel()
        loadDocument(vm)
        vm.setPassword("hunter2")
        vm.unlock()
        advanceUntilIdle()

        coVerify { pdfToolsRepository.unlockPdf(any(), any(), "hunter2", any()) }
    }

    @Test
    fun `a wrong password is reported rather than leaving it looking successful`() = runTest {
        coEvery {
            pdfToolsRepository.unlockPdf(any(), any(), any(), any())
        } returns Result.failure(RuntimeException("Bad user password"))

        val vm = createViewModel()
        loadDocument(vm)
        vm.setPassword("wrong")
        vm.unlock()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `loading a protected document notes that it is protected`() = runTest {
        val vm = createViewModel()
        loadDocument(vm)

        assertTrue(vm.state.value.sourceFile?.isPasswordProtected == true)
    }
    // endregion
}
