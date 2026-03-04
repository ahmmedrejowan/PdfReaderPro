package com.rejowan.pdfreaderpro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.rejowan.pdfreaderpro.domain.model.GithubRelease
import com.rejowan.pdfreaderpro.domain.repository.UpdateRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Implementation of UpdateRepository using Ktor for API calls and DataStore for preferences.
 */
class UpdateRepositoryImpl(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>
) : UpdateRepository {

    private object Keys {
        val LAST_CHECK_TIME = longPreferencesKey("update_last_check_time")
        val SKIPPED_VERSIONS = stringSetPreferencesKey("update_skipped_versions")
    }

    override suspend fun getLatestRelease(owner: String, repo: String): Result<GithubRelease?> {
        return try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            Timber.d("Fetching latest release from: $url")

            val response = httpClient.get(url) {
                header("Accept", "application/vnd.github.v3+json")
                header("User-Agent", "PdfReaderPro-Android")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val release = response.body<GithubRelease>()
                    Timber.d("Found release: ${release.tagName}")
                    Result.success(release)
                }
                HttpStatusCode.NotFound -> {
                    Timber.d("No releases found")
                    Result.success(null)
                }
                else -> {
                    Timber.e("Unexpected status: ${response.status}")
                    Result.failure(Exception("Failed to fetch release: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching release")
            Result.failure(e)
        }
    }

    override suspend fun checkForUpdate(
        owner: String,
        repo: String,
        currentVersion: String
    ): Result<GithubRelease?> {
        return getLatestRelease(owner, repo).map { release ->
            if (release != null) {
                val latestVersion = release.version
                Timber.d("Comparing versions: current=$currentVersion, latest=$latestVersion")

                if (isNewerVersion(latestVersion, currentVersion)) {
                    Timber.d("Update available: $latestVersion")
                    release
                } else {
                    Timber.d("App is up to date")
                    null
                }
            } else {
                null
            }
        }
    }

    override suspend fun getLastCheckTime(): Long {
        return dataStore.data.first()[Keys.LAST_CHECK_TIME] ?: 0L
    }

    override suspend fun setLastCheckTime(time: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_CHECK_TIME] = time
        }
    }

    override suspend fun shouldSkipVersion(version: String): Boolean {
        val skipped = dataStore.data.first()[Keys.SKIPPED_VERSIONS] ?: emptySet()
        return version in skipped
    }

    override suspend fun skipVersion(version: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.SKIPPED_VERSIONS] ?: emptySet()
            prefs[Keys.SKIPPED_VERSIONS] = current + version
        }
    }

    override suspend fun clearSkippedVersions() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.SKIPPED_VERSIONS)
        }
    }

    /**
     * Compares two semantic version strings.
     * @return true if [newVersion] is greater than [currentVersion]
     */
    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        try {
            val newParts = parseVersion(newVersion)
            val currentParts = parseVersion(currentVersion)

            for (i in 0 until maxOf(newParts.size, currentParts.size)) {
                val newPart = newParts.getOrElse(i) { 0 }
                val currentPart = currentParts.getOrElse(i) { 0 }

                when {
                    newPart > currentPart -> return true
                    newPart < currentPart -> return false
                }
            }
            return false
        } catch (e: Exception) {
            Timber.e(e, "Error comparing versions: $newVersion vs $currentVersion")
            return false
        }
    }

    /**
     * Parses a version string into a list of integers.
     * Handles formats like "2.0.0", "v2.0.0", "2.0.0-beta1"
     */
    private fun parseVersion(version: String): List<Int> {
        return version
            .removePrefix("v")
            .removePrefix("V")
            .split("-")[0] // Remove pre-release suffix
            .split(".")
            .mapNotNull { it.toIntOrNull() }
    }
}
