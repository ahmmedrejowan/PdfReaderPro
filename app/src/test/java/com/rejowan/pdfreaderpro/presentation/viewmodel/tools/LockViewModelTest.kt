package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.lock.LockViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: LockViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        coEvery { pdfToolsRepository.getPageCount(any()) } returns Result.success(10)
    }

    @After
    fun teardown() {
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
}
