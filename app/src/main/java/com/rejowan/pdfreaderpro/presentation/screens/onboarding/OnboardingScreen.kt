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
import androidx.compose.material.icons.rounded.Star
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
    val shapeCornerPercent: Int // For morphing effect
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Rounded.PictureAsPdf,
        title = "Welcome to\nPDF Reader Pro",
        description = "The fastest and most powerful PDF reader for Android. Read, organize, and manage all your PDF files.",
        accentColor = Color(0xFF6366F1), // Indigo
        shapeCornerPercent = 50 // Circle
    ),
    OnboardingPage(
        icon = Icons.Rounded.Folder,
        title = "All Your PDFs\nin One Place",
        description = "Automatically scan and organize PDF files from your device. Browse by folders or view everything at once.",
        accentColor = Color(0xFF8B5CF6), // Purple
        shapeCornerPercent = 35 // Rounded square
    ),
    OnboardingPage(
        icon = Icons.Rounded.Star,
        title = "Quick Access\nto Favorites",
        description = "Mark important documents as favorites for instant access. Track your reading progress seamlessly.",
        accentColor = Color(0xFFEC4899), // Pink
        shapeCornerPercent = 25 // More square
    ),
    OnboardingPage(
        icon = Icons.Rounded.Security,
        title = "Storage Access\nRequired",
        description = "Grant access to scan and display your PDF files. Your documents stay private and are never uploaded.",
        accentColor = Color(0xFF10B981), // Emerald
        shapeCornerPercent = 20 // Nearly square
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background morphing shapes (subtle)
            BackgroundMorphingShapes(
                cornerPercent = currentCornerPercent,
                accentColor = currentAccentColor
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
    accentColor: Color
) {
    // Top-right shape
    Box(
        modifier = Modifier
            .size(180.dp)
            .offset(x = 220.dp, y = 80.dp)
            .clip(RoundedCornerShape(cornerPercent.toInt()))
            .background(accentColor.copy(alpha = 0.08f))
    )

    // Bottom-left shape
    Box(
        modifier = Modifier
            .size(140.dp)
            .offset(x = (-40).dp, y = 500.dp)
            .clip(RoundedCornerShape((60 - cornerPercent / 2).toInt().coerceIn(10, 50)))
            .background(accentColor.copy(alpha = 0.06f))
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
        // Pulsing morphing icon container
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse ring (morphing)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(outerPulseScale)
                    .clip(RoundedCornerShape(cornerPercent.toInt()))
                    .background(accentColor.copy(alpha = outerPulseAlpha))
            )

            // Middle pulse ring (morphing)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(cornerPercent.toInt()))
                    .background(accentColor.copy(alpha = pulseAlpha))
            )

            // Inner container with icon (morphing shape)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(cornerPercent.toInt()))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(1f + (pulseScale - 1f) * 0.2f), // Subtle icon pulse
                    tint = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

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
