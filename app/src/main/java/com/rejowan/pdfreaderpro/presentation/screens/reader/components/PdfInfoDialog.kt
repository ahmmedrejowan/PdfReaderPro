package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentAmber = Color(0xFFFFB74D)

data class PdfInfo(
    val title: String?,
    val author: String?,
    val subject: String? = null,
    val creator: String? = null,
    val producer: String? = null,
    val creationDate: String? = null,
    val keywords: String? = null,
    val language: String? = null,
    val pdfVersion: String? = null,
    val path: String,
    val pageCount: Int,
    val fileSize: Long,
    val lastModified: Long,
    val isLinearized: Boolean = false,
    val isEncrypted: Boolean = false,
    val encryptionType: String? = null,
    val hasForms: Boolean = false,
    val hasSignatures: Boolean = false,
    val hasXfa: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PdfInfoDialog(
    info: PdfInfo,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        PdfInfoSideSheet(info = info, onDismiss = onDismiss)
    } else {
        PdfInfoBottomSheet(info = info, onDismiss = onDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfInfoBottomSheet(
    info: PdfInfo,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        PdfInfoContent(
            info = info,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PdfInfoSideSheet(
    info: PdfInfo,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    }
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                PdfInfoContent(
                    info = info,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = systemBarsPadding.calculateTopPadding(),
                            bottom = systemBarsPadding.calculateBottomPadding()
                        )
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PdfInfoContent(
    info: PdfInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentPurple.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = AccentPurple
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.title ?: File(info.path).nameWithoutExtension,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Document Information",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatChip(
                label = "${info.pageCount} pages",
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = formatFileSize(info.fileSize),
                color = AccentTeal,
                modifier = Modifier.weight(1f)
            )
            info.pdfVersion?.let {
                StatChip(
                    label = "PDF $it",
                    color = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Document Section
        if (!info.subject.isNullOrBlank() || !info.keywords.isNullOrBlank() || !info.language.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoSection(
                title = "Document",
                icon = Icons.Rounded.Info,
                accentColor = AccentPurple
            ) {
                if (!info.subject.isNullOrBlank()) {
                    InfoItem("Subject", info.subject)
                }
                if (!info.keywords.isNullOrBlank()) {
                    InfoItem("Keywords", info.keywords)
                }
                if (!info.language.isNullOrBlank()) {
                    InfoItem("Language", info.language)
                }
            }
        }

        // Author Section
        if (!info.author.isNullOrBlank() || !info.creator.isNullOrBlank() || !info.producer.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoSection(
                title = "Author & Creator",
                icon = Icons.Rounded.Person,
                accentColor = AccentBlue
            ) {
                if (!info.author.isNullOrBlank()) {
                    InfoItem("Author", info.author)
                }
                if (!info.creator.isNullOrBlank()) {
                    InfoItem("Creator", info.creator)
                }
                if (!info.producer.isNullOrBlank()) {
                    InfoItem("Producer", info.producer)
                }
            }
        }

        // Dates Section
        Spacer(modifier = Modifier.height(12.dp))
        InfoSection(
            title = "Dates",
            icon = Icons.Rounded.Schedule,
            accentColor = AccentTeal
        ) {
            if (!info.creationDate.isNullOrBlank() && info.creationDate != "null") {
                InfoItem("Created", formatPdfDate(info.creationDate))
            }
            InfoItem("Modified", formatDate(info.lastModified))
        }

        // Security & Features Section
        if (info.isEncrypted || info.hasForms || info.hasSignatures || info.hasXfa || info.isLinearized) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoSection(
                title = "Security & Features",
                icon = Icons.Rounded.Lock,
                accentColor = AccentAmber
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (info.isEncrypted) {
                        FeatureTag(
                            text = info.encryptionType?.let { "Encrypted ($it)" } ?: "Encrypted",
                            color = Color(0xFFEF5350)
                        )
                    }
                    if (info.hasForms) {
                        FeatureTag(text = "Forms", color = AccentBlue)
                    }
                    if (info.hasSignatures) {
                        FeatureTag(text = "Signatures", color = AccentTeal)
                    }
                    if (info.hasXfa) {
                        FeatureTag(text = "XFA", color = AccentPurple)
                    }
                    if (info.isLinearized) {
                        FeatureTag(text = "Web Optimized", color = AccentAmber)
                    }
                }
            }
        }

        // Location Section
        Spacer(modifier = Modifier.height(12.dp))
        InfoSection(
            title = "Location",
            icon = Icons.Rounded.Folder,
            accentColor = Color(0xFF78909C)
        ) {
            Text(
                text = info.path,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@Composable
private fun InfoSection(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = accentColor
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun FeatureTag(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatPdfDate(pdfDate: String): String {
    return try {
        val cleaned = pdfDate.removePrefix("D:")
        if (cleaned.length >= 8) {
            val year = cleaned.substring(0, 4)
            val month = cleaned.substring(4, 6)
            val day = cleaned.substring(6, 8)
            val hour = if (cleaned.length >= 10) cleaned.substring(8, 10) else "00"
            val minute = if (cleaned.length >= 12) cleaned.substring(10, 12) else "00"

            val inputFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse("$year$month$day$hour$minute")
            date?.let { outputFormat.format(it) } ?: pdfDate
        } else {
            pdfDate
        }
    } catch (e: Exception) {
        pdfDate
    }
}
