package com.rejowan.pdfreaderpro.presentation.screens.tools

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.annotation.StringRes
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToCompressTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToMergeTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToLockTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReorderTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToUnlockTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToWatermarkTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToPageNumbersTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToRemovePagesTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToRotateTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToSplitTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToImageToPdfTool
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToPdfToImageTool
import kotlinx.coroutines.delay

// Accent colors (consistent with app design system)
private val AccentPurple = Color(0xFF9575CD)
private val AccentBlue = Color(0xFF64B5F6)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentAmber = Color(0xFFFFB74D)
private val AccentGreen = Color(0xFF81C784)

enum class ToolCategory(@StringRes val titleRes: Int, val accentColor: Color) {
    ORGANIZE(R.string.tool_category_organize, AccentPurple),
    EDIT(R.string.tool_category_edit, AccentBlue),
    SECURITY(R.string.tool_category_security, AccentAmber),
    CONVERT(R.string.tool_category_convert, AccentTeal)
}

data class PdfTool(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val category: ToolCategory,
    val isEnabled: Boolean = false
)

private val pdfTools = listOf(
    PdfTool(
        "merge",
        R.string.tool_merge_pdfs,
        R.string.tool_merge_desc,
        Icons.AutoMirrored.Filled.CallMerge,
        ToolCategory.ORGANIZE,
        isEnabled = true
    ),
    PdfTool(
        "split",
        R.string.tool_split_pdf,
        R.string.tool_split_desc,
        Icons.AutoMirrored.Filled.CallSplit,
        ToolCategory.ORGANIZE,
        isEnabled = true
    ),
    PdfTool(
        "compress",
        R.string.tool_compress_pdf,
        R.string.tool_compress_desc,
        Icons.Default.Compress,
        ToolCategory.ORGANIZE,
        isEnabled = true
    ),
    PdfTool(
        "rotate",
        R.string.tool_rotate_pages,
        R.string.tool_rotate_desc,
        Icons.AutoMirrored.Filled.RotateRight,
        ToolCategory.ORGANIZE,
        isEnabled = true
    ),
    PdfTool(
        "reorder",
        R.string.tool_reorder_pages,
        R.string.tool_reorder_desc,
        Icons.Default.Reorder,
        ToolCategory.ORGANIZE,
        isEnabled = true
    ),
    PdfTool(
        "remove_pages",
        R.string.tool_remove_pages,
        R.string.tool_remove_pages_desc,
        Icons.Default.DeleteSweep,
        ToolCategory.EDIT,
        isEnabled = true
    ),
    PdfTool(
        "watermark",
        R.string.tool_watermark,
        R.string.tool_watermark_desc,
        Icons.Default.WaterDrop,
        ToolCategory.EDIT,
        isEnabled = true
    ),
    PdfTool(
        "page_numbers",
        R.string.tool_page_numbers,
        R.string.tool_page_numbers_desc,
        Icons.Default.FormatListNumbered,
        ToolCategory.EDIT,
        isEnabled = true
    ),
    PdfTool(
        "lock_pdf",
        R.string.tool_lock_pdf,
        R.string.tool_lock_desc,
        Icons.Default.Lock,
        ToolCategory.SECURITY,
        isEnabled = true
    ),
    PdfTool(
        "unlock_pdf",
        R.string.tool_unlock_pdf,
        R.string.tool_unlock_desc,
        Icons.Default.LockOpen,
        ToolCategory.SECURITY,
        isEnabled = true
    ),
    PdfTool(
        "img_to_pdf",
        R.string.tool_image_to_pdf,
        R.string.tool_image_to_pdf_desc,
        Icons.Default.Image,
        ToolCategory.CONVERT,
        isEnabled = true
    ),
    PdfTool(
        "pdf_to_img",
        R.string.tool_pdf_to_image,
        R.string.tool_pdf_to_image_desc,
        Icons.Default.Photo,
        ToolCategory.CONVERT,
        isEnabled = true
    ),
)

@Composable
fun ToolsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 80.dp)
    ) {
        var animationIndex = 0

        ToolCategory.entries.forEach { category ->
            val toolsInCategory = pdfTools.filter { it.category == category }

            if (toolsInCategory.isNotEmpty()) {
                // Section label
                SectionLabel(
                    text = stringResource(category.titleRes),
                    delay = animationIndex * 50
                )
                animationIndex++

                Spacer(modifier = Modifier.height(8.dp))

                // Tool items
                toolsInCategory.forEach { tool ->
                    ToolItem(
                        tool = tool,
                        accentColor = category.accentColor,
                        animationDelay = animationIndex * 50,
                        onClick = {
                            when (tool.id) {
                                "merge" -> navController.navigateToMergeTool()
                                "split" -> navController.navigateToSplitTool("")
                                "compress" -> navController.navigateToCompressTool("")
                                "rotate" -> navController.navigateToRotateTool("")
                                "reorder" -> navController.navigateToReorderTool("")
                                "lock_pdf" -> navController.navigateToLockTool("")
                                "unlock_pdf" -> navController.navigateToUnlockTool("")
                                "remove_pages" -> navController.navigateToRemovePagesTool("")
                                "watermark" -> navController.navigateToWatermarkTool("")
                                "page_numbers" -> navController.navigateToPageNumbersTool("")
                                "img_to_pdf" -> navController.navigateToImageToPdfTool()
                                "pdf_to_img" -> navController.navigateToPdfToImageTool("")
                            }
                        }
                    )
                    animationIndex++

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    delay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "section alpha"
    )

    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.5f
        ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun ToolItem(
    tool: PdfTool,
    accentColor: Color,
    animationDelay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "tool scale"
    )

    val effectiveAccentColor = if (tool.isEnabled) accentColor else accentColor.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (tool.isEnabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = accentColor),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = effectiveAccentColor.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp),
                    tint = effectiveAccentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(tool.nameRes),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (tool.isEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )

                    // Coming Soon badge
                    if (!tool.isEnabled) {
                        ComingSoonBadge()
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(tool.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tool.isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }

            // Arrow for enabled tools
            if (tool.isEnabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ComingSoonBadge() {
    Box(
        modifier = Modifier
            .background(
                color = AccentAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.coming_soon),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = AccentAmber
        )
    }
}
