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
import com.rejowan.pdfreaderpro.util.VersionUtils
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Implementation of UpdateRepository using Ktor for API calls and DataStore for preferences.
 */
class UpdateRepositoryImpl(
    private val httpClient: HttpClient,
    private val dataStore: DataStore<Preferences>
) : UpdateRepository {

    companion object {
        private const val TAG = "UpdateChecker"
    }

    private object Keys {
        val LAST_CHECK_TIME = longPreferencesKey("update_last_check_time")
        val SKIPPED_VERSIONS = stringSetPreferencesKey("update_skipped_versions")
    }

    override suspend fun getLatestRelease(owner: String, repo: String): Result<GithubRelease?> {
        return try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            Timber.tag(TAG).d("=== API REQUEST ===")
            Timber.tag(TAG).d("URL: $url")

            val response = httpClient.get(url) {
                header("Accept", "application/vnd.github.v3+json")
                header("User-Agent", "PdfReaderPro-Android")
            }

            Timber.tag(TAG).d("=== API RESPONSE ===")
            Timber.tag(TAG).d("Status: ${response.status}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val release = response.body<GithubRelease>()
                    Timber.tag(TAG).d("Release tag: ${release.tagName}")
                    Timber.tag(TAG).d("Release name: ${release.name}")
                    Timber.tag(TAG).d("Published: ${release.publishedAt}")
                    Timber.tag(TAG).d("Assets count: ${release.assets.size}")
                    release.assets.forEach { asset ->
                        Timber.tag(TAG).d("  - ${asset.name} (${asset.formattedSize}) isApk=${asset.isApk}")
                    }
                    Result.success(release)
                }
                HttpStatusCode.NotFound -> {
                    Timber.tag(TAG).d("No releases found (404)")
                    Result.success(null)
                }
                else -> {
                    Timber.tag(TAG).e("Unexpected status: ${response.status}")
                    Result.failure(Exception("Failed to fetch release: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "API Error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun checkForUpdate(
        owner: String,
        repo: String,
        currentVersion: String
    ): Result<GithubRelease?> {
        Timber.tag(TAG).d("=== VERSION CHECK ===")
        Timber.tag(TAG).d("Current app version: $currentVersion")

        return getLatestRelease(owner, repo).map { release ->
            if (release != null) {
                val latestVersion = release.version
                Timber.tag(TAG).d("Latest GitHub version: $latestVersion")

                val isNewer = VersionUtils.isNewerVersion(latestVersion, currentVersion)
                Timber.tag(TAG).d("Is newer: $isNewer")

                if (isNewer) {
                    Timber.tag(TAG).d("→ UPDATE AVAILABLE: $currentVersion → $latestVersion")
                    release
                } else {
                    Timber.tag(TAG).d("→ APP IS UP TO DATE")
                    null
                }
            } else {
                Timber.tag(TAG).d("→ No release found")
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
}
