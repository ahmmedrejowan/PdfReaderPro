package com.rejowan.pdfreaderpro.presentation.reader

import com.rejowan.pdfreaderpro.presentation.screens.reader.PlacedSignatureJson
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the payload the viewer reports its placed signatures with.
 *
 * This is the seam restoring depends on: the rectangle read out here is what gets
 * stored, and what a placement is later put back at. A misread corner would put a
 * restored signature somewhere the user never placed it, so the shape of the
 * decode is worth pinning down, including the malformed cases that must not throw.
 */
class PlacedSignatureJsonTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun decode(raw: String) = json.decodeFromString<List<PlacedSignatureJson>>(raw)

    @Test
    fun `decodes what the viewer reports`() {
        val placed = decode(
            """[{"key":"pdfjs_internal_editor_4","pageIndex":0,
               "rect":[238.76,379.09,369.93,413.09]}]"""
        )

        assertEquals(1, placed.size)
        assertEquals("pdfjs_internal_editor_4", placed[0].key)
        assertEquals(0, placed[0].pageIndex)
    }

    @Test
    fun `the rectangle reads left bottom right top, in that order`() {
        val placed = decode("""[{"key":"k","pageIndex":2,"rect":[100.0,200.0,300.0,400.0]}]""")[0]

        assertEquals(100f, placed.left)
        assertEquals(200f, placed.bottom)
        assertEquals(300f, placed.right)
        assertEquals(400f, placed.top)
    }

    @Test
    fun `top is above bottom, which is what the move call relies on`() {
        // PDF user space counts upwards, so a valid rectangle has top > bottom. The
        // restore passes left and top, and would place a signature upside down on
        // the page if these were read the other way round.
        val placed = decode("""[{"key":"k","pageIndex":0,"rect":[10.0,20.0,30.0,40.0]}]""")[0]
        assertTrue(placed.top > placed.bottom)
        assertTrue(placed.right > placed.left)
    }

    @Test
    fun `an empty list decodes to nothing placed`() {
        assertTrue(decode("[]").isEmpty())
    }

    @Test
    fun `several placements keep their order, so the newest is last`() {
        // persistNewestPlacement takes the last entry as the one just created.
        val placed = decode(
            """[{"key":"a","pageIndex":0,"rect":[1.0,2.0,3.0,4.0]},
                {"key":"b","pageIndex":1,"rect":[5.0,6.0,7.0,8.0]},
                {"key":"c","pageIndex":2,"rect":[9.0,10.0,11.0,12.0]}]"""
        )

        assertEquals(listOf("a", "b", "c"), placed.map { it.key })
        assertEquals("c", placed.last().key)
    }

    @Test
    fun `fields the viewer adds later are ignored rather than fatal`() {
        val placed = decode(
            """[{"key":"k","pageIndex":0,"rect":[1.0,2.0,3.0,4.0],
                "isSignature":true,"thickness":3,"somethingNew":"x"}]"""
        )
        assertEquals("k", placed[0].key)
    }

    @Test
    fun `a short rectangle falls back to zero instead of throwing`() {
        // Defensive: the payload is built in JS, and a malformed one should not take
        // the reader down mid-session.
        val placed = decode("""[{"key":"k","pageIndex":0,"rect":[5.0]}]""")[0]
        assertEquals(5f, placed.left)
        assertEquals(0f, placed.bottom)
        assertEquals(0f, placed.right)
        assertEquals(0f, placed.top)
    }

    @Test
    fun `page index survives, so a placement is restored onto the right page`() {
        val placed = decode("""[{"key":"k","pageIndex":37,"rect":[1.0,2.0,3.0,4.0]}]""")[0]
        assertEquals(37, placed.pageIndex)
    }
}
