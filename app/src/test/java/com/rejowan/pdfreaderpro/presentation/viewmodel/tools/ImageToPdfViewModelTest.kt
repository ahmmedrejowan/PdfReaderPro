package com.rejowan.pdfreaderpro.presentation.viewmodel.tools

import android.app.Application
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.presentation.screens.tools.imagetopdf.ImageItem
import com.rejowan.pdfreaderpro.presentation.screens.tools.imagetopdf.ImageToPdfViewModel
import android.os.Environment
import io.mockk.every
import io.mockk.coVerify
import io.mockk.slot
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
class ImageToPdfViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfToolsRepository: PdfToolsRepository
    private lateinit var context: Application
    private lateinit var viewModel: ImageToPdfViewModel

    @get:Rule
    val folder = TemporaryFolder()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfToolsRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Adding images copies each one into the cache before it becomes an item in
        // the list, and that copy fails silently on a relaxed mock, so the list
        // stays empty and nothing about ordering can be tested. Environment is
        // mocked because the PDF is written into the public Documents folder.
        every { context.cacheDir } returns folder.newFolder("cache")
        every { context.contentResolver.openInputStream(any()) } answers {
            ByteArrayInputStream("pretend image bytes".toByteArray())
        }
        every { context.contentResolver.query(any(), any(), any(), any(), any()) } returns null
        mockkStatic(Environment::class)
        every {
            Environment.getExternalStoragePublicDirectory(any())
        } returns folder.newFolder("documents")
    }

    @After
    fun teardown() {
        unmockkStatic(Environment::class)
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
    fun `moveImage with valid indices on empty list`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // When the list is empty, moveImage may throw IndexOutOfBoundsException
        // or handle it gracefully depending on implementation.
        // This test just verifies the initial state is maintained.
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
        every { context.getString(R.string.error_add_one_image) } returns "Please add at least one image"
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

    /** Adds images and waits for them, since each is copied off the main thread. */
    private fun TestScope.addImages(vm: ImageToPdfViewModel, count: Int) {
        vm.addImages(List(count) { mockk<android.net.Uri>(relaxed = true) })
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.images.size == count) return
            Thread.sleep(10)
        }
        error("images never arrived")
    }

    /** Adds one more image and waits until the list reaches [total]. */
    private fun TestScope.addMoreImages(vm: ImageToPdfViewModel, total: Int) {
        vm.addImages(listOf(mockk<android.net.Uri>(relaxed = true)))
        repeat(200) {
            advanceUntilIdle()
            if (vm.state.value.images.size == total) return
            Thread.sleep(10)
        }
        error("images never arrived")
    }

    // region The order images go in
    @Test
    fun `added images appear in the list`() = runTest {
        val vm = createViewModel()
        addImages(vm, 3)
        assertEquals(3, vm.state.value.images.size)
    }

    @Test
    fun `adding more images keeps the ones already there`() = runTest {
        val vm = createViewModel()
        addImages(vm, 2)
        addMoreImages(vm, total = 3)

        assertEquals(3, vm.state.value.images.size)
    }

    @Test
    fun `removing an image leaves the others in order`() = runTest {
        val vm = createViewModel()
        addImages(vm, 3)
        val ids = vm.state.value.images.map { it.id }

        vm.removeImage(ids[1])

        assertEquals(listOf(ids[0], ids[2]), vm.state.value.images.map { it.id })
    }

    @Test
    fun `removing an id that is not there changes nothing`() = runTest {
        val vm = createViewModel()
        addImages(vm, 2)
        val before = vm.state.value.images.map { it.id }

        vm.removeImage("not-a-real-id")

        assertEquals(before, vm.state.value.images.map { it.id })
    }

    @Test
    fun `moving an image forward puts it where it was dropped`() = runTest {
        // The list order is the page order, so this is the whole point of the tool.
        val vm = createViewModel()
        addImages(vm, 3)
        val ids = vm.state.value.images.map { it.id }

        vm.moveImage(0, 2)

        assertEquals(listOf(ids[1], ids[2], ids[0]), vm.state.value.images.map { it.id })
    }

    @Test
    fun `moving an image backward puts it where it was dropped`() = runTest {
        val vm = createViewModel()
        addImages(vm, 3)
        val ids = vm.state.value.images.map { it.id }

        vm.moveImage(2, 0)

        assertEquals(listOf(ids[2], ids[0], ids[1]), vm.state.value.images.map { it.id })
    }

    @Test
    fun `moving an image onto itself changes nothing`() = runTest {
        val vm = createViewModel()
        addImages(vm, 3)
        val before = vm.state.value.images.map { it.id }

        vm.moveImage(1, 1)

        assertEquals(before, vm.state.value.images.map { it.id })
    }
    // endregion

    // region Converting
    @Test
    fun `without images it asks for some and converts nothing`() = runTest {
        val vm = createViewModel()
        vm.convertToPdf()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.imagesToPdf(any(), any(), any()) }
    }

    @Test
    fun `without an output name it asks for one and converts nothing`() = runTest {
        val vm = createViewModel()
        addImages(vm, 1)
        vm.setOutputFileName("")
        vm.convertToPdf()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify(exactly = 0) { pdfToolsRepository.imagesToPdf(any(), any(), any()) }
    }

    @Test
    fun `the images are converted in the order shown`() = runTest {
        val paths = slot<List<String>>()
        coEvery {
            pdfToolsRepository.imagesToPdf(capture(paths), any(), any())
        } returns Result.success(Unit)

        val vm = createViewModel()
        addImages(vm, 3)
        vm.moveImage(0, 2)
        val expected = vm.state.value.images.map { it.path }

        vm.convertToPdf()
        advanceUntilIdle()

        assertEquals(expected, paths.captured)
    }

    @Test
    fun `a failure is surfaced and processing stops`() = runTest {
        coEvery {
            pdfToolsRepository.imagesToPdf(any(), any(), any())
        } returns Result.failure(RuntimeException("bad image"))

        val vm = createViewModel()
        addImages(vm, 1)
        vm.convertToPdf()
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isProcessing)
    }
    // endregion
}
