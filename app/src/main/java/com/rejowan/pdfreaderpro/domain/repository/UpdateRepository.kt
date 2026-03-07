package com.rejowan.pdfreaderpro.domain.repository

import com.rejowan.pdfreaderpro.domain.model.GithubRelease

/**
 * Repository for checking GitHub releases and managing update preferences.
 */
interface UpdateRepository {
    /**
     * Fetches the latest release from GitHub.
     * @param owner The repository owner (e.g., "ahmmedrejowan")
     * @param repo The repository name (e.g., "PdfReaderPro")
     * @return Result containing the latest release or null if none found
     */
    suspend fun getLatestRelease(owner: String, repo: String): Result<GithubRelease?>

    /**
     * Checks if an update is available by comparing current version with latest release.
     * @param owner The repository owner
     * @param repo The repository name
     * @param currentVersion The current app version
     * @return Result containing the release if update is available, null if up to date
     */
    suspend fun checkForUpdate(
        owner: String,
        repo: String,
        currentVersion: String
    ): Result<GithubRelease?>

    /**
     * Gets the timestamp of the last update check.
     * @return Timestamp in milliseconds, or 0 if never checked
     */
    suspend fun getLastCheckTime(): Long

    /**
     * Sets the timestamp of the last update check.
     * @param time Timestamp in milliseconds
     */
    suspend fun setLastCheckTime(time: Long)

    /**
     * Checks if a specific version should be skipped.
     * @param version The version to check
     * @return True if the version should be skipped
     */
    suspend fun shouldSkipVersion(version: String): Boolean

    /**
     * Marks a version to be skipped.
     * @param version The version to skip
     */
    suspend fun skipVersion(version: String)

    /**
     * Clears all skipped versions.
     */
    suspend fun clearSkippedVersions()
}
