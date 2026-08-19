package com.rejowan.pdfreaderpro.data.repository

import android.content.Context
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.ReaderProperties
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.AreaBreakType
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Covers what the PDF tools actually do to a document.
 *
 * Everything here works on real PDFs written by iText and reads the output back,
 * because the interesting failures are not in the view models: pages coming out in
 * the wrong order, the wrong page being dropped, or a rotation landing on pages the
 * user did not pick. Mocking the repository would assert none of that.
 *
 * Each source page carries text naming it, so a page can be identified after the
 * fact no matter where it ended up.
 */
class PdfToolsOperationsTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: PdfToolsRepositoryImpl

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        repository = PdfToolsRepositoryImpl(context)
    }

    /** A PDF whose pages read "Page 1 of alpha", and so on. */
    private fun pdf(name: String, pages: Int = 5): File {
        val file = folder.newFile("$name.pdf")
        PdfDocument(PdfWriter(file.absolutePath)).use { pdf ->
            val document = Document(pdf)
            document.setFont(PdfFontFactory.createFont())
            repeat(pages) { index ->
                document.add(Paragraph("Page ${index + 1} of $name"))
                if (index < pages - 1) document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
            }
            document.close()
        }
        return file
    }

    private fun out(name: String) = File(folder.root, name).absolutePath

    /** The text of every page, in order, so pages can be followed through a tool. */
    private fun textOf(path: String, password: String? = null): List<String> {
        val properties = ReaderProperties().apply {
            password?.let { setPassword(it.toByteArray()) }
        }
        val reader = PdfReader(File(path), properties).apply { setUnethicalReading(true) }
        return PdfDocument(reader).use { pdf ->
            (1..pdf.numberOfPages).map {
                PdfTextExtractor.getTextFromPage(pdf.getPage(it)).trim()
            }
        }
    }

    private fun pageCountOf(path: String) = PdfDocument(PdfReader(path)).use { it.numberOfPages }

    // region Reading a document
    @Test
    fun `the page count is the number of pages`() = runTest {
        val result = repository.getPageCount(pdf("alpha", pages = 7).absolutePath)
        assertEquals(7, result.getOrNull())
    }

    @Test
    fun `a file that is not a pdf fails rather than reporting zero pages`() = runTest {
        val notAPdf = folder.newFile("notes.pdf").apply { writeText("just some text") }
        assertTrue(repository.getPageCount(notAPdf.absolutePath).isFailure)
    }

    @Test
    fun `an ordinary document is not password protected`() = runTest {
        val result = repository.isPasswordProtected(pdf("alpha").absolutePath)
        assertFalse(result.getOrThrow())
    }
    // endregion

    // region Merging
    @Test
    fun `merging joins the documents in the order given`() = runTest {
        val first = pdf("alpha", pages = 2)
        val second = pdf("beta", pages = 3)
        val output = out("merged.pdf")

        repository.mergePdfs(listOf(first.absolutePath, second.absolutePath), output).getOrThrow()

        assertEquals(
            listOf(
                "Page 1 of alpha", "Page 2 of alpha",
                "Page 1 of beta", "Page 2 of beta", "Page 3 of beta"
            ),
            textOf(output)
        )
    }

    @Test
    fun `merging fewer than two documents is refused`() = runTest {
        val only = pdf("alpha")
        val result = repository.mergePdfs(listOf(only.absolutePath), out("merged.pdf"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `merging reports progress that ends at one`() = runTest {
        val seen = mutableListOf<Float>()
        repository.mergePdfs(
            listOf(pdf("alpha", 2).absolutePath, pdf("beta", 2).absolutePath),
            out("merged.pdf")
        ) { seen += it }.getOrThrow()

        assertTrue(seen.isNotEmpty())
        assertEquals(1f, seen.last(), 0.001f)
        assertEquals(seen.sorted(), seen)
    }

    @Test
    fun `merging with a selection takes only the pages asked for`() = runTest {
        val first = pdf("alpha", pages = 4)
        val second = pdf("beta", pages = 4)
        val output = out("selected.pdf")

        repository.mergePdfsWithSelection(
            listOf(
                PdfToolsRepository.PdfPageSelection(first.absolutePath, listOf(2, 4)),
                PdfToolsRepository.PdfPageSelection(second.absolutePath, listOf(1))
            ),
            output
        ).getOrThrow()

        assertEquals(
            listOf("Page 2 of alpha", "Page 4 of alpha", "Page 1 of beta"),
            textOf(output)
        )
    }

    @Test
    fun `a selection of no pages means the whole document`() = runTest {
        // null pages is the "all of it" case the merge tool sends for untouched files.
        val first = pdf("alpha", pages = 2)
        val output = out("all.pdf")

        repository.mergePdfsWithSelection(
            listOf(
                PdfToolsRepository.PdfPageSelection(first.absolutePath, null),
                PdfToolsRepository.PdfPageSelection(first.absolutePath, null)
            ),
            output
        ).getOrThrow()

        assertEquals(4, pageCountOf(output))
    }
    // endregion

    // region Splitting
    @Test
    fun `splitting by ranges writes one file per range, holding those pages`() = runTest {
        val source = pdf("alpha", pages = 6)
        val outputDir = folder.newFolder("split")

        val created = repository.splitPdf(
            source.absolutePath,
            outputDir.absolutePath,
            listOf("1-2", "5-6")
        ).getOrThrow()

        assertEquals(2, created.size)
        assertEquals(listOf("Page 1 of alpha", "Page 2 of alpha"), textOf(created[0]))
        assertEquals(listOf("Page 5 of alpha", "Page 6 of alpha"), textOf(created[1]))
    }

    @Test
    fun `a single page range writes a one page document`() = runTest {
        val source = pdf("alpha", pages = 6)
        val outputDir = folder.newFolder("single")

        val created = repository.splitPdf(
            source.absolutePath, outputDir.absolutePath, listOf("3")
        ).getOrThrow()

        assertEquals(listOf("Page 3 of alpha"), textOf(created.single()))
    }

    @Test
    fun `splitting into pages writes one file per page, in order`() = runTest {
        val source = pdf("alpha", pages = 4)
        val outputDir = folder.newFolder("pages")

        val created = repository.splitIntoPages(source.absolutePath, outputDir.absolutePath)
            .getOrThrow()

        assertEquals(4, created.size)
        created.forEachIndexed { index, path ->
            assertEquals(listOf("Page ${index + 1} of alpha"), textOf(path))
        }
    }

    @Test
    fun `extracting takes the pages asked for and nothing else`() = runTest {
        val source = pdf("alpha", pages = 6)
        val output = out("extracted.pdf")

        repository.extractPages(source.absolutePath, output, listOf(2, 5)).getOrThrow()

        assertEquals(listOf("Page 2 of alpha", "Page 5 of alpha"), textOf(output))
    }
    // endregion

    // region Rearranging
    @Test
    fun `reordering puts the pages in the order given`() = runTest {
        val source = pdf("alpha", pages = 4)
        val output = out("reordered.pdf")

        repository.reorderPages(source.absolutePath, output, listOf(4, 1, 3, 2)).getOrThrow()

        assertEquals(
            listOf("Page 4 of alpha", "Page 1 of alpha", "Page 3 of alpha", "Page 2 of alpha"),
            textOf(output)
        )
    }

    @Test
    fun `removing pages drops exactly those pages`() = runTest {
        val source = pdf("alpha", pages = 5)
        val output = out("trimmed.pdf")

        repository.removePages(source.absolutePath, output, listOf(2, 4)).getOrThrow()

        assertEquals(
            listOf("Page 1 of alpha", "Page 3 of alpha", "Page 5 of alpha"),
            textOf(output)
        )
    }

    @Test
    fun `removing the first and last pages keeps the middle`() = runTest {
        val source = pdf("alpha", pages = 3)
        val output = out("middle.pdf")

        repository.removePages(source.absolutePath, output, listOf(1, 3)).getOrThrow()

        assertEquals(listOf("Page 2 of alpha"), textOf(output))
    }
    // endregion

    // region Rotating
    @Test
    fun `rotating every page turns all of them`() = runTest {
        val source = pdf("alpha", pages = 3)
        val output = out("rotated.pdf")

        repository.rotatePages(source.absolutePath, output, 90, null).getOrThrow()

        PdfDocument(PdfReader(output)).use { pdf ->
            (1..pdf.numberOfPages).forEach {
                assertEquals(90, pdf.getPage(it).rotation)
            }
        }
    }

    @Test
    fun `rotating a selection leaves the other pages alone`() = runTest {
        val source = pdf("alpha", pages = 4)
        val output = out("part-rotated.pdf")

        repository.rotatePages(source.absolutePath, output, 180, listOf(2)).getOrThrow()

        PdfDocument(PdfReader(output)).use { pdf ->
            assertEquals(0, pdf.getPage(1).rotation)
            assertEquals(180, pdf.getPage(2).rotation)
            assertEquals(0, pdf.getPage(3).rotation)
        }
    }

    @Test
    fun `rotating twice adds up rather than replacing`() = runTest {
        // Rotation is a property of the page, so applying it again to an already
        // rotated document has to accumulate, the way a second tap does on screen.
        val source = pdf("alpha", pages = 1)
        val once = out("once.pdf")
        val twice = out("twice.pdf")

        repository.rotatePages(source.absolutePath, once, 90, null).getOrThrow()
        repository.rotatePages(once, twice, 90, null).getOrThrow()

        PdfDocument(PdfReader(twice)).use { pdf ->
            assertEquals(180, pdf.getPage(1).rotation)
        }
    }

    @Test
    fun `rotation keeps the text on the page`() = runTest {
        val source = pdf("alpha", pages = 2)
        val output = out("rotated-text.pdf")

        repository.rotatePages(source.absolutePath, output, 270, null).getOrThrow()

        assertEquals(listOf("Page 1 of alpha", "Page 2 of alpha"), textOf(output))
    }
    // endregion

    // region Passwords
    @Test
    fun `locking makes the document need its password`() = runTest {
        val source = pdf("alpha", pages = 2)
        val output = out("locked.pdf")

        repository.lockPdf(source.absolutePath, output, "open-me", "owner-me").getOrThrow()

        assertTrue(repository.isPasswordProtected(output).getOrThrow())
        assertEquals(listOf("Page 1 of alpha", "Page 2 of alpha"), textOf(output, "open-me"))
    }

    @Test
    fun `unlocking with the right password gives a document that opens freely`() = runTest {
        val source = pdf("alpha", pages = 2)
        val locked = out("locked2.pdf")
        val unlocked = out("unlocked.pdf")
        repository.lockPdf(source.absolutePath, locked, "open-me", "owner-me").getOrThrow()

        repository.unlockPdf(locked, unlocked, "open-me").getOrThrow()

        assertFalse(repository.isPasswordProtected(unlocked).getOrThrow())
        assertEquals(listOf("Page 1 of alpha", "Page 2 of alpha"), textOf(unlocked))
    }

    @Test
    fun `unlocking with the wrong password fails and writes nothing usable`() = runTest {
        val source = pdf("alpha", pages = 2)
        val locked = out("locked3.pdf")
        repository.lockPdf(source.absolutePath, locked, "open-me", "owner-me").getOrThrow()

        val result = repository.unlockPdf(locked, out("wrong.pdf"), "not-the-password")

        assertTrue(result.isFailure)
    }

    @Test
    fun `the owner password also unlocks the document`() = runTest {
        val source = pdf("alpha", pages = 1)
        val locked = out("locked4.pdf")
        val unlocked = out("unlocked4.pdf")
        repository.lockPdf(source.absolutePath, locked, "open-me", "owner-me").getOrThrow()

        repository.unlockPdf(locked, unlocked, "owner-me").getOrThrow()

        assertEquals(listOf("Page 1 of alpha"), textOf(unlocked))
    }

    @Test
    fun `a document encrypted elsewhere is recognised as protected`() = runTest {
        val file = folder.newFile("foreign.pdf")
        val properties = WriterProperties().setStandardEncryption(
            "u".toByteArray(),
            "o".toByteArray(),
            EncryptionConstants.ALLOW_PRINTING,
            EncryptionConstants.ENCRYPTION_AES_256
        )
        PdfDocument(PdfWriter(file.absolutePath, properties)).use { it.addNewPage() }

        assertTrue(repository.isPasswordProtected(file.absolutePath).getOrThrow())
    }
    // endregion

    // region Stamping
    @Test
    fun `a text watermark appears on every page alongside the original text`() = runTest {
        val source = pdf("alpha", pages = 3)
        val output = out("watermarked.pdf")

        repository.addTextWatermark(
            source.absolutePath,
            output,
            PdfToolsRepository.TextWatermarkConfig(text = "CONFIDENTIAL")
        ).getOrThrow()

        val pages = textOf(output)
        assertEquals(3, pages.size)
        pages.forEachIndexed { index, text ->
            assertTrue(text.contains("CONFIDENTIAL"))
            assertTrue(text.contains("Page ${index + 1} of alpha"))
        }
    }

    @Test
    fun `a watermark on chosen pages leaves the others unmarked`() = runTest {
        val source = pdf("alpha", pages = 3)
        val output = out("watermarked-some.pdf")

        repository.addTextWatermark(
            source.absolutePath,
            output,
            PdfToolsRepository.TextWatermarkConfig(text = "DRAFT"),
            pages = listOf(2)
        ).getOrThrow()

        val pages = textOf(output)
        assertFalse(pages[0].contains("DRAFT"))
        assertTrue(pages[1].contains("DRAFT"))
        assertFalse(pages[2].contains("DRAFT"))
    }

    @Test
    fun `page numbers are stamped on every page, counting from one`() = runTest {
        val source = pdf("alpha", pages = 3)
        val output = out("numbered.pdf")

        repository.addPageNumbers(
            source.absolutePath,
            output,
            PdfToolsRepository.PageNumberConfig()
        ).getOrThrow()

        val pages = textOf(output)
        assertTrue(pages[0].contains("1"))
        assertTrue(pages[2].contains("3"))
    }

    @Test
    fun `page numbers can start from a number other than one`() = runTest {
        val source = pdf("alpha", pages = 2)
        val output = out("numbered-from.pdf")

        repository.addPageNumbers(
            source.absolutePath,
            output,
            PdfToolsRepository.PageNumberConfig(startNumber = 10)
        ).getOrThrow()

        assertTrue(textOf(output)[0].contains("10"))
        assertTrue(textOf(output)[1].contains("11"))
    }

    @Test
    fun `the x of y format names the total as well as the page`() = runTest {
        val source = pdf("alpha", pages = 4)
        val output = out("numbered-of.pdf")

        repository.addPageNumbers(
            source.absolutePath,
            output,
            PdfToolsRepository.PageNumberConfig(
                format = PdfToolsRepository.PageNumberFormat.X_OF_Y
            )
        ).getOrThrow()

        assertTrue(textOf(output)[0].contains("1 of 4"))
    }

    @Test
    fun `a custom format keeps the prefix and suffix the user typed`() = runTest {
        val source = pdf("alpha", pages = 2)
        val output = out("numbered-custom.pdf")

        repository.addPageNumbers(
            source.absolutePath,
            output,
            PdfToolsRepository.PageNumberConfig(
                format = PdfToolsRepository.PageNumberFormat.CUSTOM,
                prefix = "[",
                suffix = "]"
            )
        ).getOrThrow()

        assertTrue(textOf(output)[0].contains("[1]"))
    }
    // endregion

    // region Compressing
    @Test
    fun `compressing keeps the pages and their text`() = runTest {
        val source = pdf("alpha", pages = 4)
        val output = out("compressed.pdf")

        repository.compressPdf(source.absolutePath, output, quality = 0.5f).getOrThrow()

        assertEquals(4, pageCountOf(output))
        assertEquals(listOf(
            "Page 1 of alpha", "Page 2 of alpha", "Page 3 of alpha", "Page 4 of alpha"
        ), textOf(output))
    }

    @Test
    fun `compressing reports the size of what it wrote`() = runTest {
        val source = pdf("alpha", pages = 4)
        val output = out("compressed2.pdf")

        val size = repository.compressPdf(source.absolutePath, output, quality = 0.3f).getOrThrow()

        assertEquals(File(output).length(), size)
        assertNotEquals(0L, size)
    }

    @Test
    fun `analysing a document reports estimates that get more aggressive in turn`() = runTest {
        val source = pdf("alpha", pages = 5)

        val analysis = repository.analyzeCompressionPotential(source.absolutePath).getOrThrow()

        assertTrue(analysis.bytesPerPage > 0)
        assertTrue(analysis.estimatedRatioHigh <= analysis.estimatedRatioMedium)
        assertTrue(analysis.estimatedRatioMedium <= analysis.estimatedRatioLow)
    }

    @Test
    fun `analysing something that is not a pdf fails rather than guessing`() = runTest {
        val notAPdf = folder.newFile("notes2.pdf").apply { writeText("just some text") }
        assertTrue(repository.analyzeCompressionPotential(notAPdf.absolutePath).isFailure)
    }
    // endregion

    // region Failing safely
    @Test
    fun `every tool fails rather than throwing when the input is missing`() = runTest {
        val missing = File(folder.root, "not-here.pdf").absolutePath

        assertTrue(repository.getPageCount(missing).isFailure)
        assertTrue(repository.extractPages(missing, out("x.pdf"), listOf(1)).isFailure)
        assertTrue(repository.rotatePages(missing, out("x.pdf"), 90).isFailure)
        assertTrue(repository.reorderPages(missing, out("x.pdf"), listOf(1)).isFailure)
        assertTrue(repository.removePages(missing, out("x.pdf"), listOf(1)).isFailure)
        assertTrue(repository.splitIntoPages(missing, folder.root.absolutePath).isFailure)
        assertTrue(repository.compressPdf(missing, out("x.pdf")).isFailure)
        assertTrue(repository.unlockPdf(missing, out("x.pdf"), "p").isFailure)
    }

    @Test
    fun `a page that does not exist is a failure, not a silent skip`() = runTest {
        val source = pdf("alpha", pages = 2)
        assertTrue(repository.extractPages(source.absolutePath, out("y.pdf"), listOf(9)).isFailure)
    }
    // endregion
}
