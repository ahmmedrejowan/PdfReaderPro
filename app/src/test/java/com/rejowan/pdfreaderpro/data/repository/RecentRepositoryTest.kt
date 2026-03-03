package com.rejowan.pdfreaderpro.data.repository

import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.database.dao.RecentDao
import com.rejowan.pdfreaderpro.data.local.database.entity.RecentEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecentRepositoryTest {

    private lateinit var recentDao: RecentDao
    private lateinit var repository: RecentRepositoryImpl

    @Before
    fun setup() {
        recentDao = mockk(relaxed = true)
        repository = RecentRepositoryImpl(recentDao)
    }

    // region Test Data
    private fun createRecentEntity(
        id: Long = 1L,
        name: String = "test.pdf",
        path: String = "/storage/test.pdf",
        size: Long = 1024L,
        lastOpened: Long = System.currentTimeMillis(),
        totalPages: Int = 10,
        lastPage: Int = 0
    ) = RecentEntity(
        id = id,
        name = name,
        path = path,
        size = size,
        lastOpened = lastOpened,
        totalPages = totalPages,
        lastPage = lastPage
    )
    // endregion

    // region getRecentFiles Tests
    @Test
    fun `getRecentFiles returns mapped RecentFile list`() = runTest {
        val entities = listOf(
            createRecentEntity(name = "file1.pdf", path = "/storage/file1.pdf"),
            createRecentEntity(name = "file2.pdf", path = "/storage/file2.pdf")
        )
        every { recentDao.getAllRecent() } returns flowOf(entities)

        repository.getRecentFiles().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("file1.pdf", result[0].name)
            assertEquals("file2.pdf", result[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecentFiles returns empty list when no recents`() = runTest {
        every { recentDao.getAllRecent() } returns flowOf(emptyList())

        repository.getRecentFiles().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecentFiles maps all fields correctly`() = runTest {
        val entity = createRecentEntity(
            id = 5L,
            name = "test.pdf",
            path = "/storage/test.pdf",
            size = 2048L,
            lastOpened = 1234567890L,
            totalPages = 50,
            lastPage = 25
        )
        every { recentDao.getAllRecent() } returns flowOf(listOf(entity))

        repository.getRecentFiles().test {
            val result = awaitItem()[0]
            assertEquals(5L, result.id)
            assertEquals("test.pdf", result.name)
            assertEquals("/storage/test.pdf", result.path)
            assertEquals(2048L, result.size)
            assertEquals(1234567890L, result.lastOpened)
            assertEquals(50, result.totalPages)
            assertEquals(25, result.lastPage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getRecentFiles emits updates when recents change`() = runTest {
        val entity = createRecentEntity()
        every { recentDao.getAllRecent() } returns flowOf(
            emptyList(),
            listOf(entity)
        )

        repository.getRecentFiles().test {
            assertTrue(awaitItem().isEmpty())
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region addOrUpdateRecent Tests
    @Test
    fun `addOrUpdateRecent calls dao upsert with correct data`() = runTest {
        val entitySlot = slot<RecentEntity>()
        coEvery { recentDao.upsert(capture(entitySlot)) } returns Unit

        repository.addOrUpdateRecent(
            path = "/storage/test.pdf",
            name = "test.pdf",
            size = 2048L,
            totalPages = 100,
            currentPage = 50
        )

        coVerify { recentDao.upsert(any()) }
        assertEquals("/storage/test.pdf", entitySlot.captured.path)
        assertEquals("test.pdf", entitySlot.captured.name)
        assertEquals(2048L, entitySlot.captured.size)
        assertEquals(100, entitySlot.captured.totalPages)
        assertEquals(50, entitySlot.captured.lastPage)
    }

    @Test
    fun `addOrUpdateRecent sets current timestamp`() = runTest {
        val entitySlot = slot<RecentEntity>()
        val beforeTime = System.currentTimeMillis()
        coEvery { recentDao.upsert(capture(entitySlot)) } returns Unit

        repository.addOrUpdateRecent(
            path = "/storage/test.pdf",
            name = "test.pdf",
            size = 1024L,
            totalPages = 10,
            currentPage = 0
        )

        val afterTime = System.currentTimeMillis()
        assertTrue(entitySlot.captured.lastOpened >= beforeTime)
        assertTrue(entitySlot.captured.lastOpened <= afterTime)
    }

    @Test
    fun `addOrUpdateRecent handles special characters in path`() = runTest {
        val entitySlot = slot<RecentEntity>()
        coEvery { recentDao.upsert(capture(entitySlot)) } returns Unit

        repository.addOrUpdateRecent(
            path = "/storage/测试/文件.pdf",
            name = "文件.pdf",
            size = 1024L,
            totalPages = 10,
            currentPage = 0
        )

        assertEquals("/storage/测试/文件.pdf", entitySlot.captured.path)
        assertEquals("文件.pdf", entitySlot.captured.name)
    }

    @Test
    fun `addOrUpdateRecent handles zero values`() = runTest {
        val entitySlot = slot<RecentEntity>()
        coEvery { recentDao.upsert(capture(entitySlot)) } returns Unit

        repository.addOrUpdateRecent(
            path = "/storage/test.pdf",
            name = "test.pdf",
            size = 0L,
            totalPages = 0,
            currentPage = 0
        )

        assertEquals(0L, entitySlot.captured.size)
        assertEquals(0, entitySlot.captured.totalPages)
        assertEquals(0, entitySlot.captured.lastPage)
    }
    // endregion

    // region updateLastPage Tests
    @Test
    fun `updateLastPage calls dao updateLastPage with correct params`() = runTest {
        val path = "/storage/test.pdf"
        val page = 25
        coEvery { recentDao.updateLastPage(path, page, any()) } returns Unit

        repository.updateLastPage(path, page)

        coVerify { recentDao.updateLastPage(path, page, any()) }
    }

    @Test
    fun `updateLastPage handles page zero`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { recentDao.updateLastPage(path, 0, any()) } returns Unit

        repository.updateLastPage(path, 0)

        coVerify { recentDao.updateLastPage(path, 0, any()) }
    }

    @Test
    fun `updateLastPage handles large page numbers`() = runTest {
        val path = "/storage/test.pdf"
        val page = 10000
        coEvery { recentDao.updateLastPage(path, page, any()) } returns Unit

        repository.updateLastPage(path, page)

        coVerify { recentDao.updateLastPage(path, page, any()) }
    }
    // endregion

    // region getLastPage Tests
    @Test
    fun `getLastPage returns page when path exists`() = runTest {
        val path = "/storage/test.pdf"
        val entity = createRecentEntity(path = path, lastPage = 42)
        coEvery { recentDao.getByPath(path) } returns entity

        val result = repository.getLastPage(path)

        assertEquals(42, result)
    }

    @Test
    fun `getLastPage returns null when path does not exist`() = runTest {
        val path = "/storage/nonexistent.pdf"
        coEvery { recentDao.getByPath(path) } returns null

        val result = repository.getLastPage(path)

        assertNull(result)
    }

    @Test
    fun `getLastPage returns zero when last page is zero`() = runTest {
        val path = "/storage/test.pdf"
        val entity = createRecentEntity(path = path, lastPage = 0)
        coEvery { recentDao.getByPath(path) } returns entity

        val result = repository.getLastPage(path)

        assertEquals(0, result)
    }
    // endregion

    // region removeRecent Tests
    @Test
    fun `removeRecent calls dao deleteByPath`() = runTest {
        val path = "/storage/test.pdf"
        coEvery { recentDao.deleteByPath(path) } returns Unit

        repository.removeRecent(path)

        coVerify { recentDao.deleteByPath(path) }
    }

    @Test
    fun `removeRecent handles non-existent path gracefully`() = runTest {
        val path = "/nonexistent/path.pdf"
        coEvery { recentDao.deleteByPath(path) } returns Unit

        repository.removeRecent(path)

        coVerify { recentDao.deleteByPath(path) }
    }
    // endregion

    // region clearAllRecent Tests
    @Test
    fun `clearAllRecent calls dao clearAll`() = runTest {
        coEvery { recentDao.clearAll() } returns Unit

        repository.clearAllRecent()

        coVerify { recentDao.clearAll() }
    }
    // endregion

    // region getRecentCount Tests
    @Test
    fun `getRecentCount returns count from dao`() = runTest {
        coEvery { recentDao.getCount() } returns 15

        val result = repository.getRecentCount()

        assertEquals(15, result)
    }

    @Test
    fun `getRecentCount returns zero when empty`() = runTest {
        coEvery { recentDao.getCount() } returns 0

        val result = repository.getRecentCount()

        assertEquals(0, result)
    }

    @Test
    fun `getRecentCount handles large counts`() = runTest {
        coEvery { recentDao.getCount() } returns 100000

        val result = repository.getRecentCount()

        assertEquals(100000, result)
    }
    // endregion

    // region RecentFile Domain Model Tests
    @Test
    fun `RecentFile calculates progress correctly`() = runTest {
        val entity = createRecentEntity(totalPages = 100, lastPage = 49) // 50th page (0-indexed)
        every { recentDao.getAllRecent() } returns flowOf(listOf(entity))

        repository.getRecentFiles().test {
            val result = awaitItem()[0]
            // Progress = (lastPage + 1) / totalPages = 50 / 100 = 0.5
            assertEquals(0.5f, result.progress, 0.01f)
            assertEquals(50, result.progressPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `RecentFile handles zero total pages`() = runTest {
        val entity = createRecentEntity(totalPages = 0, lastPage = 0)
        every { recentDao.getAllRecent() } returns flowOf(listOf(entity))

        repository.getRecentFiles().test {
            val result = awaitItem()[0]
            // Should return 0 when totalPages is 0
            assertEquals(0f, result.progress, 0.01f)
            assertEquals(0, result.progressPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `RecentFile calculates 100 percent at last page`() = runTest {
        val entity = createRecentEntity(totalPages = 10, lastPage = 9) // Last page (0-indexed)
        every { recentDao.getAllRecent() } returns flowOf(listOf(entity))

        repository.getRecentFiles().test {
            val result = awaitItem()[0]
            // Progress = (9 + 1) / 10 = 1.0
            assertEquals(1.0f, result.progress, 0.01f)
            assertEquals(100, result.progressPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion
}
