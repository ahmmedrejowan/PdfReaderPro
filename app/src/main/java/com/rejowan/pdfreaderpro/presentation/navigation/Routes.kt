package com.rejowan.pdfreaderpro.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 * Each route is a data class or object that can be serialized.
 */

@Serializable
object Onboarding

@Serializable
object Home

@Serializable
data class Search(val query: String = "")

@Serializable
object Settings

@Serializable
data class FolderDetail(
    val folderPath: String,
    val folderName: String
)

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
data class ToolRotate(
    val filePath: String
)

@Serializable
data class ToolReorder(
    val filePath: String
)

@Serializable
data class ToolLock(
    val filePath: String
)

@Serializable
data class ToolUnlock(
    val filePath: String
)

@Serializable
data class ToolRemovePages(
    val filePath: String
)

@Serializable
data class ToolWatermark(
    val filePath: String
)

@Serializable
data class ToolPageNumbers(
    val filePath: String
)

@Serializable
object ToolImageToPdf

@Serializable
data class ToolPdfToImage(
    val filePath: String
)

@Serializable
data class ToolResult(
    val toolType: String,
    val outputPath: String? = null,
    val success: Boolean = true,
    val message: String? = null
)
