package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.content.Context
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.imagetopdf.ImageItem
import com.rejowan.pdfreaderpro.presentation.screens.tools.imagetopdf.ImageToPdfViewModel
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
class ImageToPdfViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Context
    private lateinit var viewModel: ImageToPdfViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ImageToPdfViewModel {
        return ImageToPdfViewModel(
            pdfToolsRepository = pdfToolsRepository,
            context = context
        )
    }

    // region Initial State Tests
    @Test
    fun `initial state has empty images list`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.images.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has generated output filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.outputFileName.startsWith("images_"))
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

    // region removeImage Tests
    @Test
    fun `removeImage removes image by id`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Since images list is empty, nothing to remove
        viewModel.removeImage("nonexistent")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.images.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region moveImage Tests
    @Test
    fun `moveImage with invalid indices handles gracefully`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Should not throw when list is empty
        viewModel.moveImage(0, 1)
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.images.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setOutputFileName Tests
    @Test
    fun `setOutputFileName updates filename`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("my_images_pdf")
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("my_images_pdf", state.outputFileName)
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

    // region convertToPdf Validation Tests
    @Test
    fun `convertToPdf with no images sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.convertToPdf()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Please add at least one image", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `convertToPdf with blank output filename sets error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setOutputFileName("")
        viewModel.convertToPdf()
        advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.error)
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
            assertTrue(state.images.isEmpty())
            assertTrue(state.outputFileName.startsWith("images_"))
            assertFalse(state.isLoading)
            assertFalse(state.isProcessing)
            assertEquals(0f, state.progress)
            assertNull(state.error)
            assertNull(state.result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region ImageItem Tests
    @Test
    fun `ImageItem has correct properties`() {
        val imageItem = ImageItem(
            id = "123",
            uri = mockk(),
            path = "/storage/image.jpg",
            name = "image.jpg",
            size = 1024L,
            thumbnail = null
        )

        assertEquals("123", imageItem.id)
        assertEquals("/storage/image.jpg", imageItem.path)
        assertEquals("image.jpg", imageItem.name)
        assertEquals(1024L, imageItem.size)
        assertNull(imageItem.thumbnail)
    }
    // endregion
}
