package com.rejowan.pdfreaderpro.util

import com.rejowan.pdfreaderpro.util.ReleaseNotes.Block
import com.rejowan.pdfreaderpro.util.ReleaseNotes.Span
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the release notes shown in the update sheet. The release body on GitHub
 * also carries download, install and checksum sections that mean nothing inside
 * the app, and its Markdown used to show up as raw symbols.
 */
class ReleaseNotesTest {

    private val body = """
        ## What's New

        ### Added
        - **Character maps** - PDFs with Japanese text open (closes #89)
        - Try `Again` works, see [the issue](https://github.com/x/y/issues/89)

        ### Fixed
        - Document Info no longer crashes
          - in landscape too

        ## Download

        | File | Size |
        |------|------|
        | `PdfReaderPro-v2.5.0.apk` | 16M |

        ## What's Changed
        * Add Italian by @someone in https://github.com/x/y/pull/95
    """.trimIndent()

    @Test
    fun `only the what's new section is kept`() {
        val notes = ReleaseNotes.whatsNew(body)

        assertTrue(notes.startsWith("### Added"))
        assertTrue(notes.contains("in landscape too"))
        assertFalse(notes.contains("Download"))
        assertFalse(notes.contains("PdfReaderPro-v2.5.0.apk"))
        assertFalse(notes.contains("What's Changed"))
    }

    @Test
    fun `notes without a what's new heading are shown whole`() {
        assertEquals("Bug fixes and improvements", ReleaseNotes.whatsNew("  Bug fixes and improvements\n"))
    }

    @Test
    fun `headings and bullets become blocks without their markdown symbols`() {
        val blocks = ReleaseNotes.parse(ReleaseNotes.whatsNew(body))

        assertEquals(Block.Heading(3, listOf(Span.Plain("Added"))), blocks[0])
        assertTrue(blocks[1] is Block.Bullet)
        assertEquals(Block.Heading(3, listOf(Span.Plain("Fixed"))), blocks[3])
        assertEquals(Block.Bullet(1, listOf(Span.Plain("in landscape too"))), blocks[5])
    }

    @Test
    fun `bold, code and links are recognised inside a line`() {
        val blocks = ReleaseNotes.parse(ReleaseNotes.whatsNew(body))

        assertEquals(
            listOf(Span.Bold("Character maps"), Span.Plain(" - PDFs with Japanese text open (closes #89)")),
            (blocks[1] as Block.Bullet).text
        )
        assertEquals(
            listOf(
                Span.Plain("Try "),
                Span.Code("Again"),
                Span.Plain(" works, see "),
                Span.Link("the issue", "https://github.com/x/y/issues/89")
            ),
            (blocks[2] as Block.Bullet).text
        )
    }

    @Test
    fun `wrapped lines join into one paragraph and blank lines split them`() {
        val blocks = ReleaseNotes.parse("First line\ncontinues here\n\nSecond paragraph\n\n---\n")

        assertEquals(
            listOf(
                Block.Paragraph(listOf(Span.Plain("First line continues here"))),
                Block.Paragraph(listOf(Span.Plain("Second paragraph")))
            ),
            blocks
        )
    }
}
