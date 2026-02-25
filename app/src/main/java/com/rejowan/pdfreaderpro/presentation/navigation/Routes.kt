package com.rejowan.pdfreaderpro.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 * Each route is a data class or object that can be serialized.
 */

@Serializable
object Splash

@Serializable
object Onboarding

@Serializable
object Home

@Serializable
data class Search(val query: String = "")

@Serializable
object Settings

@Serializable
data class Reader(
    val path: String,
    val page: Int = 0,
    val fromIntent: Boolean = false
)

@Serializable
object Tools

@Serializable
data class ToolMerge(
    val selectedFiles: List<String> = emptyList()
)

@Serializable
data class ToolSplit(
    val filePath: String
)

@Serializable
data class ToolCompress(
    val filePath: String
)

@Serializable
data class ToolResult(
    val toolType: String,
    val outputPath: String? = null,
    val success: Boolean = true,
    val message: String? = null
)
