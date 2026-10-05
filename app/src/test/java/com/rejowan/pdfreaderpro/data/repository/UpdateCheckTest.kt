package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Covers the update check against a stand-in GitHub.
 *
 * The comparison decides whether the user is told there is a new version, so
 * getting it wrong either nags people who are up to date or leaves everyone on an
 * old build. The release payload is the real shape GitHub returns, including
 * fields the app does not read, since those must not break the parse.
 */
class UpdateCheckTest {

    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        dataStore = mockk(relaxed = true)
        every { dataStore.data } returns flowOf(preferencesOf())
    }

    private fun release(tag: String) = """
        {
          "tag_name": "$tag",
          "name": "Release $tag",
          "body": "What changed",
          "published_at": "2026-01-01T00:00:00Z",
          "html_url": "https://github.com/owner/repo/releases/$tag",
          "draft": false,
          "prerelease": false,
          "assets": [
            {
              "name": "app-release.apk",
              "browser_download_url": "https://example.invalid/app.apk",
              "size": 12582912,
              "download_count": 42
            }
          ]
        }
    """.trimIndent()

    private fun repositoryReturning(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = release("v2.5.0")
    ): UpdateRepositoryImpl {
        val engine = MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return UpdateRepositoryImpl(client, dataStore)
    }

    private fun repositoryFailing(cause: Throwable): UpdateRepositoryImpl {
        val client = HttpClient(MockEngine { throw cause })
        return UpdateRepositoryImpl(client, dataStore)
    }

    // region Fetching the latest release
    @Test
    fun `a release is read back with the parts the app shows`() = runTest {
        val result = repositoryReturning().getLatestRelease("owner", "repo")

        val release = result.getOrThrow()!!
        assertEquals("v2.5.0", release.tagName)
        assertEquals("Release v2.5.0", release.name)
        assertEquals(1, release.assets.size)
        assertEquals("app-release.apk", release.assets.single().name)
    }

    @Test
    fun `fields the app does not read do not break the parse`() = runTest {
        // GitHub adds fields freely, and a strict parse would break the update
        // check on a payload change the app has no interest in.
        val result = repositoryReturning(body = release("v2.5.0"))
        assertTrue(result.getLatestRelease("owner", "repo").isSuccess)
    }

    @Test
    fun `a project with no releases yet is not an error`() = runTest {
        val result = repositoryReturning(status = HttpStatusCode.NotFound, body = "{}")
            .getLatestRelease("owner", "repo")

        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `a server error is a failure, not an absent release`() = runTest {
        // Reporting "no update" on a rate limit would quietly stop update checks.
        val engine = MockEngine { respondError(HttpStatusCode.TooManyRequests) }
        val repository = UpdateRepositoryImpl(HttpClient(engine), dataStore)

        assertTrue(repository.getLatestRelease("owner", "repo").isFailure)
    }

    @Test
    fun `no network is a failure rather than a crash`() = runTest {
        val repository = repositoryFailing(IOException("network unreachable"))

        assertTrue(repository.getLatestRelease("owner", "repo").isFailure)
    }

    @Test
    fun `a payload that is not a release is a failure`() = runTest {
        val result = repositoryReturning(body = """{"unexpected":true}""")
            .getLatestRelease("owner", "repo")

        assertTrue(result.isFailure)
    }
    // endregion

    // region Deciding whether there is an update
    @Test
    fun `a newer release is offered`() = runTest {
        val result = repositoryReturning(body = release("v2.5.0"))
            .checkForUpdate("owner", "repo", currentVersion = "2.4.0")

        assertEquals("2.5.0", result.getOrThrow()?.version)
    }

    @Test
    fun `the same version is not offered`() = runTest {
        val result = repositoryReturning(body = release("v2.4.0"))
            .checkForUpdate("owner", "repo", currentVersion = "2.4.0")

        assertNull(result.getOrThrow())
    }

    @Test
    fun `an older release is not offered`() = runTest {
        // Which matters for anyone running a build newer than the last release.
        val result = repositoryReturning(body = release("v2.3.0"))
            .checkForUpdate("owner", "repo", currentVersion = "2.4.0")

        assertNull(result.getOrThrow())
    }

    @Test
    fun `a tag with a v prefix still compares as a version`() = runTest {
        val result = repositoryReturning(body = release("V2.5.0"))
            .checkForUpdate("owner", "repo", currentVersion = "2.4.0")

        assertEquals("2.5.0", result.getOrThrow()?.version)
    }

    @Test
    fun `a release prefixed tag still compares as a version`() = runTest {
        val result = repositoryReturning(body = release("release-2.5.0"))
            .checkForUpdate("owner", "repo", currentVersion = "2.4.0")

        assertEquals("2.5.0", result.getOrThrow()?.version)
    }

    @Test
    fun `a failed fetch stays a failure through the check`() = runTest {
        val repository = repositoryFailing(IOException("network unreachable"))

        assertTrue(repository.checkForUpdate("owner", "repo", "2.4.0").isFailure)
    }

    @Test
    fun `no releases means nothing to offer`() = runTest {
        val result = repositoryReturning(status = HttpStatusCode.NotFound, body = "{}")
            .checkForUpdate("owner", "repo", "2.4.0")

        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }
    // endregion
}
