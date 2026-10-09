package com.rejowan.pdfreaderpro.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The language picker lists [AppLanguage.supported] by hand, so a translation
 * added without updating it would never be offered. This keeps the two in step.
 */
class AppLanguageTest {

    @Test
    fun `the picker offers exactly the languages the app is translated into`() {
        val translated = File("src/main/res")
            .listFiles { file -> file.isDirectory && File(file, "strings.xml").exists() }!!
            .map { it.name }
            .filter { it.startsWith("values-") }
            .map { it.removePrefix("values-") }
            // Qualifiers such as night or v31 are not languages.
            .filter { Regex("^[a-z]{2,3}(-r[A-Z]{2})?$").matches(it) }

        assertEquals((translated + "en").sorted(), AppLanguage.supported.sorted())
    }
}
