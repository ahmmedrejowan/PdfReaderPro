package com.rejowan.pdfreaderpro.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.database.PdfDatabase
import com.rejowan.pdfreaderpro.data.local.database.dao.FavoriteDao
import com.rejowan.pdfreaderpro.data.local.database.entity.FavoriteEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var database: PdfDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PdfDatabase::class.java
        ).allowMainThreadQueries().build()

        favoriteDao = database.favoriteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // region Test Data
    private fun createFavorite(
        name: String = "test.pdf",
        path: String = "/storage/test.pdf",
        size: Long = 1024L,
        dateModified: Long = System.currentTimeMillis(),
        parentFolder: String = "/storage"
    ) = FavoriteEntity(
        name = name,
        path = path,
        size = size,
        dateModified = dateModified,
        parentFolder = parentFolder
    )
    // endregion

    // region Insert Tests
    @Test
    fun insert_singleFavorite_insertsSuccessfully() = runTest {
        val favorite = createFavorite()

        favoriteDao.insert(favorite)
        val result = favoriteDao.getByPath(favorite.path)

        assertNotNull(result)
        assertEquals(favorite.name, result?.name)
        assertEquals(favorite.path, result?.path)
    }

    @Test
    fun insert_duplicatePath_replacesExisting() = runTest {
        val favorite1 = createFavorite(name = "original.pdf")
        val favorite2 = createFavorite(name = "updated.pdf")

        favoriteDao.insert(favorite1)
        favoriteDao.insert(favorite2)

        val result = favoriteDao.getByPath(favorite1.path)
        assertEquals("updated.pdf", result?.name)
        assertEquals(1, favoriteDao.getCount())
    }

    @Test
    fun insert_multipleFavorites_insertsAll() = runTest {
        val favorite1 = createFavorite(path = "/storage/test1.pdf", name = "test1.pdf")
        val favorite2 = createFavorite(path = "/storage/test2.pdf", name = "test2.pdf")
        val favorite3 = createFavorite(path = "/storage/test3.pdf", name = "test3.pdf")

        favoriteDao.insert(favorite1)
        favoriteDao.insert(favorite2)
        favoriteDao.insert(favorite3)

        assertEquals(3, favoriteDao.getCount())
    }
    // endregion

    // region Delete Tests
    @Test
    fun delete_existingFavorite_removesFromDatabase() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)
        val inserted = favoriteDao.getByPath(favorite.path)!!

        favoriteDao.delete(inserted)

        assertNull(favoriteDao.getByPath(favorite.path))
    }

    @Test
    fun deleteByPath_existingPath_removesFromDatabase() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)

        favoriteDao.deleteByPath(favorite.path)

        assertNull(favoriteDao.getByPath(favorite.path))
        assertEquals(0, favoriteDao.getCount())
    }

    @Test
    fun deleteByPath_nonExistentPath_doesNothing() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)

        favoriteDao.deleteByPath("/nonexistent/path.pdf")

        assertEquals(1, favoriteDao.getCount())
    }

    @Test
    fun clearAll_withMultipleFavorites_removesAll() = runTest {
        favoriteDao.insert(createFavorite(path = "/storage/test1.pdf"))
        favoriteDao.insert(createFavorite(path = "/storage/test2.pdf"))
        favoriteDao.insert(createFavorite(path = "/storage/test3.pdf"))

        favoriteDao.clearAll()

        assertEquals(0, favoriteDao.getCount())
    }
    // endregion

    // region Query Tests
    @Test
    fun getByPath_existingPath_returnsFavorite() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)

        val result = favoriteDao.getByPath(favorite.path)

        assertNotNull(result)
        assertEquals(favorite.path, result?.path)
    }

    @Test
    fun getByPath_nonExistentPath_returnsNull() = runTest {
        val result = favoriteDao.getByPath("/nonexistent/path.pdf")

        assertNull(result)
    }

    @Test
    fun isFavorite_existingPath_returnsTrue() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)

        val result = favoriteDao.isFavorite(favorite.path)

        assertTrue(result)
    }

    @Test
    fun isFavorite_nonExistentPath_returnsFalse() = runTest {
        val result = favoriteDao.isFavorite("/nonexistent/path.pdf")

        assertFalse(result)
    }

    @Test
    fun getCount_emptyDatabase_returnsZero() = runTest {
        val count = favoriteDao.getCount()

        assertEquals(0, count)
    }

    @Test
    fun getCount_withFavorites_returnsCorrectCount() = runTest {
        favoriteDao.insert(createFavorite(path = "/storage/test1.pdf"))
        favoriteDao.insert(createFavorite(path = "/storage/test2.pdf"))

        val count = favoriteDao.getCount()

        assertEquals(2, count)
    }
    // endregion

    // region Flow Tests
    @Test
    fun getAllFavorites_emptyDatabase_emitsEmptyList() = runTest {
        favoriteDao.getAllFavorites().test {
            assertEquals(emptyList<FavoriteEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavorites_withFavorites_emitsListSortedByName() = runTest {
        favoriteDao.insert(createFavorite(path = "/storage/c.pdf", name = "c.pdf"))
        favoriteDao.insert(createFavorite(path = "/storage/a.pdf", name = "a.pdf"))
        favoriteDao.insert(createFavorite(path = "/storage/b.pdf", name = "b.pdf"))

        favoriteDao.getAllFavorites().test {
            val favorites = awaitItem()
            assertEquals(3, favorites.size)
            assertEquals("a.pdf", favorites[0].name)
            assertEquals("b.pdf", favorites[1].name)
            assertEquals("c.pdf", favorites[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavorites_updatesOnInsert() = runTest {
        favoriteDao.getAllFavorites().test {
            // Initial empty list
            assertEquals(emptyList<FavoriteEntity>(), awaitItem())

            // Insert favorite
            favoriteDao.insert(createFavorite())

            // Should emit updated list
            val updatedList = awaitItem()
            assertEquals(1, updatedList.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllFavorites_updatesOnDelete() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)

        favoriteDao.getAllFavorites().test {
            // Initial list with one item
            assertEquals(1, awaitItem().size)

            // Delete favorite
            favoriteDao.deleteByPath(favorite.path)

            // Should emit empty list
            assertEquals(emptyList<FavoriteEntity>(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavoriteFlow_existingPath_emitsTrue() = runTest {
        val favorite = createFavorite()
        favoriteDao.insert(favorite)

        favoriteDao.isFavoriteFlow(favorite.path).test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavoriteFlow_nonExistentPath_emitsFalse() = runTest {
        favoriteDao.isFavoriteFlow("/nonexistent/path.pdf").test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavoriteFlow_updatesOnInsert() = runTest {
        val path = "/storage/test.pdf"

        favoriteDao.isFavoriteFlow(path).test {
            // Initially not favorite
            assertFalse(awaitItem())

            // Add to favorites
            favoriteDao.insert(createFavorite(path = path))

            // Should emit true
            assertTrue(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isFavoriteFlow_updatesOnDelete() = runTest {
        val path = "/storage/test.pdf"
        favoriteDao.insert(createFavorite(path = path))

        favoriteDao.isFavoriteFlow(path).test {
            // Initially favorite
            assertTrue(awaitItem())

            // Remove from favorites
            favoriteDao.deleteByPath(path)

            // Should emit false
            assertFalse(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Edge Cases
    @Test
    fun insert_veryLongPath_insertsSuccessfully() = runTest {
        val longPath = "/storage/" + "a".repeat(500) + ".pdf"
        val favorite = createFavorite(path = longPath)

        favoriteDao.insert(favorite)

        val result = favoriteDao.getByPath(longPath)
        assertNotNull(result)
        assertEquals(longPath, result?.path)
    }

    @Test
    fun insert_specialCharactersInName_insertsSuccessfully() = runTest {
        val specialName = "test (1) - [copy] 日本語.pdf"
        val favorite = createFavorite(name = specialName)

        favoriteDao.insert(favorite)

        val result = favoriteDao.getByPath(favorite.path)
        assertEquals(specialName, result?.name)
    }

    @Test
    fun insert_zeroSize_insertsSuccessfully() = runTest {
        val favorite = createFavorite(size = 0L)

        favoriteDao.insert(favorite)

        val result = favoriteDao.getByPath(favorite.path)
        assertEquals(0L, result?.size)
    }

    @Test
    fun insert_maxLongSize_insertsSuccessfully() = runTest {
        val favorite = createFavorite(size = Long.MAX_VALUE)

        favoriteDao.insert(favorite)

        val result = favoriteDao.getByPath(favorite.path)
        assertEquals(Long.MAX_VALUE, result?.size)
    }
    // endregion
}
