package com.rejowan.pdfreaderpro.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.OutputStream

/**
 * Covers the store the signature sheet reads from.
 *
 * Works against real files in a temporary directory, because what the store is for
 * is what ends up on disk: a saved signature the sheet cannot find, or a placement
 * pointing at a file that has been deleted, are the failures worth catching.
 *
 * Bitmap is mocked rather than real, matching the approach the other tests here
 * take with Android graphics, and its compress writes recognisable bytes so the
 * file contents can be asserted on.
 */
class SignatureStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var store: SignatureStore
    private lateinit var filesDir: File

    @Before
    fun setUp() {
        filesDir = folder.newFolder("files")
        context = mockk(relaxed = true)
        every { context.filesDir } returns filesDir
        store = SignatureStore(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(BitmapFactory::class)
    }

    /** A bitmap whose compress writes [content], so the file can be identified. */
    private fun bitmap(content: String = "png-bytes"): Bitmap {
        val bitmap = mockk<Bitmap>()
        every {
            bitmap.compress(any(), any(), any())
        } answers {
            (thirdArg() as OutputStream).write(content.toByteArray())
            true
        }
        return bitmap
    }

    private fun signaturesDir() = File(filesDir, "signatures")

    @Test
    fun `starts empty`() = runTest {
        assertTrue(store.list().isEmpty())
    }

    @Test
    fun `saving writes a png that exists on disk`() = runTest {
        val saved = store.save(bitmap())

        assertNotNull(saved)
        assertTrue(saved!!.file.exists())
        assertEquals("png", saved.file.extension)
        assertEquals("png-bytes", saved.file.readText())
    }

    @Test
    fun `saving puts the file in the signatures directory`() = runTest {
        val saved = store.save(bitmap())!!
        assertEquals(signaturesDir().absolutePath, saved.file.parentFile?.absolutePath)
    }

    @Test
    fun `each save gets its own file`() = runTest {
        val first = store.save(bitmap())!!
        val second = store.save(bitmap())!!

        assertFalse(first.id == second.id)
        assertFalse(first.file.absolutePath == second.file.absolutePath)
        assertEquals(2, store.list().size)
    }

    @Test
    fun `newest is listed first`() = runTest {
        val first = store.save(bitmap("first"))!!
        val second = store.save(bitmap("second"))!!
        // lastModified is only second-granular on some filesystems, so make the
        // ordering explicit rather than leaning on the wall clock.
        first.file.setLastModified(1_000L)
        second.file.setLastModified(9_000L)

        assertEquals(second.id, store.list().first().id)
    }

    @Test
    fun `the id matches the file name, so a placement can point back at it`() = runTest {
        val saved = store.save(bitmap())!!
        assertEquals(saved.file.nameWithoutExtension, saved.id)
    }

    @Test
    fun `loading reads the file back through the id`() = runTest {
        val saved = store.save(bitmap())!!
        val decoded = mockk<Bitmap>()
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeFile(saved.file.absolutePath) } returns decoded

        assertEquals(decoded, store.load(saved.id))
    }

    @Test
    fun `loading an unknown id gives null rather than throwing`() = runTest {
        assertNull(store.load("not-a-real-id"))
    }

    @Test
    fun `deleting removes it from the list and from disk`() = runTest {
        val saved = store.save(bitmap())!!

        assertTrue(store.delete(saved.id))
        assertFalse(saved.file.exists())
        assertTrue(store.list().isEmpty())
    }

    @Test
    fun `deleting something that is not there reports false`() = runTest {
        assertFalse(store.delete("not-a-real-id"))
    }

    @Test
    fun `deleting one leaves the others alone`() = runTest {
        val keep = store.save(bitmap("keep"))!!
        val drop = store.save(bitmap("drop"))!!

        store.delete(drop.id)

        val remaining = store.list()
        assertEquals(1, remaining.size)
        assertEquals(keep.id, remaining.first().id)
    }

    @Test
    fun `files that are not pngs are ignored`() = runTest {
        store.save(bitmap())
        File(signaturesDir(), "notes.txt").writeText("not a signature")

        assertEquals(1, store.list().size)
    }

    @Test
    fun `a failing compress reports null instead of leaving a broken entry`() = runTest {
        val failing = mockk<Bitmap>()
        every { failing.compress(any(), any(), any()) } throws RuntimeException("out of memory")

        assertNull(store.save(failing))
    }
}
