package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PdfRed = Color(0xFFE53935)
private val PdfRedLight = Color(0xFFFF6F60)
private val PdfRedDark = Color(0xFFAB000D)

@Composable
fun PdfThumbnail(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    pageCount: Int? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Document shape with folded corner
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PdfRedLight.copy(alpha = 0.15f),
                                PdfRed.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .drawBehind {
                        // Folded corner effect
                        val cornerSize = size.toPx() * 0.25f
                        val path = Path().apply {
                            moveTo(this@drawBehind.size.width - cornerSize, 0f)
                            lineTo(this@drawBehind.size.width, cornerSize)
                            lineTo(this@drawBehind.size.width, 0f)
                            close()
                        }
                        drawPath(path, PdfRed.copy(alpha = 0.3f))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.22f).sp,
                            letterSpacing = 1.sp
                        ),
                        color = PdfRed
                    )
                    if (pageCount != null) {
                        Text(
                            text = "$pageCount pg",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = (size.value * 0.16f).sp
                            ),
                            color = PdfRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PdfThumbnailGrid(
    modifier: Modifier = Modifier,
    pageCount: Int? = null
) {
    Box(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PdfRedLight.copy(alpha = 0.12f),
                            PdfRed.copy(alpha = 0.18f)
                        )
                    )
                )
                .drawBehind {
                    // Folded corner
                    val cornerSize = 24.dp.toPx()
                    val path = Path().apply {
                        moveTo(this@drawBehind.size.width - cornerSize, 0f)
                        lineTo(this@drawBehind.size.width, cornerSize)
                        lineTo(this@drawBehind.size.width, 0f)
                        close()
                    }
                    drawPath(path, PdfRed.copy(alpha = 0.25f))
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "PDF",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PdfRed
                )
                if (pageCount != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PdfRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$pageCount pages",
                            style = MaterialTheme.typography.labelSmall,
                            color = PdfRed.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
