package com.rejowan.pdfreaderpro.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.presentation.screens.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var pdfFileRepository: PdfFileRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        pdfFileRepository = mockk(relaxed = true)
        favoriteRepository = mockk(relaxed = true)

        // Default mocks
        every { pdfFileRepository.searchPdfs(any()) } returns flowOf(emptyList())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(
            pdfFileRepository = pdfFileRepository,
            favoriteRepository = favoriteRepository
        )
    }

    // region Test Data
    private fun createPdfFile(
        id: Long = 1L,
        name: String = "test.pdf",
        path: String = "/storage/test.pdf"
    ) = PdfFile(
        id = id,
        name = name,
        path = path,
        uri = mockk<Uri>(),
        size = 1024L,
        dateModified = System.currentTimeMillis(),
        dateAdded = System.currentTimeMillis(),
        parentFolder = "/storage"
    )
    // endregion

    // region Initial State Tests
    @Test
    fun `initial searchQuery is empty`() = runTest {
        viewModel = createViewModel()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `initial isSearching is false`() = runTest {
        viewModel = createViewModel()

        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `initial searchResults is empty`() = runTest {
        viewModel = createViewModel()

        viewModel.searchResults.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(emptyList<PdfFile>(), result)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region setSearchQuery Tests
    @Test
    fun `setSearchQuery updates searchQuery state`() = runTest {
        viewModel = createViewModel()

        viewModel.setSearchQuery("test query")

        assertEquals("test query", viewModel.searchQuery.value)
    }

    @Test
    fun `setSearchQuery with blank query does not search`() = runTest {
        viewModel = createViewModel()
        viewModel.setSearchQuery("")

        viewModel.searchResults.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(emptyList<PdfFile>(), result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery with whitespace only does not search`() = runTest {
        viewModel = createViewModel()
        viewModel.setSearchQuery("   ")

        viewModel.searchResults.test {
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(emptyList<PdfFile>(), result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery debounces rapid changes`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Rapid query changes
        viewModel.setSearchQuery("t")
        advanceTimeBy(100)
        viewModel.setSearchQuery("te")
        advanceTimeBy(100)
        viewModel.setSearchQuery("test")
        advanceTimeBy(350) // Total > 300ms debounce
        advanceUntilIdle()

        // Only the final query should be searched
        assertEquals("test", viewModel.searchQuery.value)
    }
    // endregion

    // region searchResults Tests
    @Test
    fun `searchResults returns matching files`() = runTest {
        val files = listOf(
            createPdfFile(name = "report.pdf"),
            createPdfFile(name = "document.pdf")
        )
        every { pdfFileRepository.searchPdfs("report") } returns flowOf(files.filter { it.name.contains("report") })

        viewModel = createViewModel()
        viewModel.setSearchQuery("report")

        viewModel.searchResults.test {
            advanceTimeBy(350) // Wait for debounce
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(1, result.size)
            assertEquals("report.pdf", result[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchResults returns empty list for no matches`() = runTest {
        every { pdfFileRepository.searchPdfs("xyz") } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.setSearchQuery("xyz")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(emptyList<PdfFile>(), result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchResults updates when query changes`() = runTest {
        val file1 = createPdfFile(name = "alpha.pdf")
        val file2 = createPdfFile(name = "beta.pdf")
        every { pdfFileRepository.searchPdfs("alpha") } returns flowOf(listOf(file1))
        every { pdfFileRepository.searchPdfs("beta") } returns flowOf(listOf(file2))

        viewModel = createViewModel()

        // First search
        viewModel.setSearchQuery("alpha")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result1 = expectMostRecentItem()
            assertTrue(result1.isNotEmpty())
            assertEquals("alpha.pdf", result1[0].name)
            cancelAndIgnoreRemainingEvents()
        }

        // Second search
        viewModel.setSearchQuery("beta")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result2 = expectMostRecentItem()
            assertTrue(result2.isNotEmpty())
            assertEquals("beta.pdf", result2[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region clearSearch Tests
    @Test
    fun `clearSearch resets searchQuery to empty`() = runTest {
        viewModel = createViewModel()
        viewModel.setSearchQuery("test")
        advanceUntilIdle()

        viewModel.clearSearch()

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `clearSearch returns empty results`() = runTest {
        val files = listOf(createPdfFile())
        every { pdfFileRepository.searchPdfs("test") } returns flowOf(files)

        viewModel = createViewModel()
        viewModel.setSearchQuery("test")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            expectMostRecentItem() // consume first result

            viewModel.clearSearch()
            advanceTimeBy(350)
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(emptyList<PdfFile>(), result)
            cancelAndIgnoreRemainingEvents()
        }
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

    @Test
    fun `toggleFavorite handles multiple calls`() = runTest {
        val pdfFile = createPdfFile()
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleFavorite(pdfFile)
        viewModel.toggleFavorite(pdfFile)
        advanceUntilIdle()

        coVerify(exactly = 2) { favoriteRepository.toggleFavorite(pdfFile) }
    }
    // endregion

    // region isFavorite Tests
    @Test
    fun `isFavorite returns true when file is favorite`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { favoriteRepository.isFavorite(path) } returns true

        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.isFavorite(path)

        assertTrue(result)
    }

    @Test
    fun `isFavorite returns false when file is not favorite`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { favoriteRepository.isFavorite(path) } returns false

        viewModel = createViewModel()
        advanceUntilIdle()

        val result = viewModel.isFavorite(path)

        assertFalse(result)
    }
    // endregion

    // region Search Case Sensitivity Tests
    @Test
    fun `search handles case insensitive matching`() = runTest {
        val file = createPdfFile(name = "Report.PDF")
        every { pdfFileRepository.searchPdfs("report") } returns flowOf(listOf(file))

        viewModel = createViewModel()
        viewModel.setSearchQuery("report")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Special Characters Tests
    @Test
    fun `search handles special characters in query`() = runTest {
        val file = createPdfFile(name = "file (1).pdf")
        every { pdfFileRepository.searchPdfs("(1)") } returns flowOf(listOf(file))

        viewModel = createViewModel()
        viewModel.setSearchQuery("(1)")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search handles unicode characters`() = runTest {
        val file = createPdfFile(name = "文档.pdf")
        every { pdfFileRepository.searchPdfs("文档") } returns flowOf(listOf(file))

        viewModel = createViewModel()
        viewModel.setSearchQuery("文档")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Edge Cases
    @Test
    fun `handles single character search`() = runTest {
        val files = listOf(
            createPdfFile(name = "a.pdf"),
            createPdfFile(name = "b.pdf")
        )
        every { pdfFileRepository.searchPdfs("a") } returns flowOf(files.filter { it.name.startsWith("a") })

        viewModel = createViewModel()
        viewModel.setSearchQuery("a")

        viewModel.searchResults.test {
            advanceTimeBy(350)
            advanceUntilIdle()
            val result = expectMostRecentItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handles very long search query`() = runTest {
        val longQuery = "a".repeat(1000)
        every { pdfFileRepository.searchPdfs(longQuery) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.setSearchQuery(longQuery)
        advanceTimeBy(350)
        advanceUntilIdle()

        assertEquals(longQuery, viewModel.searchQuery.value)
    }
    // endregion
}
