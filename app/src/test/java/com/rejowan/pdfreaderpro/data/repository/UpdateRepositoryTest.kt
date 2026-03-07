package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringSetPreferencesKey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UpdateRepositoryImpl.
 * Tests DataStore read operations for update checking preferences.
 * Note: Write operations (edit) and HTTP operations are tested via integration tests.
 */
class UpdateRepositoryTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var httpClient: io.ktor.client.HttpClient

    companion object {
        private val LAST_CHECK_TIME = longPreferencesKey("update_last_check_time")
        private val SKIPPED_VERSIONS = stringSetPreferencesKey("update_skipped_versions")
    }

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        httpClient = mockk(relaxed = true)
    }

    private fun createRepository(preferences: Preferences = preferencesOf()): UpdateRepositoryImpl {
        every { dataStore.data } returns flowOf(preferences)
        return UpdateRepositoryImpl(httpClient, dataStore)
    }

    // region getLastCheckTime Tests
    @Test
    fun `getLastCheckTime returns stored time`() = runTest {
        val expectedTime = 1234567890L
        val prefs = preferencesOf(LAST_CHECK_TIME to expectedTime)
        val repository = createRepository(prefs)

        val result = repository.getLastCheckTime()

        assertEquals(expectedTime, result)
    }

    @Test
    fun `getLastCheckTime returns 0 when not set`() = runTest {
        val repository = createRepository()

        val result = repository.getLastCheckTime()

        assertEquals(0L, result)
    }

    @Test
    fun `getLastCheckTime returns large timestamp correctly`() = runTest {
        val largeTimestamp = 1709251200000L // March 2024
        val prefs = preferencesOf(LAST_CHECK_TIME to largeTimestamp)
        val repository = createRepository(prefs)

        val result = repository.getLastCheckTime()

        assertEquals(largeTimestamp, result)
    }

    @Test
    fun `getLastCheckTime handles zero value`() = runTest {
        val prefs = preferencesOf(LAST_CHECK_TIME to 0L)
        val repository = createRepository(prefs)

        val result = repository.getLastCheckTime()

        assertEquals(0L, result)
    }

    @Test
    fun `getLastCheckTime handles max long value`() = runTest {
        val maxTime = Long.MAX_VALUE
        val prefs = preferencesOf(LAST_CHECK_TIME to maxTime)
        val repository = createRepository(prefs)

        val result = repository.getLastCheckTime()

        assertEquals(maxTime, result)
    }
    // endregion

    // region shouldSkipVersion Tests
    @Test
    fun `shouldSkipVersion returns true when version is in skipped set`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("2.0.0", "2.1.0"))
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("2.0.0")

        assertTrue(result)
    }

    @Test
    fun `shouldSkipVersion returns true for second version in set`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("2.0.0", "2.1.0", "3.0.0"))
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("2.1.0")

        assertTrue(result)
    }

    @Test
    fun `shouldSkipVersion returns true for third version in set`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("2.0.0", "2.1.0", "3.0.0"))
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("3.0.0")

        assertTrue(result)
    }

    @Test
    fun `shouldSkipVersion returns false when version is not in skipped set`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("2.0.0"))
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("3.0.0")

        assertFalse(result)
    }

    @Test
    fun `shouldSkipVersion returns false when skipped set is empty`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to emptySet())
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("2.0.0")

        assertFalse(result)
    }

    @Test
    fun `shouldSkipVersion returns false when skipped set is not present`() = runTest {
        val repository = createRepository()

        val result = repository.shouldSkipVersion("2.0.0")

        assertFalse(result)
    }

    @Test
    fun `shouldSkipVersion handles version with v prefix`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("v2.0.0"))
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("v2.0.0")

        assertTrue(result)
    }

    @Test
    fun `shouldSkipVersion is case sensitive`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("v2.0.0"))
        val repository = createRepository(prefs)

        val result = repository.shouldSkipVersion("V2.0.0")

        assertFalse(result)
    }

    @Test
    fun `shouldSkipVersion handles single version in set`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("1.0.0"))
        val repository = createRepository(prefs)

        assertTrue(repository.shouldSkipVersion("1.0.0"))
        assertFalse(repository.shouldSkipVersion("1.0.1"))
    }

    @Test
    fun `shouldSkipVersion handles many versions in set`() = runTest {
        val versions = (1..100).map { "1.0.$it" }.toSet()
        val prefs = preferencesOf(SKIPPED_VERSIONS to versions)
        val repository = createRepository(prefs)

        assertTrue(repository.shouldSkipVersion("1.0.50"))
        assertFalse(repository.shouldSkipVersion("1.0.101"))
    }
    // endregion

    // region Combined State Tests
    @Test
    fun `repository handles both preferences set`() = runTest {
        val prefs = preferencesOf(
            LAST_CHECK_TIME to 1234567890L,
            SKIPPED_VERSIONS to setOf("2.0.0", "2.1.0")
        )
        val repository = createRepository(prefs)

        assertEquals(1234567890L, repository.getLastCheckTime())
        assertTrue(repository.shouldSkipVersion("2.0.0"))
        assertFalse(repository.shouldSkipVersion("3.0.0"))
    }

    @Test
    fun `repository handles only check time set`() = runTest {
        val prefs = preferencesOf(LAST_CHECK_TIME to 9876543210L)
        val repository = createRepository(prefs)

        assertEquals(9876543210L, repository.getLastCheckTime())
        assertFalse(repository.shouldSkipVersion("any.version"))
    }

    @Test
    fun `repository handles only skipped versions set`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("1.0.0"))
        val repository = createRepository(prefs)

        assertEquals(0L, repository.getLastCheckTime())
        assertTrue(repository.shouldSkipVersion("1.0.0"))
    }

    @Test
    fun `repository handles empty preferences`() = runTest {
        val repository = createRepository()

        assertEquals(0L, repository.getLastCheckTime())
        assertFalse(repository.shouldSkipVersion("any.version"))
    }
    // endregion

    // region Edge Cases
    @Test
    fun `shouldSkipVersion handles empty string version`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf(""))
        val repository = createRepository(prefs)

        assertTrue(repository.shouldSkipVersion(""))
        assertFalse(repository.shouldSkipVersion("1.0.0"))
    }

    @Test
    fun `shouldSkipVersion handles special characters`() = runTest {
        val prefs = preferencesOf(SKIPPED_VERSIONS to setOf("2.0.0-beta.1", "2.0.0+build.123"))
        val repository = createRepository(prefs)

        assertTrue(repository.shouldSkipVersion("2.0.0-beta.1"))
        assertTrue(repository.shouldSkipVersion("2.0.0+build.123"))
        assertFalse(repository.shouldSkipVersion("2.0.0"))
    }

    @Test
    fun `getLastCheckTime multiple calls return same value`() = runTest {
        val prefs = preferencesOf(LAST_CHECK_TIME to 1234567890L)
        val repository = createRepository(prefs)

        val result1 = repository.getLastCheckTime()
        val result2 = repository.getLastCheckTime()
        val result3 = repository.getLastCheckTime()

        assertEquals(result1, result2)
        assertEquals(result2, result3)
    }
    // endregion
}
