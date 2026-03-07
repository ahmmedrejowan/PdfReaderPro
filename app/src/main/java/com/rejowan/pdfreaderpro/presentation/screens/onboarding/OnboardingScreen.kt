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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.rejowan.pdfreaderpro.R
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.Home
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Individual shape configuration for varied morphing
data class ShapeConfig(
    val offsetX: Float,
    val offsetY: Float,
    val size: Float,
    val rotation: Float = 0f,
    val alpha: Float = 0.1f,
    // Asymmetric corners for varied shapes (0-50 range, percentage-like)
    val topStartCorner: Int = 50,
    val topEndCorner: Int = 50,
    val bottomStartCorner: Int = 50,
    val bottomEndCorner: Int = 50
)

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color,
    // Icon container shape morphing parameters
    val topStartCorner: Int,
    val topEndCorner: Int,
    val bottomStartCorner: Int,
    val bottomEndCorner: Int,
    // Icon container size and position
    val iconContainerScale: Float = 1f,
    val iconOffsetX: Float = 0f,
    val iconOffsetY: Float = 0f,
    // Background shapes with individual configurations
    val bgShape1: ShapeConfig,
    val bgShape2: ShapeConfig,
    val bgShape3: ShapeConfig,
    val bgShape4: ShapeConfig? = null // Optional 4th shape for some pages
)

@Composable
private fun getOnboardingPages(): List<OnboardingPage> = listOf(
    OnboardingPage(
        icon = Icons.Rounded.PictureAsPdf,
        title = stringResource(R.string.onboarding_welcome),
        description = stringResource(R.string.onboarding_welcome_desc),
        accentColor = Color(0xFF6366F1), // Indigo
        // Circle shape for icon
        topStartCorner = 50,
        topEndCorner = 50,
        bottomStartCorner = 50,
        bottomEndCorner = 50,
        iconContainerScale = 1f,
        // Background shapes - varied shapes, spread out (subtle)
        bgShape1 = ShapeConfig(
            offsetX = 260f, offsetY = 40f, size = 140f,
            rotation = 0f, alpha = 0.06f,
            topStartCorner = 50, topEndCorner = 50, // Circle
            bottomStartCorner = 50, bottomEndCorner = 50
        ),
        bgShape2 = ShapeConfig(
            offsetX = -80f, offsetY = 480f, size = 180f,
            rotation = 15f, alpha = 0.04f,
            topStartCorner = 30, topEndCorner = 50, // Teardrop-ish
            bottomStartCorner = 50, bottomEndCorner = 30
        ),
        bgShape3 = ShapeConfig(
            offsetX = 300f, offsetY = 350f, size = 100f,
            rotation = -10f, alpha = 0.03f,
            topStartCorner = 20, topEndCorner = 40, // Blob-like
            bottomStartCorner = 35, bottomEndCorner = 25
        ),
        bgShape4 = ShapeConfig(
            offsetX = -40f, offsetY = 180f, size = 80f,
            rotation = 25f, alpha = 0.025f,
            topStartCorner = 45, topEndCorner = 15, // Leaf-like
            bottomStartCorner = 15, bottomEndCorner = 45
        )
    ),
    OnboardingPage(
        icon = Icons.Rounded.Folder,
        title = stringResource(R.string.onboarding_all_pdfs),
        description = stringResource(R.string.onboarding_all_pdfs_desc),
        accentColor = Color(0xFF8B5CF6), // Purple
        // Asymmetric rounded for icon
        topStartCorner = 40,
        topEndCorner = 20,
        bottomStartCorner = 20,
        bottomEndCorner = 40,
        iconContainerScale = 1.05f,
        iconOffsetY = -5f,
        // Shapes slide to new positions with different morphs (subtle)
        bgShape1 = ShapeConfig(
            offsetX = 180f, offsetY = 100f, size = 200f,
            rotation = 45f, alpha = 0.05f,
            topStartCorner = 25, topEndCorner = 45, // Diamond-ish
            bottomStartCorner = 45, bottomEndCorner = 25
        ),
        bgShape2 = ShapeConfig(
            offsetX = -100f, offsetY = 380f, size = 160f,
            rotation = -20f, alpha = 0.045f,
            topStartCorner = 50, topEndCorner = 20, // Egg shape
            bottomStartCorner = 35, bottomEndCorner = 50
        ),
        bgShape3 = ShapeConfig(
            offsetX = 280f, offsetY = 500f, size = 120f,
            rotation = 30f, alpha = 0.035f,
            topStartCorner = 15, topEndCorner = 15, // Squarish with round bottom
            bottomStartCorner = 40, bottomEndCorner = 40
        ),
        bgShape4 = ShapeConfig(
            offsetX = -60f, offsetY = 120f, size = 90f,
            rotation = -45f, alpha = 0.03f,
            topStartCorner = 40, topEndCorner = 40, // Rounded top, pointed bottom
            bottomStartCorner = 10, bottomEndCorner = 10
        )
    ),
    OnboardingPage(
        icon = Icons.Rounded.Tune,
        title = stringResource(R.string.onboarding_powerful),
        description = stringResource(R.string.onboarding_powerful_desc),
        accentColor = Color(0xFFEC4899), // Pink
        // Diamond-like shape for icon
        topStartCorner = 15,
        topEndCorner = 45,
        bottomStartCorner = 45,
        bottomEndCorner = 15,
        iconContainerScale = 0.95f,
        iconOffsetX = 3f,
        // More dramatic positions and shapes (subtle)
        bgShape1 = ShapeConfig(
            offsetX = 320f, offsetY = 60f, size = 170f,
            rotation = -30f, alpha = 0.055f,
            topStartCorner = 10, topEndCorner = 50, // Comma shape
            bottomStartCorner = 50, bottomEndCorner = 10
        ),
        bgShape2 = ShapeConfig(
            offsetX = -120f, offsetY = 320f, size = 220f,
            rotation = 60f, alpha = 0.04f,
            topStartCorner = 35, topEndCorner = 20, // Organic blob
            bottomStartCorner = 45, bottomEndCorner = 30
        ),
        bgShape3 = ShapeConfig(
            offsetX = 200f, offsetY = 450f, size = 90f,
            rotation = -60f, alpha = 0.03f,
            topStartCorner = 50, topEndCorner = 50, // Small circle
            bottomStartCorner = 50, bottomEndCorner = 50
        ),
        bgShape4 = ShapeConfig(
            offsetX = 350f, offsetY = 280f, size = 70f,
            rotation = 90f, alpha = 0.025f,
            topStartCorner = 30, topEndCorner = 30, // Squircle
            bottomStartCorner = 30, bottomEndCorner = 30
        )
    ),
    OnboardingPage(
        icon = Icons.Rounded.Security,
        title = stringResource(R.string.onboarding_storage),
        description = stringResource(R.string.onboarding_storage_desc),
        accentColor = Color(0xFF10B981), // Emerald
        // Squircle for icon
        topStartCorner = 30,
        topEndCorner = 30,
        bottomStartCorner = 30,
        bottomEndCorner = 30,
        iconContainerScale = 1.02f,
        iconOffsetY = 2f,
        // Final settled positions (subtle)
        bgShape1 = ShapeConfig(
            offsetX = 240f, offsetY = 80f, size = 160f,
            rotation = 15f, alpha = 0.05f,
            topStartCorner = 35, topEndCorner = 35, // Soft squircle
            bottomStartCorner = 35, bottomEndCorner = 35
        ),
        bgShape2 = ShapeConfig(
            offsetX = -90f, offsetY = 450f, size = 180f,
            rotation = -15f, alpha = 0.04f,
            topStartCorner = 40, topEndCorner = 25, // Slightly organic
            bottomStartCorner = 30, bottomEndCorner = 45
        ),
        bgShape3 = ShapeConfig(
            offsetX = 280f, offsetY = 380f, size = 110f,
            rotation = 0f, alpha = 0.03f,
            topStartCorner = 50, topEndCorner = 30, // Pebble shape
            bottomStartCorner = 40, bottomEndCorner = 50
        ),
        bgShape4 = ShapeConfig(
            offsetX = -20f, offsetY = 220f, size = 85f,
            rotation = 30f, alpha = 0.025f,
            topStartCorner = 25, topEndCorner = 50, // Asymmetric blob
            bottomStartCorner = 50, bottomEndCorner = 20
        )
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
    val onboardingPages = getOnboardingPages()

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

    // Animated corner values for asymmetric shapes
    val topStartCorner by animateFloatAsState(
        targetValue = onboardingPages[currentPage].topStartCorner.toFloat(),
        animationSpec = tween(600),
        label = "topStart"
    )
    val topEndCorner by animateFloatAsState(
        targetValue = onboardingPages[currentPage].topEndCorner.toFloat(),
        animationSpec = tween(600),
        label = "topEnd"
    )
    val bottomStartCorner by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bottomStartCorner.toFloat(),
        animationSpec = tween(600),
        label = "bottomStart"
    )
    val bottomEndCorner by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bottomEndCorner.toFloat(),
        animationSpec = tween(600),
        label = "bottomEnd"
    )

    // Animated icon container properties
    val iconContainerScale by animateFloatAsState(
        targetValue = onboardingPages[currentPage].iconContainerScale,
        animationSpec = tween(500),
        label = "iconScale"
    )
    val iconOffsetX by animateFloatAsState(
        targetValue = onboardingPages[currentPage].iconOffsetX,
        animationSpec = tween(500),
        label = "iconOffsetX"
    )
    val iconOffsetY by animateFloatAsState(
        targetValue = onboardingPages[currentPage].iconOffsetY,
        animationSpec = tween(500),
        label = "iconOffsetY"
    )

    // Shape 1 animations
    val bg1X by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.offsetX,
        animationSpec = tween(700), label = "bg1X"
    )
    val bg1Y by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.offsetY,
        animationSpec = tween(750), label = "bg1Y"
    )
    val bg1Size by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.size,
        animationSpec = tween(600), label = "bg1Size"
    )
    val bg1Rotation by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.rotation,
        animationSpec = tween(800), label = "bg1Rot"
    )
    val bg1Alpha by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.alpha,
        animationSpec = tween(500), label = "bg1Alpha"
    )
    val bg1TS by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.topStartCorner.toFloat(),
        animationSpec = tween(650), label = "bg1TS"
    )
    val bg1TE by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.topEndCorner.toFloat(),
        animationSpec = tween(650), label = "bg1TE"
    )
    val bg1BS by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.bottomStartCorner.toFloat(),
        animationSpec = tween(650), label = "bg1BS"
    )
    val bg1BE by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape1.bottomEndCorner.toFloat(),
        animationSpec = tween(650), label = "bg1BE"
    )

    // Shape 2 animations
    val bg2X by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.offsetX,
        animationSpec = tween(800), label = "bg2X"
    )
    val bg2Y by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.offsetY,
        animationSpec = tween(850), label = "bg2Y"
    )
    val bg2Size by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.size,
        animationSpec = tween(700), label = "bg2Size"
    )
    val bg2Rotation by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.rotation,
        animationSpec = tween(900), label = "bg2Rot"
    )
    val bg2Alpha by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.alpha,
        animationSpec = tween(550), label = "bg2Alpha"
    )
    val bg2TS by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.topStartCorner.toFloat(),
        animationSpec = tween(700), label = "bg2TS"
    )
    val bg2TE by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.topEndCorner.toFloat(),
        animationSpec = tween(700), label = "bg2TE"
    )
    val bg2BS by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.bottomStartCorner.toFloat(),
        animationSpec = tween(700), label = "bg2BS"
    )
    val bg2BE by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape2.bottomEndCorner.toFloat(),
        animationSpec = tween(700), label = "bg2BE"
    )

    // Shape 3 animations
    val bg3X by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.offsetX,
        animationSpec = tween(650), label = "bg3X"
    )
    val bg3Y by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.offsetY,
        animationSpec = tween(700), label = "bg3Y"
    )
    val bg3Size by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.size,
        animationSpec = tween(550), label = "bg3Size"
    )
    val bg3Rotation by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.rotation,
        animationSpec = tween(750), label = "bg3Rot"
    )
    val bg3Alpha by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.alpha,
        animationSpec = tween(450), label = "bg3Alpha"
    )
    val bg3TS by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.topStartCorner.toFloat(),
        animationSpec = tween(600), label = "bg3TS"
    )
    val bg3TE by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.topEndCorner.toFloat(),
        animationSpec = tween(600), label = "bg3TE"
    )
    val bg3BS by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.bottomStartCorner.toFloat(),
        animationSpec = tween(600), label = "bg3BS"
    )
    val bg3BE by animateFloatAsState(
        targetValue = onboardingPages[currentPage].bgShape3.bottomEndCorner.toFloat(),
        animationSpec = tween(600), label = "bg3BE"
    )

    // Shape 4 animations (optional shape)
    val bg4Config = onboardingPages[currentPage].bgShape4
    val bg4X by animateFloatAsState(
        targetValue = bg4Config?.offsetX ?: -200f,
        animationSpec = tween(720), label = "bg4X"
    )
    val bg4Y by animateFloatAsState(
        targetValue = bg4Config?.offsetY ?: 300f,
        animationSpec = tween(780), label = "bg4Y"
    )
    val bg4Size by animateFloatAsState(
        targetValue = bg4Config?.size ?: 0f,
        animationSpec = tween(620), label = "bg4Size"
    )
    val bg4Rotation by animateFloatAsState(
        targetValue = bg4Config?.rotation ?: 0f,
        animationSpec = tween(850), label = "bg4Rot"
    )
    val bg4Alpha by animateFloatAsState(
        targetValue = bg4Config?.alpha ?: 0f,
        animationSpec = tween(500), label = "bg4Alpha"
    )
    val bg4TS by animateFloatAsState(
        targetValue = (bg4Config?.topStartCorner ?: 50).toFloat(),
        animationSpec = tween(680), label = "bg4TS"
    )
    val bg4TE by animateFloatAsState(
        targetValue = (bg4Config?.topEndCorner ?: 50).toFloat(),
        animationSpec = tween(680), label = "bg4TE"
    )
    val bg4BS by animateFloatAsState(
        targetValue = (bg4Config?.bottomStartCorner ?: 50).toFloat(),
        animationSpec = tween(680), label = "bg4BS"
    )
    val bg4BE by animateFloatAsState(
        targetValue = (bg4Config?.bottomEndCorner ?: 50).toFloat(),
        animationSpec = tween(680), label = "bg4BE"
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
            // Background morphing shapes with varied animations
            BackgroundMorphingShapes(
                accentColor = currentAccentColor,
                continuousRotation = bgRotation,
                floatOffset = bgFloat,
                breatheScale = bgScale,
                // Shape 1
                shape1X = bg1X, shape1Y = bg1Y, shape1Size = bg1Size,
                shape1Rotation = bg1Rotation, shape1Alpha = bg1Alpha,
                shape1TS = bg1TS, shape1TE = bg1TE, shape1BS = bg1BS, shape1BE = bg1BE,
                // Shape 2
                shape2X = bg2X, shape2Y = bg2Y, shape2Size = bg2Size,
                shape2Rotation = bg2Rotation, shape2Alpha = bg2Alpha,
                shape2TS = bg2TS, shape2TE = bg2TE, shape2BS = bg2BS, shape2BE = bg2BE,
                // Shape 3
                shape3X = bg3X, shape3Y = bg3Y, shape3Size = bg3Size,
                shape3Rotation = bg3Rotation, shape3Alpha = bg3Alpha,
                shape3TS = bg3TS, shape3TE = bg3TE, shape3BS = bg3BS, shape3BE = bg3BE,
                // Shape 4
                shape4X = bg4X, shape4Y = bg4Y, shape4Size = bg4Size,
                shape4Rotation = bg4Rotation, shape4Alpha = bg4Alpha,
                shape4TS = bg4TS, shape4TE = bg4TE, shape4BS = bg4BS, shape4BE = bg4BE
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
                        topStartCorner = topStartCorner,
                        topEndCorner = topEndCorner,
                        bottomStartCorner = bottomStartCorner,
                        bottomEndCorner = bottomEndCorner,
                        iconContainerScale = iconContainerScale,
                        iconOffsetX = iconOffsetX,
                        iconOffsetY = iconOffsetY,
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
                        shape = RoundedCornerShape(
                            topStart = topStartCorner.toInt().coerceIn(14, 28).dp,
                            topEnd = topEndCorner.toInt().coerceIn(14, 28).dp,
                            bottomStart = bottomStartCorner.toInt().coerceIn(14, 28).dp,
                            bottomEnd = bottomEndCorner.toInt().coerceIn(14, 28).dp
                        ),
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
                                contentDescription = stringResource(R.string.cd_decorative),
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
    accentColor: Color,
    continuousRotation: Float,
    floatOffset: Float,
    breatheScale: Float,
    // Shape 1
    shape1X: Float, shape1Y: Float, shape1Size: Float,
    shape1Rotation: Float, shape1Alpha: Float,
    shape1TS: Float, shape1TE: Float, shape1BS: Float, shape1BE: Float,
    // Shape 2
    shape2X: Float, shape2Y: Float, shape2Size: Float,
    shape2Rotation: Float, shape2Alpha: Float,
    shape2TS: Float, shape2TE: Float, shape2BS: Float, shape2BE: Float,
    // Shape 3
    shape3X: Float, shape3Y: Float, shape3Size: Float,
    shape3Rotation: Float, shape3Alpha: Float,
    shape3TS: Float, shape3TE: Float, shape3BS: Float, shape3BE: Float,
    // Shape 4
    shape4X: Float, shape4Y: Float, shape4Size: Float,
    shape4Rotation: Float, shape4Alpha: Float,
    shape4TS: Float, shape4TE: Float, shape4BS: Float, shape4BE: Float
) {
    // Shape 1 - morphing with gentle breathing and subtle rotation
    Box(
        modifier = Modifier
            .size(shape1Size.dp)
            .offset(x = shape1X.dp, y = (shape1Y + floatOffset * 0.8f).dp)
            .scale(breatheScale * 0.98f)
            .rotate(shape1Rotation + continuousRotation * 0.02f)
            .clip(
                RoundedCornerShape(
                    topStartPercent = shape1TS.toInt(),
                    topEndPercent = shape1TE.toInt(),
                    bottomStartPercent = shape1BS.toInt(),
                    bottomEndPercent = shape1BE.toInt()
                )
            )
            .background(accentColor.copy(alpha = shape1Alpha))
    )

    // Shape 2 - larger, slower, drifts opposite direction
    Box(
        modifier = Modifier
            .size(shape2Size.dp)
            .offset(x = shape2X.dp, y = (shape2Y - floatOffset * 0.6f).dp)
            .scale(1.05f - (breatheScale - 1f) * 0.5f)
            .rotate(shape2Rotation - continuousRotation * 0.015f)
            .clip(
                RoundedCornerShape(
                    topStartPercent = shape2TS.toInt(),
                    topEndPercent = shape2TE.toInt(),
                    bottomStartPercent = shape2BS.toInt(),
                    bottomEndPercent = shape2BE.toInt()
                )
            )
            .background(accentColor.copy(alpha = shape2Alpha))
    )

    // Shape 3 - smaller, more playful movement
    Box(
        modifier = Modifier
            .size(shape3Size.dp)
            .offset(x = (shape3X + floatOffset * 0.3f).dp, y = shape3Y.dp)
            .scale(breatheScale * 0.95f)
            .rotate(shape3Rotation + continuousRotation * 0.025f)
            .clip(
                RoundedCornerShape(
                    topStartPercent = shape3TS.toInt(),
                    topEndPercent = shape3TE.toInt(),
                    bottomStartPercent = shape3BS.toInt(),
                    bottomEndPercent = shape3BE.toInt()
                )
            )
            .background(accentColor.copy(alpha = shape3Alpha))
    )

    // Shape 4 - optional accent shape, subtle diagonal drift
    if (shape4Alpha > 0.01f) {
        Box(
            modifier = Modifier
                .size(shape4Size.dp)
                .offset(
                    x = (shape4X + floatOffset * 0.4f).dp,
                    y = (shape4Y + floatOffset * 0.5f).dp
                )
                .scale(breatheScale)
                .rotate(shape4Rotation - continuousRotation * 0.03f)
                .clip(
                    RoundedCornerShape(
                        topStartPercent = shape4TS.toInt(),
                        topEndPercent = shape4TE.toInt(),
                        bottomStartPercent = shape4BS.toInt(),
                        bottomEndPercent = shape4BE.toInt()
                    )
                )
                .background(accentColor.copy(alpha = shape4Alpha))
        )
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float,
    topStartCorner: Float,
    topEndCorner: Float,
    bottomStartCorner: Float,
    bottomEndCorner: Float,
    iconContainerScale: Float,
    iconOffsetX: Float,
    iconOffsetY: Float,
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

    // Create asymmetric shape for icon container
    val iconShape = RoundedCornerShape(
        topStartPercent = topStartCorner.toInt(),
        topEndPercent = topEndCorner.toInt(),
        bottomStartPercent = bottomStartCorner.toInt(),
        bottomEndPercent = bottomEndCorner.toInt()
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
        // Pulsing morphing icon container with asymmetric shapes
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = iconOffsetX.dp, y = iconOffsetY.dp)
                .scale(iconContainerScale),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse ring (asymmetric morphing)
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(outerPulseScale)
                    .clip(iconShape)
                    .background(accentColor.copy(alpha = outerPulseAlpha))
            )

            // Middle pulse ring (asymmetric morphing)
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(pulseScale)
                    .clip(iconShape)
                    .background(accentColor.copy(alpha = pulseAlpha))
            )

            // Inner container with icon (asymmetric morphing shape)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(iconShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = stringResource(R.string.cd_illustration),
                    modifier = Modifier
                        .size(56.dp)
                        .scale(1f + (pulseScale - 1f) * 0.25f),
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
