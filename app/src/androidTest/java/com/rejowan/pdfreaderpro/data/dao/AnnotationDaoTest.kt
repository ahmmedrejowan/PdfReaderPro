package com.rejowan.pdfreaderpro.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.rejowan.pdfreaderpro.data.local.database.PdfDatabase
import com.rejowan.pdfreaderpro.data.local.database.dao.AnnotationDao
import com.rejowan.pdfreaderpro.data.local.database.entity.AnnotationEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnnotationDaoTest {

    private lateinit var database: PdfDatabase
    private lateinit var annotationDao: AnnotationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PdfDatabase::class.java
        ).allowMainThreadQueries().build()

        annotationDao = database.annotationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // region Test Data
    private fun createAnnotation(
        id: Long = 0,
        pdfPath: String = "/storage/test.pdf",
        pageNumber: Int = 1,
        type: String = "highlight",
        content: String? = null,
        color: Int? = 0xFFFF00,
        startX: Float? = 10f,
        startY: Float? = 20f,
        endX: Float? = 100f,
        endY: Float? = 40f,
        createdAt: Long = System.currentTimeMillis()
    ) = AnnotationEntity(
        id = id,
        pdfPath = pdfPath,
        pageNumber = pageNumber,
        type = type,
        content = content,
        color = color,
        startX = startX,
        startY = startY,
        endX = endX,
        endY = endY,
        createdAt = createdAt,
        updatedAt = createdAt
    )
    // endregion

    // region Insert Tests
    @Test
    fun insert_singleAnnotation_insertsSuccessfully() = runTest {
        val annotation = createAnnotation()

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertNotNull(result)
        assertEquals(annotation.pdfPath, result?.pdfPath)
        assertEquals(annotation.pageNumber, result?.pageNumber)
        assertEquals(annotation.type, result?.type)
    }

    @Test
    fun insert_multipleAnnotations_insertsAll() = runTest {
        val annotation1 = createAnnotation(pageNumber = 1)
        val annotation2 = createAnnotation(pageNumber = 2)
        val annotation3 = createAnnotation(pageNumber = 3)

        annotationDao.insert(annotation1)
        annotationDao.insert(annotation2)
        annotationDao.insert(annotation3)

        val count = annotationDao.getCountForPdf("/storage/test.pdf")
        assertEquals(3, count)
    }

    @Test
    fun insert_highlightAnnotation_insertsWithCorrectType() = runTest {
        val annotation = createAnnotation(type = "highlight", color = 0xFFFF00)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals("highlight", result?.type)
        assertEquals(0xFFFF00, result?.color)
    }

    @Test
    fun insert_underlineAnnotation_insertsWithCorrectType() = runTest {
        val annotation = createAnnotation(type = "underline", color = 0xFF0000)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals("underline", result?.type)
    }

    @Test
    fun insert_noteAnnotation_insertsWithContent() = runTest {
        val annotation = createAnnotation(
            type = "note",
            content = "This is a note"
        )

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals("note", result?.type)
        assertEquals("This is a note", result?.content)
    }

    @Test
    fun insert_duplicateId_replacesExisting() = runTest {
        val annotation1 = createAnnotation(id = 1, content = "Original")
        val annotation2 = createAnnotation(id = 1, content = "Updated")

        annotationDao.insert(annotation1)
        annotationDao.insert(annotation2)

        val result = annotationDao.getById(1)
        assertEquals("Updated", result?.content)
    }
    // endregion

    // region Update Tests
    @Test
    fun update_existingAnnotation_updatesSuccessfully() = runTest {
        val annotation = createAnnotation(content = "Original")
        val insertedId = annotationDao.insert(annotation)
        val inserted = annotationDao.getById(insertedId)!!

        val updated = inserted.copy(content = "Updated content")
        annotationDao.update(updated)

        val result = annotationDao.getById(insertedId)
        assertEquals("Updated content", result?.content)
    }

    @Test
    fun update_annotationColor_updatesSuccessfully() = runTest {
        val annotation = createAnnotation(color = 0xFFFF00)
        val insertedId = annotationDao.insert(annotation)
        val inserted = annotationDao.getById(insertedId)!!

        val updated = inserted.copy(color = 0x00FF00)
        annotationDao.update(updated)

        val result = annotationDao.getById(insertedId)
        assertEquals(0x00FF00, result?.color)
    }

    @Test
    fun update_annotationCoordinates_updatesSuccessfully() = runTest {
        val annotation = createAnnotation(startX = 10f, startY = 20f, endX = 100f, endY = 40f)
        val insertedId = annotationDao.insert(annotation)
        val inserted = annotationDao.getById(insertedId)!!

        val updated = inserted.copy(startX = 50f, startY = 60f, endX = 150f, endY = 80f)
        annotationDao.update(updated)

        val result = annotationDao.getById(insertedId)
        assertEquals(50f, result?.startX)
        assertEquals(60f, result?.startY)
        assertEquals(150f, result?.endX)
        assertEquals(80f, result?.endY)
    }
    // endregion

    // region Delete Tests
    @Test
    fun delete_existingAnnotation_removesFromDatabase() = runTest {
        val annotation = createAnnotation()
        val insertedId = annotationDao.insert(annotation)
        val inserted = annotationDao.getById(insertedId)!!

        annotationDao.delete(inserted)

        assertNull(annotationDao.getById(insertedId))
    }

    @Test
    fun deleteById_existingId_removesFromDatabase() = runTest {
        val annotation = createAnnotation()
        val insertedId = annotationDao.insert(annotation)

        annotationDao.deleteById(insertedId)

        assertNull(annotationDao.getById(insertedId))
    }

    @Test
    fun deleteById_nonExistentId_doesNothing() = runTest {
        val annotation = createAnnotation()
        annotationDao.insert(annotation)

        annotationDao.deleteById(9999L)

        assertEquals(1, annotationDao.getCountForPdf("/storage/test.pdf"))
    }

    @Test
    fun deleteAllForPdf_removesAllAnnotationsForPdf() = runTest {
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test.pdf", pageNumber = 1))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test.pdf", pageNumber = 2))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/other.pdf", pageNumber = 1))

        annotationDao.deleteAllForPdf("/storage/test.pdf")

        assertEquals(0, annotationDao.getCountForPdf("/storage/test.pdf"))
        assertEquals(1, annotationDao.getCountForPdf("/storage/other.pdf"))
    }

    @Test
    fun deleteAllForPage_removesAllAnnotationsForPage() = runTest {
        annotationDao.insert(createAnnotation(pageNumber = 1))
        annotationDao.insert(createAnnotation(pageNumber = 1))
        annotationDao.insert(createAnnotation(pageNumber = 2))

        annotationDao.deleteAllForPage("/storage/test.pdf", 1)

        assertEquals(1, annotationDao.getCountForPdf("/storage/test.pdf"))
    }

    @Test
    fun clearAll_removesAllAnnotations() = runTest {
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test1.pdf"))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test2.pdf"))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test3.pdf"))

        annotationDao.clearAll()

        assertEquals(0, annotationDao.getCountForPdf("/storage/test1.pdf"))
        assertEquals(0, annotationDao.getCountForPdf("/storage/test2.pdf"))
        assertEquals(0, annotationDao.getCountForPdf("/storage/test3.pdf"))
    }
    // endregion

    // region Query Tests
    @Test
    fun getById_existingId_returnsAnnotation() = runTest {
        val annotation = createAnnotation()
        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)

        assertNotNull(result)
        assertEquals(insertedId, result?.id)
    }

    @Test
    fun getById_nonExistentId_returnsNull() = runTest {
        val result = annotationDao.getById(9999L)

        assertNull(result)
    }

    @Test
    fun getCountForPdf_emptyDatabase_returnsZero() = runTest {
        val count = annotationDao.getCountForPdf("/storage/test.pdf")

        assertEquals(0, count)
    }

    @Test
    fun getCountForPdf_withAnnotations_returnsCorrectCount() = runTest {
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test.pdf", pageNumber = 1))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test.pdf", pageNumber = 2))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/other.pdf", pageNumber = 1))

        val count = annotationDao.getCountForPdf("/storage/test.pdf")

        assertEquals(2, count)
    }
    // endregion

    // region Flow Tests
    @Test
    fun getAnnotationsForPdf_emptyDatabase_emitsEmptyList() = runTest {
        annotationDao.getAnnotationsForPdf("/storage/test.pdf").test {
            assertEquals(emptyList<AnnotationEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPdf_withAnnotations_emitsListSortedByPageAndCreatedAt() = runTest {
        val time = System.currentTimeMillis()
        annotationDao.insert(createAnnotation(pageNumber = 3, createdAt = time))
        annotationDao.insert(createAnnotation(pageNumber = 1, createdAt = time))
        annotationDao.insert(createAnnotation(pageNumber = 1, createdAt = time + 1000))
        annotationDao.insert(createAnnotation(pageNumber = 2, createdAt = time))

        annotationDao.getAnnotationsForPdf("/storage/test.pdf").test {
            val annotations = awaitItem()
            assertEquals(4, annotations.size)
            assertEquals(1, annotations[0].pageNumber)
            assertEquals(1, annotations[1].pageNumber)
            assertEquals(2, annotations[2].pageNumber)
            assertEquals(3, annotations[3].pageNumber)
            // Check createdAt ordering within same page
            assertTrue(annotations[0].createdAt <= annotations[1].createdAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPdf_onlyReturnsAnnotationsForSpecifiedPdf() = runTest {
        annotationDao.insert(createAnnotation(pdfPath = "/storage/test.pdf", pageNumber = 1))
        annotationDao.insert(createAnnotation(pdfPath = "/storage/other.pdf", pageNumber = 1))

        annotationDao.getAnnotationsForPdf("/storage/test.pdf").test {
            val annotations = awaitItem()
            assertEquals(1, annotations.size)
            assertEquals("/storage/test.pdf", annotations[0].pdfPath)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPdf_updatesOnInsert() = runTest {
        annotationDao.getAnnotationsForPdf("/storage/test.pdf").test {
            assertEquals(emptyList<AnnotationEntity>(), awaitItem())

            annotationDao.insert(createAnnotation())

            val updatedList = awaitItem()
            assertEquals(1, updatedList.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPdf_updatesOnDelete() = runTest {
        val annotation = createAnnotation()
        val insertedId = annotationDao.insert(annotation)

        annotationDao.getAnnotationsForPdf("/storage/test.pdf").test {
            assertEquals(1, awaitItem().size)

            annotationDao.deleteById(insertedId)

            assertEquals(emptyList<AnnotationEntity>(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPage_emptyDatabase_emitsEmptyList() = runTest {
        annotationDao.getAnnotationsForPage("/storage/test.pdf", 1).test {
            assertEquals(emptyList<AnnotationEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPage_withAnnotations_emitsAnnotationsForSpecificPage() = runTest {
        annotationDao.insert(createAnnotation(pageNumber = 1))
        annotationDao.insert(createAnnotation(pageNumber = 1))
        annotationDao.insert(createAnnotation(pageNumber = 2))

        annotationDao.getAnnotationsForPage("/storage/test.pdf", 1).test {
            val annotations = awaitItem()
            assertEquals(2, annotations.size)
            assertTrue(annotations.all { it.pageNumber == 1 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAnnotationsForPage_updatesOnInsert() = runTest {
        annotationDao.getAnnotationsForPage("/storage/test.pdf", 1).test {
            assertEquals(emptyList<AnnotationEntity>(), awaitItem())

            annotationDao.insert(createAnnotation(pageNumber = 1))

            assertEquals(1, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion

    // region Edge Cases
    @Test
    fun insert_annotationWithNullContent_insertsSuccessfully() = runTest {
        val annotation = createAnnotation(type = "highlight", content = null)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertNull(result?.content)
    }

    @Test
    fun insert_annotationWithNullCoordinates_insertsSuccessfully() = runTest {
        val annotation = createAnnotation(
            type = "note",
            startX = null,
            startY = null,
            endX = null,
            endY = null
        )

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertNull(result?.startX)
        assertNull(result?.startY)
        assertNull(result?.endX)
        assertNull(result?.endY)
    }

    @Test
    fun insert_annotationWithVeryLongContent_insertsSuccessfully() = runTest {
        val longContent = "a".repeat(10000)
        val annotation = createAnnotation(type = "note", content = longContent)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals(longContent, result?.content)
    }

    @Test
    fun insert_annotationWithSpecialCharactersInPath_insertsSuccessfully() = runTest {
        val specialPath = "/storage/测试/文件 (1) [copy].pdf"
        val annotation = createAnnotation(pdfPath = specialPath)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals(specialPath, result?.pdfPath)
    }

    @Test
    fun insert_annotationWithZeroCoordinates_insertsSuccessfully() = runTest {
        val annotation = createAnnotation(startX = 0f, startY = 0f, endX = 0f, endY = 0f)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals(0f, result?.startX)
        assertEquals(0f, result?.startY)
    }

    @Test
    fun insert_annotationWithNegativeCoordinates_insertsSuccessfully() = runTest {
        val annotation = createAnnotation(startX = -10f, startY = -20f)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals(-10f, result?.startX)
        assertEquals(-20f, result?.startY)
    }

    @Test
    fun insert_annotationWithPageZero_insertsSuccessfully() = runTest {
        val annotation = createAnnotation(pageNumber = 0)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals(0, result?.pageNumber)
    }

    @Test
    fun insert_annotationWithLargePageNumber_insertsSuccessfully() = runTest {
        val annotation = createAnnotation(pageNumber = Int.MAX_VALUE)

        val insertedId = annotationDao.insert(annotation)

        val result = annotationDao.getById(insertedId)
        assertEquals(Int.MAX_VALUE, result?.pageNumber)
    }
    // endregion
}
