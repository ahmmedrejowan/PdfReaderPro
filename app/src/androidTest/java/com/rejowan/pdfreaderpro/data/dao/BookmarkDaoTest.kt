package com.rejowan.pdfreaderpro.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.database.PdfDatabase
import com.rejowan.pdfreaderpro.data.local.database.dao.BookmarkDao
import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookmarkDaoTest {

    private lateinit var database: PdfDatabase
    private lateinit var bookmarkDao: BookmarkDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PdfDatabase::class.java
        ).allowMainThreadQueries().build()

        bookmarkDao = database.bookmarkDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // region Test Data
    private fun createBookmark(
        id: Long = 0,
        pdfPath: String = "/storage/test.pdf",
        pageNumber: Int = 0,
        title: String? = "Page 1",
        createdAt: Long = System.currentTimeMillis()
    ) = BookmarkEntity(
        id = id,
        pdfPath = pdfPath,
        pageNumber = pageNumber,
        title = title,
        createdAt = createdAt
    )
    // endregion

    // region Insert Tests
    @Test
    fun insert_singleBookmark_insertsSuccessfully() = runTest {
        val bookmark = createBookmark()

        val id = bookmarkDao.insert(bookmark)

        assertTrue(id > 0)
        val result = bookmarkDao.getById(id)
        assertNotNull(result)
        assertEquals(bookmark.pdfPath, result?.pdfPath)
        assertEquals(bookmark.pageNumber, result?.pageNumber)
    }

    @Test
    fun insert_multipleBookmarksForSamePdf_insertsAll() = runTest {
        val pdfPath = "/storage/test.pdf"
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 0))
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 5))
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 10))

        bookmarkDao.getBookmarksForPdf(pdfPath).test {
            val bookmarks = awaitItem()
            assertEquals(3, bookmarks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insert_bookmarksForDifferentPdfs_insertsAll() = runTest {
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test1.pdf"))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test2.pdf"))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test3.pdf"))

        bookmarkDao.getAllBookmarks().test {
            val bookmarks = awaitItem()
            assertEquals(3, bookmarks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insert_duplicatePageSamePdf_replacesExisting() = runTest {
        val pdfPath = "/storage/test.pdf"
        val pageNumber = 5

        val id1 = bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = pageNumber, title = "Original"))
        val id2 = bookmarkDao.insert(createBookmark(id = id1, pdfPath = pdfPath, pageNumber = pageNumber, title = "Updated"))

        // Same ID means replacement
        assertEquals(id1, id2)
        val result = bookmarkDao.getById(id1)
        assertEquals("Updated", result?.title)
    }

    @Test
    fun insert_returnsGeneratedId() = runTest {
        val bookmark = createBookmark()

        val id = bookmarkDao.insert(bookmark)

        assertTrue(id > 0)
    }
    // endregion

    // region Update Tests
    @Test
    fun update_existingBookmark_updatesSuccessfully() = runTest {
        val bookmark = createBookmark(title = "Original Title")
        val id = bookmarkDao.insert(bookmark)
        val inserted = bookmarkDao.getById(id)!!

        val updated = inserted.copy(title = "Updated Title")
        bookmarkDao.update(updated)

        val result = bookmarkDao.getById(id)
        assertEquals("Updated Title", result?.title)
    }

    @Test
    fun update_changePageNumber_updatesSuccessfully() = runTest {
        val bookmark = createBookmark(pageNumber = 5)
        val id = bookmarkDao.insert(bookmark)
        val inserted = bookmarkDao.getById(id)!!

        val updated = inserted.copy(pageNumber = 10)
        bookmarkDao.update(updated)

        val result = bookmarkDao.getById(id)
        assertEquals(10, result?.pageNumber)
    }
    // endregion

    // region Delete Tests
    @Test
    fun delete_existingBookmark_removesFromDatabase() = runTest {
        val bookmark = createBookmark()
        val id = bookmarkDao.insert(bookmark)
        val inserted = bookmarkDao.getById(id)!!

        bookmarkDao.delete(inserted)

        assertNull(bookmarkDao.getById(id))
    }

    @Test
    fun deleteByPage_existingBookmark_removesFromDatabase() = runTest {
        val pdfPath = "/storage/test.pdf"
        val pageNumber = 5
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = pageNumber))

        bookmarkDao.deleteByPage(pdfPath, pageNumber)

        assertFalse(bookmarkDao.isPageBookmarked(pdfPath, pageNumber))
    }

    @Test
    fun deleteByPage_nonExistentPage_doesNothing() = runTest {
        val pdfPath = "/storage/test.pdf"
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 5))

        bookmarkDao.deleteByPage(pdfPath, 10)

        assertTrue(bookmarkDao.isPageBookmarked(pdfPath, 5))
    }

    @Test
    fun deleteAllForPdf_removesAllBookmarksForPdf() = runTest {
        val pdfPath = "/storage/test.pdf"
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 0))
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 5))
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 10))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/other.pdf", pageNumber = 0))

        bookmarkDao.deleteAllForPdf(pdfPath)

        bookmarkDao.getBookmarksForPdf(pdfPath).test {
            assertEquals(emptyList<BookmarkEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        // Other PDF should not be affected
        bookmarkDao.getBookmarksForPdf("/storage/other.pdf").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAll_removesAllBookmarks() = runTest {
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test1.pdf"))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test2.pdf"))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test3.pdf"))

        bookmarkDao.clearAll()

        bookmarkDao.getAllBookmarks().test {
            assertEquals(emptyList<BookmarkEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Query Tests
    @Test
    fun getById_existingId_returnsBookmark() = runTest {
        val bookmark = createBookmark()
        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)

        assertNotNull(result)
        assertEquals(bookmark.pdfPath, result?.pdfPath)
    }

    @Test
    fun getById_nonExistentId_returnsNull() = runTest {
        val result = bookmarkDao.getById(999L)

        assertNull(result)
    }

    @Test
    fun isPageBookmarked_bookmarkedPage_returnsTrue() = runTest {
        val pdfPath = "/storage/test.pdf"
        val pageNumber = 5
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = pageNumber))

        val result = bookmarkDao.isPageBookmarked(pdfPath, pageNumber)

        assertTrue(result)
    }

    @Test
    fun isPageBookmarked_notBookmarkedPage_returnsFalse() = runTest {
        val pdfPath = "/storage/test.pdf"
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 5))

        val result = bookmarkDao.isPageBookmarked(pdfPath, 10)

        assertFalse(result)
    }

    @Test
    fun isPageBookmarked_differentPdfSamePage_returnsFalse() = runTest {
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/test1.pdf", pageNumber = 5))

        val result = bookmarkDao.isPageBookmarked("/storage/test2.pdf", 5)

        assertFalse(result)
    }
    // endregion

    // region Flow Tests
    @Test
    fun getBookmarksForPdf_emptyDatabase_emitsEmptyList() = runTest {
        bookmarkDao.getBookmarksForPdf("/storage/test.pdf").test {
            assertEquals(emptyList<BookmarkEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBookmarksForPdf_withBookmarks_emitsListSortedByPageNumber() = runTest {
        val pdfPath = "/storage/test.pdf"
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 10))
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 0))
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = 5))

        bookmarkDao.getBookmarksForPdf(pdfPath).test {
            val bookmarks = awaitItem()
            assertEquals(3, bookmarks.size)
            assertEquals(0, bookmarks[0].pageNumber)
            assertEquals(5, bookmarks[1].pageNumber)
            assertEquals(10, bookmarks[2].pageNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBookmarksForPdf_updatesOnInsert() = runTest {
        val pdfPath = "/storage/test.pdf"

        bookmarkDao.getBookmarksForPdf(pdfPath).test {
            // Initially empty
            assertEquals(emptyList<BookmarkEntity>(), awaitItem())

            // Insert bookmark
            bookmarkDao.insert(createBookmark(pdfPath = pdfPath))

            // Should emit updated list
            assertEquals(1, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getBookmarksForPdf_updatesOnDelete() = runTest {
        val pdfPath = "/storage/test.pdf"
        val pageNumber = 5
        bookmarkDao.insert(createBookmark(pdfPath = pdfPath, pageNumber = pageNumber))

        bookmarkDao.getBookmarksForPdf(pdfPath).test {
            // Initially has one bookmark
            assertEquals(1, awaitItem().size)

            // Delete bookmark
            bookmarkDao.deleteByPage(pdfPath, pageNumber)

            // Should emit empty list
            assertEquals(emptyList<BookmarkEntity>(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllBookmarks_orderedByCreatedAtDesc() = runTest {
        val now = System.currentTimeMillis()
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/old.pdf", createdAt = now - 2000))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/newest.pdf", createdAt = now))
        bookmarkDao.insert(createBookmark(pdfPath = "/storage/middle.pdf", createdAt = now - 1000))

        bookmarkDao.getAllBookmarks().test {
            val bookmarks = awaitItem()
            assertEquals(3, bookmarks.size)
            assertEquals("/storage/newest.pdf", bookmarks[0].pdfPath)
            assertEquals("/storage/middle.pdf", bookmarks[1].pdfPath)
            assertEquals("/storage/old.pdf", bookmarks[2].pdfPath)
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Edge Cases
    @Test
    fun insert_nullTitle_insertsSuccessfully() = runTest {
        val bookmark = createBookmark(title = null)

        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)
        assertNull(result?.title)
    }

    @Test
    fun insert_pageNumberZero_insertsSuccessfully() = runTest {
        val bookmark = createBookmark(pageNumber = 0)

        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)
        assertEquals(0, result?.pageNumber)
    }

    @Test
    fun insert_largePageNumber_insertsSuccessfully() = runTest {
        val bookmark = createBookmark(pageNumber = 10000)

        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)
        assertEquals(10000, result?.pageNumber)
    }

    @Test
    fun insert_specialCharactersInPath_insertsSuccessfully() = runTest {
        val specialPath = "/storage/测试/тест/test (1).pdf"
        val bookmark = createBookmark(pdfPath = specialPath)

        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)
        assertEquals(specialPath, result?.pdfPath)
    }

    @Test
    fun insert_emptyTitle_insertsSuccessfully() = runTest {
        val bookmark = createBookmark(title = "")

        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)
        assertEquals("", result?.title)
    }

    @Test
    fun insert_longTitle_insertsSuccessfully() = runTest {
        val longTitle = "A".repeat(1000)
        val bookmark = createBookmark(title = longTitle)

        val id = bookmarkDao.insert(bookmark)

        val result = bookmarkDao.getById(id)
        assertEquals(longTitle, result?.title)
    }
    // endregion
}
