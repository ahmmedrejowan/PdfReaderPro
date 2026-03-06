package com.rejowan.pdfreaderpro.presentation.components.dialogs

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Accent colors
private val AccentOrange = Color(0xFFFF7043)
private val AccentOrangeDark = Color(0xFFE64A19)
private val AccentAmber = Color(0xFFFFB74D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitConfirmSheet(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ExitConfirmSideSheet(onDismiss = onDismiss, onConfirmExit = onConfirmExit)
    } else {
        ExitConfirmBottomSheet(onDismiss = onDismiss, onConfirmExit = onConfirmExit)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitConfirmBottomSheet(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        ExitConfirmContent(
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
            onConfirmExit = {
                scope.launch {
                    sheetState.hide()
                    onConfirmExit()
                }
            },
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun ExitConfirmSideSheet(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
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

        // Side panel
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
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                ExitConfirmContent(
                    onDismiss = onDismiss,
                    onConfirmExit = onConfirmExit,
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

@Composable
private fun ExitConfirmContent(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Staggered animation states
    var showIcon by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showIcon = true
        delay(100)
        showTitle = true
        delay(100)
        showButtons = true
    }

    // Infinite animations for icon
    val infiniteTransition = rememberInfiniteTransition(label = "exit")

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    val outerRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerRingScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated icon section
        val iconAlpha by animateFloatAsState(
            targetValue = if (showIcon) 1f else 0f,
            animationSpec = tween(400),
            label = "iconAlpha"
        )
        val iconScaleAnim by animateFloatAsState(
            targetValue = if (showIcon) 1f else 0.8f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "iconScaleAnim"
        )

        Box(
            modifier = Modifier
                .scale(iconScaleAnim)
                .size(94.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulsing ring
            Box(
                modifier = Modifier
                    .size(87.dp)
                    .scale(outerRingScale)
                    .clip(RoundedCornerShape(35))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentOrange.copy(alpha = ringAlpha * 0.5f),
                                AccentAmber.copy(alpha = ringAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Middle ring
            Box(
                modifier = Modifier
                    .size(67.dp)
                    .scale(iconScale)
                    .clip(RoundedCornerShape(30))
                    .background(AccentOrange.copy(alpha = ringAlpha))
            )

            // Inner icon container
            Surface(
                modifier = Modifier.size(47.dp),
                shape = RoundedCornerShape(15.dp),
                color = AccentOrange.copy(alpha = 0.15f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale),
                        tint = AccentOrangeDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title with wave icon
        val titleAlpha by animateFloatAsState(
            targetValue = if (showTitle) 1f else 0f,
            animationSpec = tween(400),
            label = "titleAlpha"
        )

        Text(
            text = stringResource(R.string.exit_app),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = titleAlpha),
            modifier = Modifier.scale(if (showTitle) 1f else 0.95f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.exit_confirm_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = titleAlpha * 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        val buttonAlpha by animateFloatAsState(
            targetValue = if (showButtons) 1f else 0f,
            animationSpec = tween(400),
            label = "buttonAlpha"
        )
        val buttonScale by animateFloatAsState(
            targetValue = if (showButtons) 1f else 0.95f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "buttonScale"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(buttonScale),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stay button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.stay),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Exit button
            Button(
                onClick = onConfirmExit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.exit),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
