package com.rejowan.pdfreaderpro.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.database.PdfDatabase
import com.rejowan.pdfreaderpro.data.local.database.dao.RecentDao
import com.rejowan.pdfreaderpro.data.local.database.entity.RecentEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentDaoTest {

    private lateinit var database: PdfDatabase
    private lateinit var recentDao: RecentDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PdfDatabase::class.java
        ).allowMainThreadQueries().build()

        recentDao = database.recentDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // region Test Data
    private fun createRecent(
        name: String = "test.pdf",
        path: String = "/storage/test.pdf",
        size: Long = 1024L,
        lastOpened: Long = System.currentTimeMillis(),
        totalPages: Int = 10,
        lastPage: Int = 0
    ) = RecentEntity(
        name = name,
        path = path,
        size = size,
        lastOpened = lastOpened,
        totalPages = totalPages,
        lastPage = lastPage
    )
    // endregion

    // region Upsert Tests
    @Test
    fun upsert_newRecent_insertsSuccessfully() = runTest {
        val recent = createRecent()

        recentDao.upsert(recent)
        val result = recentDao.getByPath(recent.path)

        assertNotNull(result)
        assertEquals(recent.name, result?.name)
        assertEquals(recent.path, result?.path)
    }

    @Test
    fun upsert_existingPath_updatesRecord() = runTest {
        val recent1 = createRecent(name = "original.pdf", lastPage = 5)
        val recent2 = createRecent(name = "updated.pdf", lastPage = 10)

        recentDao.upsert(recent1)
        recentDao.upsert(recent2)

        val result = recentDao.getByPath(recent1.path)
        assertEquals("updated.pdf", result?.name)
        assertEquals(10, result?.lastPage)
        assertEquals(1, recentDao.getCount())
    }

    @Test
    fun upsert_multipleRecents_insertsAll() = runTest {
        recentDao.upsert(createRecent(path = "/storage/test1.pdf"))
        recentDao.upsert(createRecent(path = "/storage/test2.pdf"))
        recentDao.upsert(createRecent(path = "/storage/test3.pdf"))

        assertEquals(3, recentDao.getCount())
    }
    // endregion

    // region UpdateLastPage Tests
    @Test
    fun updateLastPage_existingPath_updatesPage() = runTest {
        val recent = createRecent(lastPage = 0)
        recentDao.upsert(recent)

        recentDao.updateLastPage(recent.path, 15)

        val result = recentDao.getByPath(recent.path)
        assertEquals(15, result?.lastPage)
    }

    @Test
    fun updateLastPage_existingPath_updatesTimestamp() = runTest {
        val oldTimestamp = System.currentTimeMillis() - 10000
        val recent = createRecent(lastOpened = oldTimestamp)
        recentDao.upsert(recent)

        recentDao.updateLastPage(recent.path, 5)

        val result = recentDao.getByPath(recent.path)
        assertTrue(result!!.lastOpened > oldTimestamp)
    }

    @Test
    fun updateLastPage_nonExistentPath_doesNothing() = runTest {
        recentDao.updateLastPage("/nonexistent/path.pdf", 5)

        assertNull(recentDao.getByPath("/nonexistent/path.pdf"))
    }

    @Test
    fun updateLastPage_preservesOtherFields() = runTest {
        val recent = createRecent(
            name = "test.pdf",
            totalPages = 100,
            size = 5000L
        )
        recentDao.upsert(recent)

        recentDao.updateLastPage(recent.path, 50)

        val result = recentDao.getByPath(recent.path)
        assertEquals("test.pdf", result?.name)
        assertEquals(100, result?.totalPages)
        assertEquals(5000L, result?.size)
        assertEquals(50, result?.lastPage)
    }
    // endregion

    // region Delete Tests
    @Test
    fun delete_existingRecent_removesFromDatabase() = runTest {
        val recent = createRecent()
        recentDao.upsert(recent)
        val inserted = recentDao.getByPath(recent.path)!!

        recentDao.delete(inserted)

        assertNull(recentDao.getByPath(recent.path))
    }

    @Test
    fun deleteByPath_existingPath_removesFromDatabase() = runTest {
        val recent = createRecent()
        recentDao.upsert(recent)

        recentDao.deleteByPath(recent.path)

        assertNull(recentDao.getByPath(recent.path))
        assertEquals(0, recentDao.getCount())
    }

    @Test
    fun deleteByPath_nonExistentPath_doesNothing() = runTest {
        val recent = createRecent()
        recentDao.upsert(recent)

        recentDao.deleteByPath("/nonexistent/path.pdf")

        assertEquals(1, recentDao.getCount())
    }

    @Test
    fun clearAll_withMultipleRecents_removesAll() = runTest {
        recentDao.upsert(createRecent(path = "/storage/test1.pdf"))
        recentDao.upsert(createRecent(path = "/storage/test2.pdf"))
        recentDao.upsert(createRecent(path = "/storage/test3.pdf"))

        recentDao.clearAll()

        assertEquals(0, recentDao.getCount())
    }
    // endregion

    // region Query Tests
    @Test
    fun getByPath_existingPath_returnsRecent() = runTest {
        val recent = createRecent()
        recentDao.upsert(recent)

        val result = recentDao.getByPath(recent.path)

        assertNotNull(result)
        assertEquals(recent.path, result?.path)
    }

    @Test
    fun getByPath_nonExistentPath_returnsNull() = runTest {
        val result = recentDao.getByPath("/nonexistent/path.pdf")

        assertNull(result)
    }

    @Test
    fun getCount_emptyDatabase_returnsZero() = runTest {
        val count = recentDao.getCount()

        assertEquals(0, count)
    }

    @Test
    fun getCount_withRecents_returnsCorrectCount() = runTest {
        recentDao.upsert(createRecent(path = "/storage/test1.pdf"))
        recentDao.upsert(createRecent(path = "/storage/test2.pdf"))

        val count = recentDao.getCount()

        assertEquals(2, count)
    }
    // endregion

    // region Flow Tests
    @Test
    fun getAllRecent_emptyDatabase_emitsEmptyList() = runTest {
        recentDao.getAllRecent().test {
            assertEquals(emptyList<RecentEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecent_withRecents_emitsListOrderedByLastOpenedDesc() = runTest {
        val now = System.currentTimeMillis()
        recentDao.upsert(createRecent(path = "/storage/old.pdf", lastOpened = now - 2000))
        recentDao.upsert(createRecent(path = "/storage/newest.pdf", lastOpened = now))
        recentDao.upsert(createRecent(path = "/storage/middle.pdf", lastOpened = now - 1000))

        recentDao.getAllRecent().test {
            val recents = awaitItem()
            assertEquals(3, recents.size)
            assertEquals("/storage/newest.pdf", recents[0].path)
            assertEquals("/storage/middle.pdf", recents[1].path)
            assertEquals("/storage/old.pdf", recents[2].path)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecent_updatesOnInsert() = runTest {
        recentDao.getAllRecent().test {
            // Initial empty list
            assertEquals(emptyList<RecentEntity>(), awaitItem())

            // Insert recent
            recentDao.upsert(createRecent())

            // Should emit updated list
            val updatedList = awaitItem()
            assertEquals(1, updatedList.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecent_updatesOnDelete() = runTest {
        val recent = createRecent()
        recentDao.upsert(recent)

        recentDao.getAllRecent().test {
            // Initial list with one item
            assertEquals(1, awaitItem().size)

            // Delete recent
            recentDao.deleteByPath(recent.path)

            // Should emit empty list
            assertEquals(emptyList<RecentEntity>(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllRecent_updatesOnUpsert() = runTest {
        val now = System.currentTimeMillis()
        recentDao.upsert(createRecent(path = "/storage/file1.pdf", name = "file1.pdf", lastOpened = now - 1000))
        recentDao.upsert(createRecent(path = "/storage/file2.pdf", name = "file2.pdf", lastOpened = now - 2000))

        recentDao.getAllRecent().test {
            // Initial order: file1, file2
            val initial = awaitItem()
            assertEquals("file1.pdf", initial[0].name)

            // Re-open file2 (upsert with newer timestamp)
            recentDao.upsert(createRecent(path = "/storage/file2.pdf", name = "file2.pdf", lastOpened = now))

            // New order: file2, file1
            val updated = awaitItem()
            assertEquals("file2.pdf", updated[0].name)
            assertEquals("file1.pdf", updated[1].name)

            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Edge Cases
    @Test
    fun upsert_zeroPages_insertsSuccessfully() = runTest {
        val recent = createRecent(totalPages = 0, lastPage = 0)

        recentDao.upsert(recent)

        val result = recentDao.getByPath(recent.path)
        assertEquals(0, result?.totalPages)
    }

    @Test
    fun upsert_largePageCount_insertsSuccessfully() = runTest {
        val recent = createRecent(totalPages = 100000, lastPage = 50000)

        recentDao.upsert(recent)

        val result = recentDao.getByPath(recent.path)
        assertEquals(100000, result?.totalPages)
        assertEquals(50000, result?.lastPage)
    }

    @Test
    fun upsert_specialCharactersInPath_insertsSuccessfully() = runTest {
        val specialPath = "/storage/文档/документ/test (1).pdf"
        val recent = createRecent(path = specialPath)

        recentDao.upsert(recent)

        val result = recentDao.getByPath(specialPath)
        assertEquals(specialPath, result?.path)
    }

    @Test
    fun upsert_veryLongPath_insertsSuccessfully() = runTest {
        val longPath = "/storage/" + "a".repeat(500) + ".pdf"
        val recent = createRecent(path = longPath)

        recentDao.upsert(recent)

        val result = recentDao.getByPath(longPath)
        assertNotNull(result)
    }

    @Test
    fun upsert_zeroSize_insertsSuccessfully() = runTest {
        val recent = createRecent(size = 0L)

        recentDao.upsert(recent)

        val result = recentDao.getByPath(recent.path)
        assertEquals(0L, result?.size)
    }

    @Test
    fun upsert_maxLongSize_insertsSuccessfully() = runTest {
        val recent = createRecent(size = Long.MAX_VALUE)

        recentDao.upsert(recent)

        val result = recentDao.getByPath(recent.path)
        assertEquals(Long.MAX_VALUE, result?.size)
    }

    @Test
    fun updateLastPage_lastPageEqualsTotal_succeeds() = runTest {
        val recent = createRecent(totalPages = 10, lastPage = 0)
        recentDao.upsert(recent)

        recentDao.updateLastPage(recent.path, 9) // 0-indexed, last page of 10

        val result = recentDao.getByPath(recent.path)
        assertEquals(9, result?.lastPage)
    }

    @Test
    fun getAllRecent_manyItems_returnsAll() = runTest {
        val now = System.currentTimeMillis()
        repeat(100) { i ->
            recentDao.upsert(createRecent(path = "/storage/test$i.pdf", lastOpened = now - i))
        }

        recentDao.getAllRecent().test {
            val recents = awaitItem()
            assertEquals(100, recents.size)
            // First should be most recent (index 0)
            assertEquals("/storage/test0.pdf", recents[0].path)
            // Last should be oldest (index 99)
            assertEquals("/storage/test99.pdf", recents[99].path)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Unique Path Constraint Tests
    @Test
    fun upsert_duplicatePath_replacesInsteadOfDuplicate() = runTest {
        val path = "/storage/test.pdf"

        // Insert first entry
        recentDao.upsert(createRecent(path = path, name = "First"))
        assertEquals(1, recentDao.getCount())

        // Insert second entry with same path
        recentDao.upsert(createRecent(path = path, name = "Second"))
        assertEquals(1, recentDao.getCount())

        // Should have the latest name
        val result = recentDao.getByPath(path)
        assertEquals("Second", result?.name)
    }
    // endregion
}
