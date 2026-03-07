package com.rejowan.pdfreaderpro.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rejowan.pdfreaderpro.util.PdfThumbnailManager

private val PdfRed = Color(0xFFE53935)
private val PdfRedLight = Color(0xFFFF6F60)

@Composable
fun PdfThumbnail(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    pdfPath: String? = null,
    pageCount: Int? = null
) {
    val context = LocalContext.current
    var thumbnail by remember(pdfPath) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pdfPath) { mutableStateOf(pdfPath != null) }

    LaunchedEffect(pdfPath) {
        if (pdfPath != null) {
            isLoading = true
            thumbnail = PdfThumbnailManager.getThumbnail(context, pdfPath)
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            thumbnail != null -> {
                // Show actual thumbnail
                Image(
                    bitmap = requireNotNull(thumbnail).asImageBitmap(),
                    contentDescription = "PDF thumbnail",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PdfRedLight.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.4f),
                        color = PdfRed,
                        strokeWidth = 2.dp
                    )
                }
            }
            else -> {
                // Fallback placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PdfRedLight.copy(alpha = 0.15f)),
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
}

@Composable
fun PdfThumbnailGrid(
    modifier: Modifier = Modifier,
    pdfPath: String? = null,
    pageCount: Int? = null
) {
    val context = LocalContext.current
    var thumbnail by remember(pdfPath) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pdfPath) { mutableStateOf(pdfPath != null) }

    LaunchedEffect(pdfPath) {
        if (pdfPath != null) {
            isLoading = true
            thumbnail = PdfThumbnailManager.getThumbnail(context, pdfPath)
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            thumbnail != null -> {
                // Show actual thumbnail
                Image(
                    bitmap = requireNotNull(thumbnail).asImageBitmap(),
                    contentDescription = "PDF thumbnail",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PdfRedLight.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = PdfRed,
                        strokeWidth = 2.dp
                    )
                }
            }
            else -> {
                // Fallback placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PdfRedLight.copy(alpha = 0.12f)),
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
    }
}
