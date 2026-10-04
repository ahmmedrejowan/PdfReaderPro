package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R

/**
 * Captures a signature in the app's own UI.
 *
 * This replaces the viewer's built in dialog, whose drawing surface records only
 * a fraction of the distance a finger actually travels and so is unusable on a
 * touch screen. Drawing here is a plain Compose [Canvas], which tracks the
 * pointer directly.
 *
 * Hands back a transparent PNG bitmap; the caller decides whether to remember it.
 */
@Composable
fun SignatureCapture(
    onCaptured: (Bitmap, remember: Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableStateOf(0) }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val current = remember { mutableStateListOf<Offset>() }
    var typed by remember { mutableStateOf("") }
    var pickedImage by remember { mutableStateOf<Bitmap?>(null) }
    var rememberIt by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val density = LocalDensity.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        pickedImage = runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                android.graphics.BitmapFactory.decodeStream(it)
            }
        }.getOrNull()
    }

    val hasContent = when (tab) {
        0 -> strokes.isNotEmpty() || current.isNotEmpty()
        1 -> typed.isNotBlank()
        else -> pickedImage != null
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.sign_tab_draw)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.sign_tab_type)) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.sign_tab_image)) })
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            when (tab) {
                0 -> Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    current.clear()
                                    current.add(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    current.add(change.position)
                                },
                                onDragEnd = {
                                    if (current.size > 1) strokes.add(current.toList())
                                    current.clear()
                                }
                            )
                        }
                ) {
                    val stroke = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                    (strokes + listOf(current.toList())).forEach { points ->
                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(path, Color.Black, style = stroke)
                        }
                    }
                }

                1 -> Text(
                    text = typed.ifBlank { stringResource(R.string.sign_type_placeholder) },
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Cursive,
                    fontStyle = FontStyle.Italic,
                    color = if (typed.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                else -> {
                    val picked = pickedImage
                    if (picked != null) {
                        androidx.compose.foundation.Image(
                            bitmap = picked.asImageBitmapSafe(),
                            contentDescription = null
                        )
                    } else {
                        TextButton(onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) { Text(stringResource(R.string.sign_pick_image)) }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (tab == 1) {
            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it },
                label = { Text(stringResource(R.string.sign_type_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                strokes.clear(); current.clear(); typed = ""; pickedImage = null
            }) { Text(stringResource(R.string.sign_clear)) }

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onCancel) { Text(stringResource(R.string.sign_cancel)) }

            Spacer(Modifier.width(8.dp))

            Button(
                enabled = hasContent,
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    val bitmap = when (tab) {
                        0 -> renderStrokes(strokes, with(density) { 6.dp.toPx() })
                        1 -> renderText(typed)
                        else -> pickedImage
                    }
                    if (bitmap != null) onCaptured(bitmap, rememberIt)
                }
            ) { Text(stringResource(R.string.sign_place)) }
        }
    }
}

private fun Bitmap.asImageBitmapSafe() = this.asImageBitmap()

/**
 * Draws the captured strokes onto a transparent bitmap, cropped to what was
 * actually drawn so the placed signature has no dead space around it.
 */
private fun renderStrokes(strokes: List<List<Offset>>, strokeWidth: Float): Bitmap? {
    val points = strokes.flatten()
    if (points.isEmpty()) return null

    val pad = strokeWidth
    val minX = points.minOf { it.x } - pad
    val minY = points.minOf { it.y } - pad
    val width = (points.maxOf { it.x } + pad - minX).toInt().coerceAtLeast(1)
    val height = (points.maxOf { it.y } + pad - minY).toInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    strokes.forEach { stroke ->
        if (stroke.size < 2) return@forEach
        val path = AndroidPath().apply {
            moveTo(stroke.first().x - minX, stroke.first().y - minY)
            stroke.drop(1).forEach { lineTo(it.x - minX, it.y - minY) }
        }
        canvas.drawPath(path, paint)
    }
    return bitmap
}

/** Renders typed text in a script face onto a transparent bitmap. */
private fun renderText(text: String): Bitmap? {
    if (text.isBlank()) return null
    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        textSize = 120f
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.SERIF,
            android.graphics.Typeface.ITALIC
        )
    }
    val bounds = android.graphics.Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    val pad = 24
    val width = (bounds.width() + pad * 2).coerceAtLeast(1)
    val height = (bounds.height() + pad * 2).coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    AndroidCanvas(bitmap).drawText(
        text,
        (pad - bounds.left).toFloat(),
        (pad - bounds.top).toFloat(),
        paint
    )
    return bitmap
}
