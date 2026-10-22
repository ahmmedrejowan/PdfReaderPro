package com.rejowan.pdfreaderpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The app edits a few lines inside the bundled PDF.js (see Upgrading PDF.js in
 * CONTRIBUTING.md). Upgrading PDF.js replaces those files and drops the edits
 * without any other sign, so these checks fail until each one is applied again.
 */
class BundledPdfJsPatchesTest {

    private val assets = File("src/main/assets/com/rejowan/mozilla")
    private val pdf by lazy { File(assets, "build/pdf.mjs").readText() }
    private val viewer by lazy { File(assets, "pdfjs/pdf_viewer.mjs").readText() }

    @Test
    fun `the bundled PDF js is the version the patches were written for`() {
        val version = Regex("""const version = "([^"]+)"""").find(pdf)?.groupValues?.get(1)
        assertEquals(
            "PDF.js changed: re-apply Upgrading PDF.js in CONTRIBUTING.md, then update this version",
            "5.3.31",
            version
        )
    }

    @Test
    fun `the signature editor is turned on in both option defaults`() {
        assertTrue(
            "enableSignatureEditor default in defaultOptions (Upgrading PDF.js, 1)",
            Regex("""enableSignatureEditor: \{\s*value: true""").containsMatchIn(viewer)
        )
        assertTrue(
            "enableSignatureEditor default in BasePreferences (Upgrading PDF.js, 1)",
            Regex("""\n\s*enableSignatureEditor: true,""").containsMatchIn(viewer)
        )
    }

    @Test
    fun `images are drawn through the image filter in both painters`() {
        assertEquals(
            "ctx.filter = globalThis.pdfReaderImageFilter (Upgrading PDF.js, 2)",
            2,
            Regex("""ctx\.filter = globalThis\.pdfReaderImageFilter;""").findAll(pdf).count()
        )
    }

    @Test
    fun `jumps leave room for the top inset`() {
        assertTrue(
            "offsetY -= globalThis.pdfReaderTopInset in scrollIntoView (Upgrading PDF.js, 3)",
            viewer.contains("offsetY -= globalThis.pdfReaderTopInset || 0;")
        )
    }

    @Test
    fun `the character maps are bundled`() {
        val maps = File(assets, "web/cmaps").listFiles { file -> file.extension == "bcmap" }.orEmpty()
        assertTrue("web/cmaps from pdfjs-dist (Upgrading PDF.js)", maps.size > 100)
    }
}
