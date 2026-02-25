package com.rejowan.pdfreaderpro.presentation.screens.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

enum class ToolCategory(val title: String) {
    ORGANIZE("Organize"),
    CONVERT("Convert"),
    EXTRACT("Extract")
}

data class PdfTool(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: ToolCategory,
    val isEnabled: Boolean = false // Coming soon by default
)

private val pdfTools = listOf(
    PdfTool("merge", "Merge PDFs", "Combine multiple PDFs", Icons.AutoMirrored.Filled.CallMerge, ToolCategory.ORGANIZE),
    PdfTool("split", "Split PDF", "Split into multiple files", Icons.AutoMirrored.Filled.CallSplit, ToolCategory.ORGANIZE),
    PdfTool("compress", "Compress", "Reduce file size", Icons.Default.Compress, ToolCategory.ORGANIZE),
    PdfTool("rotate", "Rotate Pages", "Rotate pages", Icons.AutoMirrored.Filled.RotateRight, ToolCategory.ORGANIZE),
    PdfTool("img_to_pdf", "Image to PDF", "Convert images", Icons.Default.Image, ToolCategory.CONVERT),
    PdfTool("pdf_to_img", "PDF to Images", "Export as images", Icons.Default.Photo, ToolCategory.CONVERT),
    PdfTool("extract", "Extract Pages", "Extract specific pages", Icons.Default.SelectAll, ToolCategory.EXTRACT),
    PdfTool("reorder", "Reorder Pages", "Rearrange pages", Icons.Default.Reorder, ToolCategory.EXTRACT),
)

@Composable
fun ToolsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ToolCategory.entries.forEach { category ->
            val toolsInCategory = pdfTools.filter { it.category == category }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(toolsInCategory) { tool ->
                ToolCard(
                    tool = tool,
                    onClick = {
                        // TODO: Navigate to tool screen when implemented
                    }
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: PdfTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = tool.isEnabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (tool.isEnabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (tool.isEnabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tool.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (tool.isEnabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )

            if (!tool.isEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Coming Soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
