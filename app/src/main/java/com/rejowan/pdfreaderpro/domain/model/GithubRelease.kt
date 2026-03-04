package com.rejowan.pdfreaderpro.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a GitHub release from the GitHub API.
 */
@Serializable
data class GithubRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String,
    @SerialName("body")
    val body: String,
    @SerialName("published_at")
    val publishedAt: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("assets")
    val assets: List<ReleaseAsset>
) {
    /**
     * Extracts the version number from the tag name.
     * Strips common prefixes like "v" or "release-".
     */
    val version: String
        get() = tagName
            .removePrefix("v")
            .removePrefix("V")
            .removePrefix("release-")
            .removePrefix("Release-")
            .trim()
}

/**
 * Represents an asset (downloadable file) attached to a release.
 */
@Serializable
data class ReleaseAsset(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val downloadUrl: String,
    @SerialName("size")
    val size: Long
) {
    /**
     * Returns a human-readable file size string.
     */
    val formattedSize: String
        get() {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format("%.1f MB", mb)
                kb >= 1.0 -> String.format("%.1f KB", kb)
                else -> "$size bytes"
            }
        }

    /**
     * Checks if this asset is an APK file.
     */
    val isApk: Boolean
        get() = name.endsWith(".apk", ignoreCase = true)
}

/**
 * Represents the state of the update checking process.
 */
sealed class UpdateState {
    /** Initial state, no update check has been performed. */
    data object Idle : UpdateState()

    /** Currently checking for updates. */
    data object Checking : UpdateState()

    /** A new update is available. */
    data class Available(
        val release: GithubRelease,
        val currentVersion: String
    ) : UpdateState()

    /** The app is up to date. */
    data object UpToDate : UpdateState()

    /** An error occurred while checking for updates. */
    data class Error(val message: String) : UpdateState()
}
