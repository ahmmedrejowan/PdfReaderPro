package com.rejowan.pdfreaderpro.presentation.screens.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.Home
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color,
    // Shape morphing parameters
    val topStartCorner: Int,
    val topEndCorner: Int,
    val bottomStartCorner: Int,
    val bottomEndCorner: Int,
    // Icon container size and position
    val iconContainerScale: Float = 1f,
    val iconOffsetX: Float = 0f,
    val iconOffsetY: Float = 0f,
    // Background shape positions (for slide effects)
    val bgShape1OffsetX: Float = 200f,
    val bgShape1OffsetY: Float = 60f,
    val bgShape2OffsetX: Float = -50f,
    val bgShape2OffsetY: Float = 480f,
    val bgShape3OffsetX: Float = -20f,
    val bgShape3OffsetY: Float = 250f,
    // Background shape sizes
    val bgShape1Size: Float = 200f,
    val bgShape2Size: Float = 160f,
    val bgShape3Size: Float = 120f
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Rounded.PictureAsPdf,
        title = "Welcome to\nPDF Reader Pro",
        description = "A clean, lightweight PDF reader built for simplicity. Open source, ad-free, and respects your privacy.",
        accentColor = Color(0xFF6366F1), // Indigo
        // Circle shape
        topStartCorner = 50,
        topEndCorner = 50,
        bottomStartCorner = 50,
        bottomEndCorner = 50,
        iconContainerScale = 1f,
        // Background shapes - spread out
        bgShape1OffsetX = 220f,
        bgShape1OffsetY = 80f,
        bgShape2OffsetX = -60f,
        bgShape2OffsetY = 520f,
        bgShape3OffsetX = 280f,
        bgShape3OffsetY = 380f,
        bgShape1Size = 180f,
        bgShape2Size = 140f,
        bgShape3Size = 100f
    ),
    OnboardingPage(
        icon = Icons.Rounded.Folder,
        title = "All Your PDFs\nin One Place",
        description = "Automatically discovers PDFs across your device. Browse by folders, sort by name, date, or size, and find any document instantly with search.",
        accentColor = Color(0xFF8B5CF6), // Purple
        // Rounded rectangle - slightly asymmetric
        topStartCorner = 40,
        topEndCorner = 20,
        bottomStartCorner = 20,
        bottomEndCorner = 40,
        iconContainerScale = 1.05f,
        iconOffsetY = -5f,
        // Background shapes - slide to different positions
        bgShape1OffsetX = 180f,
        bgShape1OffsetY = 120f,
        bgShape2OffsetX = -80f,
        bgShape2OffsetY = 420f,
        bgShape3OffsetX = -40f,
        bgShape3OffsetY = 200f,
        bgShape1Size = 220f,
        bgShape2Size = 180f,
        bgShape3Size = 90f
    ),
    OnboardingPage(
        icon = Icons.Rounded.Tune,
        title = "Powerful Reader\n& PDF Tools",
        description = "Search text, customize themes, and adjust display settings. Plus built-in tools to merge, split, compress, and organize your PDFs.",
        accentColor = Color(0xFFEC4899), // Pink
        // Diamond-like shape - opposing corners
        topStartCorner = 15,
        topEndCorner = 45,
        bottomStartCorner = 45,
        bottomEndCorner = 15,
        iconContainerScale = 0.95f,
        iconOffsetX = 3f,
        // Background shapes - more dramatic slide
        bgShape1OffsetX = 250f,
        bgShape1OffsetY = 40f,
        bgShape2OffsetX = -100f,
        bgShape2OffsetY = 350f,
        bgShape3OffsetX = 300f,
        bgShape3OffsetY = 280f,
        bgShape1Size = 160f,
        bgShape2Size = 200f,
        bgShape3Size = 130f
    ),
    OnboardingPage(
        icon = Icons.Rounded.Security,
        title = "Storage Access\nRequired",
        description = "Grant access to scan and display your PDF files. Your documents stay private and are never uploaded.",
        accentColor = Color(0xFF10B981), // Emerald
        // Squircle-like - medium uniform corners
        topStartCorner = 30,
        topEndCorner = 30,
        bottomStartCorner = 30,
        bottomEndCorner = 30,
        iconContainerScale = 1.02f,
        iconOffsetY = 2f,
        // Background shapes - settle into final positions
        bgShape1OffsetX = 200f,
        bgShape1OffsetY = 100f,
        bgShape2OffsetX = -70f,
        bgShape2OffsetY = 480f,
        bgShape3OffsetX = -30f,
        bgShape3OffsetY = 320f,
        bgShape1Size = 190f,
        bgShape2Size = 150f,
        bgShape3Size = 110f
    )
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    onOnboardingComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { onboardingPages.size }
    )

    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    val isLastPage = currentPage == onboardingPages.size - 1

    var waitingForManagePermission by remember { mutableStateOf(false) }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onOnboardingComplete()
        navController.navigate(Home) {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (waitingForManagePermission) {
                waitingForManagePermission = false
                onOnboardingComplete()
                navController.navigate(Home) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Animated values for current page
    val currentAccentColor by animateColorAsState(
        targetValue = onboardingPages[currentPage].accentColor,
        animationSpec = tween(500),
        label = "accent"
    )

    val currentCornerPercent by animateFloatAsState(
        targetValue = onboardingPages[currentPage].shapeCornerPercent.toFloat(),
        animationSpec = tween(500),
        label = "corner"
    )

    // Infinite pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val outerPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerPulseScale"
    )

    val outerPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerPulseAlpha"
    )

    // Background shape movement animations
    val bgRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgRotation"
    )

    val bgFloat by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgFloat"
    )

    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background morphing shapes (subtle) with movement
            BackgroundMorphingShapes(
                cornerPercent = currentCornerPercent,
                accentColor = currentAccentColor,
                rotation = bgRotation,
                floatOffset = bgFloat,
                scale = bgScale
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top bar with Skip button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    if (!isLastPage) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(onboardingPages.size - 1)
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                "Skip",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                    OnboardingPageContent(
                        page = onboardingPages[page],
                        pageOffset = pageOffset,
                        cornerPercent = currentCornerPercent,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha,
                        outerPulseScale = outerPulseScale,
                        outerPulseAlpha = outerPulseAlpha,
                        accentColor = currentAccentColor
                    )
                }

                // Bottom section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Morphing page indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        repeat(onboardingPages.size) { index ->
                            MorphingPageIndicator(
                                isSelected = index == currentPage,
                                accentColor = currentAccentColor,
                                pulseScale = if (index == currentPage) pulseScale else 1f
                            )
                        }
                    }

                    // Action button with morphing corners
                    Button(
                        onClick = {
                            if (isLastPage) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    if (Environment.isExternalStorageManager()) {
                                        onOnboardingComplete()
                                        navController.navigate(Home) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    } else {
                                        waitingForManagePermission = true
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    }
                                } else {
                                    legacyPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    )
                                }
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(currentCornerPercent.toInt().coerceIn(16, 28)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentAccentColor
                        )
                    ) {
                        Text(
                            text = if (isLastPage) "Grant Access" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundMorphingShapes(
    cornerPercent: Float,
    accentColor: Color,
    rotation: Float,
    floatOffset: Float,
    scale: Float
) {
    // Top-right shape - rotates slowly and floats
    Box(
        modifier = Modifier
            .size(200.dp)
            .offset(x = 200.dp, y = (60 + floatOffset).dp)
            .scale(scale)
            .rotate(rotation * 0.5f)
            .clip(RoundedCornerShape(cornerPercent.toInt()))
            .background(accentColor.copy(alpha = 0.1f))
    )

    // Bottom-left shape - rotates opposite direction and floats inverse
    Box(
        modifier = Modifier
            .size(160.dp)
            .offset(x = (-50).dp, y = (480 - floatOffset).dp)
            .scale(1.1f - (scale - 1f))
            .rotate(-rotation * 0.3f)
            .clip(RoundedCornerShape((60 - cornerPercent / 2).toInt().coerceIn(10, 50)))
            .background(accentColor.copy(alpha = 0.08f))
    )

    // Third shape - center-left, more subtle
    Box(
        modifier = Modifier
            .size(120.dp)
            .offset(x = (-20).dp, y = (250 + floatOffset * 0.5f).dp)
            .scale(scale * 0.95f)
            .rotate(rotation * 0.2f)
            .clip(RoundedCornerShape((cornerPercent * 0.8f).toInt()))
            .background(accentColor.copy(alpha = 0.05f))
    )
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float,
    cornerPercent: Float,
    pulseScale: Float,
    pulseAlpha: Float,
    outerPulseScale: Float,
    outerPulseAlpha: Float,
    accentColor: Color
) {
    val contentAlpha by animateFloatAsState(
        targetValue = 1f - (pageOffset.absoluteValue * 0.5f).coerceIn(0f, 0.5f),
        animationSpec = spring(),
        label = "alpha"
    )

    val contentScale by animateFloatAsState(
        targetValue = 1f - (pageOffset.absoluteValue * 0.1f).coerceIn(0f, 0.1f),
        animationSpec = spring(),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .graphicsLayer {
                alpha = contentAlpha
                scaleX = contentScale
                scaleY = contentScale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing morphing icon container - BIGGER circles
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse ring (morphing) - bigger
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(outerPulseScale)
                    .clip(RoundedCornerShape(cornerPercent.toInt()))
                    .background(accentColor.copy(alpha = outerPulseAlpha))
            )

            // Middle pulse ring (morphing) - bigger
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(cornerPercent.toInt()))
                    .background(accentColor.copy(alpha = pulseAlpha))
            )

            // Inner container with icon (morphing shape) - bigger and more visible
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(cornerPercent.toInt()))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .scale(1f + (pulseScale - 1f) * 0.25f), // More noticeable icon pulse
                    tint = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun MorphingPageIndicator(
    isSelected: Boolean,
    accentColor: Color,
    pulseScale: Float
) {
    // Morphing width (dot → pill)
    val width by animateDpAsState(
        targetValue = if (isSelected) 28.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "width"
    )

    // Morphing corner radius
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 4.dp,
        animationSpec = tween(300),
        label = "corner"
    )

    val color by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "color"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(8.dp)
            .width(width)
            .scale(if (isSelected) 1f + (pulseScale - 1f) * 0.15f else 1f) // Subtle pulse on selected
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
    )
}
