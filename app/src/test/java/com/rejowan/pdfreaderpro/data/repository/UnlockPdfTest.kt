package com.rejowan.pdfreaderpro.data.repository

import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.ReaderProperties
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.exceptions.BadPasswordException
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.AreaBreakType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Covers the decryption the reader's "save a copy without the password" relies on.
 *
 * The reader hands its stored password to the same iText path the Unlock tool uses,
 * so these round-trip real encrypted PDFs rather than mocking the repository. The
 * point of the feature is that the copy keeps its text, which the print route does
 * not, so that is asserted directly.
 */
class UnlockPdfTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val userPassword = "user123"
    private val ownerPassword = "owner123"

    /** An encrypted PDF whose pages carry known, extractable text. */
    private fun encryptedPdf(pages: Int = 3): File {
        val file = folder.newFile("encrypted-$pages.pdf")
        val writerProperties = WriterProperties().setStandardEncryption(
            userPassword.toByteArray(),
            ownerPassword.toByteArray(),
            EncryptionConstants.ALLOW_PRINTING,
            EncryptionConstants.ENCRYPTION_AES_256
        )
        val pdf = PdfDocument(PdfWriter(file.absolutePath, writerProperties))
        val document = Document(pdf)
        document.setFont(PdfFontFactory.createFont())
        repeat(pages) { index ->
            document.add(Paragraph("Page ${index + 1} content"))
            if (index < pages - 1) document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
        }
        document.close()
        return file
    }

    private fun decrypt(source: File, password: String): File {
        val out = folder.newFile("decrypted-${source.name}")
        val reader = PdfReader(source, ReaderProperties().setPassword(password.toByteArray()))
        reader.setUnethicalReading(true)
        PdfDocument(reader, PdfWriter(out)).use { }
        return out
    }

    @Test
    fun `source pdf really is encrypted`() {
        val source = encryptedPdf()
        assertTrue(source.readBytes().toString(Charsets.ISO_8859_1).contains("/Encrypt"))
    }

    @Test
    fun `opening the source without a password fails`() {
        val source = encryptedPdf()
        var threw = false
        try {
            PdfDocument(PdfReader(source)).use { }
        } catch (e: BadPasswordException) {
            threw = true
        }
        assertTrue("expected a BadPasswordException without the password", threw)
    }

    @Test
    fun `decrypted copy opens with no password at all`() {
        val decrypted = decrypt(encryptedPdf(), userPassword)
        // Would throw if the copy were still encrypted.
        PdfDocument(PdfReader(decrypted)).use { pdf ->
            assertEquals(3, pdf.numberOfPages)
        }
        assertFalse(
            "the copy should carry no /Encrypt dictionary",
            decrypted.readBytes().toString(Charsets.ISO_8859_1).contains("/Encrypt")
        )
    }

    @Test
    fun `decrypted copy keeps its text rather than being rasterised`() {
        val decrypted = decrypt(encryptedPdf(pages = 2), userPassword)
        PdfDocument(PdfReader(decrypted)).use { pdf ->
            val firstPage = PdfTextExtractor.getTextFromPage(pdf.getPage(1))
            assertTrue(
                "expected selectable text, got '$firstPage'",
                firstPage.contains("Page 1 content")
            )
        }
    }

    @Test
    fun `page count survives decryption`() {
        val decrypted = decrypt(encryptedPdf(pages = 5), userPassword)
        PdfDocument(PdfReader(decrypted)).use { pdf ->
            assertEquals(5, pdf.numberOfPages)
        }
    }

    @Test
    fun `the owner password also decrypts`() {
        val decrypted = decrypt(encryptedPdf(), ownerPassword)
        PdfDocument(PdfReader(decrypted)).use { pdf ->
            assertEquals(3, pdf.numberOfPages)
        }
    }

    @Test
    fun `a wrong password is rejected`() {
        val source = encryptedPdf()
        var threw = false
        try {
            decrypt(source, "not-the-password")
        } catch (e: BadPasswordException) {
            threw = true
        }
        assertTrue("expected a BadPasswordException for a wrong password", threw)
    }
}
