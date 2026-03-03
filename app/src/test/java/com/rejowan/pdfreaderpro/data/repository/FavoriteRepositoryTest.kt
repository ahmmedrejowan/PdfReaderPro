package com.rejowan.pdfreaderpro.data.repository

import android.net.Uri
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.database.dao.FavoriteDao
import com.rejowan.pdfreaderpro.data.local.database.entity.FavoriteEntity
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryTest {

    private lateinit var favoriteDao: FavoriteDao
    private lateinit var repository: FavoriteRepositoryImpl
    private val mockUri: Uri = mockk(relaxed = true)

    @Before
    fun setup() {
        favoriteDao = mockk(relaxed = true)
        repository = FavoriteRepositoryImpl(favoriteDao)

        // Mock Uri.parse to return a mock Uri
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
    }

    @After
    fun teardown() {
        unmockkStatic(Uri::class)
    }

    // region Test Data
    private fun createPdfFile(
        id: Long = 1L,
        name: String = "test.pdf",
        path: String = "/storage/test.pdf",
        size: Long = 1024L,
        dateModified: Long = System.currentTimeMillis(),
        parentFolder: String = "/storage"
    ) = PdfFile(
        id = id,
        name = name,
        path = path,
        uri = mockUri,
        size = size,
        dateModified = dateModified,
        dateAdded = dateModified,
        parentFolder = parentFolder
    )

    private fun createFavoriteEntity(
        id: Long = 1L,
        name: String = "test.pdf",
        path: String = "/storage/test.pdf",
        size: Long = 1024L,
        dateModified: Long = System.currentTimeMillis(),
        parentFolder: String = "/storage"
    ) = FavoriteEntity(
        id = id,
        name = name,
        path = path,
        size = size,
        dateModified = dateModified,
        parentFolder = parentFolder
    )
    // endregion

    // region getFavorites Tests
    @Test
    fun `getFavorites returns mapped PdfFile list`() = runTest {
        val entities = listOf(
            createFavoriteEntity(name = "file1.pdf", path = "/storage/file1.pdf"),
            createFavoriteEntity(name = "file2.pdf", path = "/storage/file2.pdf")
        )
        every { favoriteDao.getAllFavorites() } returns flowOf(entities)

        repository.getFavorites().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("file1.pdf", result[0].name)
            assertEquals("file2.pdf", result[1].name)
            assertTrue(result[0].isFavorite)
            assertTrue(result[1].isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFavorites returns empty list when no favorites`() = runTest {
        every { favoriteDao.getAllFavorites() } returns flowOf(emptyList())

        repository.getFavorites().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFavorites emits single list`() = runTest {
        val entity1 = createFavoriteEntity(name = "file1.pdf")
        every { favoriteDao.getAllFavorites() } returns flowOf(listOf(entity1))

        repository.getFavorites().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region isFavoriteFlow Tests
    @Test
    fun `isFavoriteFlow returns true when file is favorite`() = runTest {
        val path = "/storage/test.pdf"
        every { favoriteDao.isFavoriteFlow(path) } returns flowOf(true)

        repository.isFavoriteFlow(path).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFavoriteFlow returns false when file is not favorite`() = runTest {
        val path = "/storage/test.pdf"
        every { favoriteDao.isFavoriteFlow(path) } returns flowOf(false)

        repository.isFavoriteFlow(path).test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region addFavorite Tests
    @Test
    fun `addFavorite inserts entity with correct data`() = runTest {
        val pdfFile = createPdfFile()
        val entitySlot = slot<FavoriteEntity>()
        coEvery { favoriteDao.insert(capture(entitySlot)) } returns Unit

        repository.addFavorite(pdfFile)

        coVerify { favoriteDao.insert(any()) }
        assertEquals(pdfFile.name, entitySlot.captured.name)
        assertEquals(pdfFile.path, entitySlot.captured.path)
        assertEquals(pdfFile.size, entitySlot.captured.size)
        assertEquals(pdfFile.dateModified, entitySlot.captured.dateModified)
        assertEquals(pdfFile.parentFolder, entitySlot.captured.parentFolder)
    }

    @Test
    fun `addFavorite handles special characters in path`() = runTest {
        val pdfFile = createPdfFile(path = "/storage/测试/文件.pdf", name = "文件.pdf")
        val entitySlot = slot<FavoriteEntity>()
        coEvery { favoriteDao.insert(capture(entitySlot)) } returns Unit

        repository.addFavorite(pdfFile)

        assertEquals("/storage/测试/文件.pdf", entitySlot.captured.path)
        assertEquals("文件.pdf", entitySlot.captured.name)
    }
    // endregion

    // region removeFavorite Tests
    @Test
    fun `removeFavorite calls deleteByPath with correct path`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { favoriteDao.deleteByPath(path) } returns Unit

        repository.removeFavorite(path)

        coVerify { favoriteDao.deleteByPath(path) }
    }

    @Test
    fun `removeFavorite handles non-existent path gracefully`() = runTest {
        val path = "/nonexistent/path.pdf"
        coEvery { favoriteDao.deleteByPath(path) } returns Unit

        repository.removeFavorite(path)

        coVerify { favoriteDao.deleteByPath(path) }
    }
    // endregion

    // region isFavorite Tests
    @Test
    fun `isFavorite returns true when file exists in favorites`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { favoriteDao.isFavorite(path) } returns true

        val result = repository.isFavorite(path)

        assertTrue(result)
    }

    @Test
    fun `isFavorite returns false when file does not exist in favorites`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { favoriteDao.isFavorite(path) } returns false

        val result = repository.isFavorite(path)

        assertFalse(result)
    }
    // endregion

    // region toggleFavorite Tests
    @Test
    fun `toggleFavorite adds favorite when not already favorite`() = runTest {
        val pdfFile = createPdfFile()
        coEvery { favoriteDao.isFavorite(pdfFile.path) } returns false
        coEvery { favoriteDao.insert(any()) } returns Unit

        repository.toggleFavorite(pdfFile)

        coVerify { favoriteDao.insert(any()) }
        coVerify(exactly = 0) { favoriteDao.deleteByPath(any()) }
    }

    @Test
    fun `toggleFavorite removes favorite when already favorite`() = runTest {
        val pdfFile = createPdfFile()
        coEvery { favoriteDao.isFavorite(pdfFile.path) } returns true
        coEvery { favoriteDao.deleteByPath(pdfFile.path) } returns Unit

        repository.toggleFavorite(pdfFile)

        coVerify { favoriteDao.deleteByPath(pdfFile.path) }
        coVerify(exactly = 0) { favoriteDao.insert(any()) }
    }

    @Test
    fun `toggleFavorite checks isFavorite before action`() = runTest {
        val pdfFile = createPdfFile()
        coEvery { favoriteDao.isFavorite(pdfFile.path) } returns false
        coEvery { favoriteDao.insert(any()) } returns Unit

        repository.toggleFavorite(pdfFile)

        coVerify(exactly = 1) { favoriteDao.isFavorite(pdfFile.path) }
    }
    // endregion

    // region clearAllFavorites Tests
    @Test
    fun `clearAllFavorites calls dao clearAll`() = runTest {
        coEvery { favoriteDao.clearAll() } returns Unit

        repository.clearAllFavorites()

        coVerify { favoriteDao.clearAll() }
    }
    // endregion

    // region getFavoriteCount Tests
    @Test
    fun `getFavoriteCount returns count from dao`() = runTest {
        coEvery { favoriteDao.getCount() } returns 5

        val result = repository.getFavoriteCount()

        assertEquals(5, result)
    }

    @Test
    fun `getFavoriteCount returns zero when empty`() = runTest {
        coEvery { favoriteDao.getCount() } returns 0

        val result = repository.getFavoriteCount()

        assertEquals(0, result)
    }
    // endregion

    // region Edge Cases
    @Test
    fun `addFavorite with zero size works correctly`() = runTest {
        val pdfFile = createPdfFile(size = 0L)
        val entitySlot = slot<FavoriteEntity>()
        coEvery { favoriteDao.insert(capture(entitySlot)) } returns Unit

        repository.addFavorite(pdfFile)

        assertEquals(0L, entitySlot.captured.size)
    }

    @Test
    fun `addFavorite with empty parent folder works correctly`() = runTest {
        val pdfFile = createPdfFile(parentFolder = "")
        val entitySlot = slot<FavoriteEntity>()
        coEvery { favoriteDao.insert(capture(entitySlot)) } returns Unit

        repository.addFavorite(pdfFile)

        assertEquals("", entitySlot.captured.parentFolder)
    }
    // endregion
}
