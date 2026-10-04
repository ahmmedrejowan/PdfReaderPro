package com.rejowan.pdfreaderpro.data.local.database

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the upgrade path between database versions.
 *
 * A missing or misnumbered migration is not a subtle failure: Room refuses to open
 * the database and the app will not start for anyone upgrading, while a fresh
 * install works fine. That asymmetry is exactly what a test is for, since the
 * developer's own device usually has the newest schema already.
 *
 * The statements are captured rather than run, because there is no SQLite here.
 * What can still be checked is that each migration writes the schema it claims to
 * and does so in a way that survives being applied to a database that already has
 * part of it.
 */
class PdfDatabaseMigrationTest {

    private fun statementsFrom(migration: androidx.room.migration.Migration): List<String> {
        val statements = mutableListOf<String>()
        val sql = slot<String>()
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        every { db.execSQL(capture(sql)) } answers { statements += sql.captured }

        migration.migrate(db)
        return statements
    }

    private val migrations = PdfDatabase.migrations

    @Test
    fun `the migrations form an unbroken chain up to the current version`() {
        // A gap here means the app cannot open an older database at all.
        val steps = migrations.map { it.startVersion to it.endVersion }.sortedBy { it.first }

        steps.zipWithNext { earlier, later ->
            assertEquals(earlier.second, later.first)
        }
        assertEquals(10, steps.last().second)
    }

    @Test
    fun `every migration moves exactly one version forward`() {
        migrations.forEach {
            assertEquals(1, it.endVersion - it.startVersion)
        }
    }

    @Test
    fun `no version is migrated twice`() {
        val starts = migrations.map { it.startVersion }
        assertEquals(starts.size, starts.distinct().size)
    }

    @Test
    fun `every migration writes something`() {
        migrations.forEach {
            assertTrue(
                "migration ${it.startVersion} to ${it.endVersion} did nothing",
                statementsFrom(it).isNotEmpty()
            )
        }
    }

    @Test
    fun `every statement is safe to run against a database that already has part of it`() {
        // Upgrades can be interrupted and retried, so a bare CREATE or ADD COLUMN
        // would fail the second time and leave the app unable to start.
        migrations.forEach { migration ->
            statementsFrom(migration).forEach { statement ->
                val normalised = statement.replace(Regex("\\s+"), " ").trim().uppercase()
                if (normalised.startsWith("CREATE")) {
                    assertTrue(
                        "not idempotent: $normalised",
                        normalised.contains("IF NOT EXISTS")
                    )
                }
            }
        }
    }

    // region The signatures table
    @Test
    fun `the last migration creates the signatures table`() {
        val statements = statementsFrom(PdfDatabase.MIGRATION_9_10)

        assertTrue(statements.any { it.contains("CREATE TABLE") && it.contains("signatures") })
    }

    @Test
    fun `the signatures table carries everything a placement needs to be replayed`() {
        // Each of these is read back when a placement is put onto the page again,
        // and a column missing here means the row cannot be used at all.
        val created = statementsFrom(PdfDatabase.MIGRATION_9_10)
            .first { it.contains("CREATE TABLE") }

        listOf(
            "pdfPath", "pageIndex",
            "rectLeft", "rectBottom", "rectRight", "rectTop",
            "savedSignatureId", "imagePath", "createdAt"
        ).forEach {
            assertTrue("missing column $it", created.contains(it))
        }
    }

    @Test
    fun `placements are indexed by document, since that is how they are looked up`() {
        val statements = statementsFrom(PdfDatabase.MIGRATION_9_10)

        assertTrue(
            statements.any { it.contains("CREATE INDEX") && it.contains("pdfPath") }
        )
    }

    @Test
    fun `the saved signature a placement came from may be absent`() {
        // The placement keeps its own copy of the image, so deleting the saved
        // signature must not take the placement with it.
        val created = statementsFrom(PdfDatabase.MIGRATION_9_10)
            .first { it.contains("CREATE TABLE") }
        val savedIdColumn = created.lines().first { it.contains("savedSignatureId") }

        assertTrue(!savedIdColumn.uppercase().contains("NOT NULL"))
    }
    // endregion
}
