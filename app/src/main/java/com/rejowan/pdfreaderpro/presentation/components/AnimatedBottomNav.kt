package com.rejowan.pdfreaderpro.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

enum class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    FOLDERS("Folders", Icons.Filled.Folder, Icons.Outlined.Folder),
    TOOLS("Tools", Icons.Outlined.Build, Icons.Outlined.Build),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AnimatedBottomNav(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val circleRadius = 20.dp
    val items = NavItem.entries

    val selectedTextColor = MaterialTheme.colorScheme.onSurface
    val selectedIconColor = MaterialTheme.colorScheme.onPrimary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    var currentSelected by rememberSaveable { mutableIntStateOf(selectedIndex) }

    // Sync with external state
    if (selectedIndex != currentSelected) {
        currentSelected = selectedIndex
    }

    var barSize by remember { mutableStateOf(IntSize(0, 0)) }

    val offsetStep = remember(barSize) {
        barSize.width.toFloat() / (items.size * 2)
    }
    val offset = remember(currentSelected, offsetStep) {
        offsetStep + currentSelected * 2 * offsetStep
    }

    val circleRadiusPx = LocalDensity.current.run { circleRadius.toPx().toInt() }
    val offsetTransition = updateTransition(offset, "offset transition")
    val animation = spring<Float>(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow)

    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = {
            if (initialState == 0f) snap() else animation
        },
        label = "cutout offset",
        targetValueByState = { state -> state }
    )

    val circleOffset by offsetTransition.animateIntOffset(
        transitionSpec = {
            if (initialState == 0f) snap() else spring(animation.dampingRatio, animation.stiffness)
        },
        label = "circle offset",
        targetValueByState = { state ->
            IntOffset(state.toInt() - circleRadiusPx, -circleRadiusPx)
        }
    )

    val barShape = remember(cutoutOffset) {
        BarShape(
            offset = cutoutOffset,
            circleRadius = circleRadius,
            cornerRadius = 0.dp,
        )
    }

    Column(modifier = modifier) {
        Box {
            // Floating Circle
            FloatingCircle(
                modifier = Modifier
                    .offset { circleOffset }
                    .zIndex(1f),
                color = MaterialTheme.colorScheme.primary,
                radius = circleRadius,
                item = items[currentSelected],
                iconColor = selectedIconColor,
            )

            // Navigation Bar with cutout shape
            Row(
                modifier = Modifier
                    .onPlaced { barSize = it.size }
                    .graphicsLayer {
                        shape = barShape
                        clip = true
                    }
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == currentSelected
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentSelected = index
                            onItemClick(index)
                        },
                        icon = {
                            val iconAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 0f else 1f,
                                label = "Navbar item icon"
                            )
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(18.dp)
                                    .alpha(iconAlpha)
                            )
                        },
                        label = {
                            Text(text = item.label)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedIconColor,
                            selectedTextColor = selectedTextColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = Color.Transparent,
                        )
                    )
                }
            }
        }

        // Navigation bar insets spacer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
    }
}

@Composable
private fun FloatingCircle(
    modifier: Modifier = Modifier,
    color: Color,
    radius: Dp,
    item: NavItem,
    iconColor: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .clip(CircleShape)
            .background(color),
    ) {
        AnimatedContent(
            targetState = item.selectedIcon,
            label = "Bottom bar circle icon",
        ) { targetIcon ->
            Icon(
                imageVector = targetIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private class BarShape(
    private val offset: Float,
    private val circleRadius: Dp,
    private val cornerRadius: Dp,
    private val circleGap: Dp = 5.dp,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(getPath(size, density))
    }

    private fun getPath(size: Size, density: Density): Path {
        val cutoutCenterX = offset
        val cutoutRadius = density.run { (circleRadius + circleGap).toPx() }
        val cornerRadiusPx = density.run { cornerRadius.toPx() }
        val cornerDiameter = cornerRadiusPx * 2

        return Path().apply {
            val cutoutEdgeOffset = cutoutRadius * 1.5f
            val cutoutLeftX = cutoutCenterX - cutoutEdgeOffset
            val cutoutRightX = cutoutCenterX + cutoutEdgeOffset

            moveTo(x = 0F, y = size.height)

            if (cornerRadiusPx > 0 && cutoutLeftX > 0) {
                val realLeftCornerDiameter = if (cutoutLeftX >= cornerRadiusPx) {
                    cornerDiameter
                } else {
                    cutoutLeftX * 2
                }
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = 0f,
                        right = realLeftCornerDiameter,
                        bottom = realLeftCornerDiameter
                    ),
                    startAngleDegrees = 180.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            } else {
                lineTo(x = 0f, y = 0f)
            }

            lineTo(cutoutLeftX.coerceAtLeast(0f), 0f)

            cubicTo(
                x1 = cutoutCenterX - cutoutRadius,
                y1 = 0f,
                x2 = cutoutCenterX - cutoutRadius,
                y2 = cutoutRadius,
                x3 = cutoutCenterX,
                y3 = cutoutRadius,
            )
            cubicTo(
                x1 = cutoutCenterX + cutoutRadius,
                y1 = cutoutRadius,
                x2 = cutoutCenterX + cutoutRadius,
                y2 = 0f,
                x3 = cutoutRightX.coerceAtMost(size.width),
                y3 = 0f,
            )

            if (cornerRadiusPx > 0 && cutoutRightX < size.width) {
                val realRightCornerDiameter = if (cutoutRightX <= size.width - cornerRadiusPx) {
                    cornerDiameter
                } else {
                    (size.width - cutoutRightX) * 2
                }
                arcTo(
                    rect = Rect(
                        left = size.width - realRightCornerDiameter,
                        top = 0f,
                        right = size.width,
                        bottom = realRightCornerDiameter
                    ),
                    startAngleDegrees = -90.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            } else {
                lineTo(x = size.width, y = 0f)
            }

            lineTo(x = size.width, y = size.height)
            close()
        }
    }
}
