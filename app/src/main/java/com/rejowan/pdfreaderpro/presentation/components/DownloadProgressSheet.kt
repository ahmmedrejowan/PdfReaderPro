package com.rejowan.pdfreaderpro.presentation.components

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.util.ApkDownloadManager

/**
 * A sheet that shows download progress for APK updates.
 * Can be dismissed but download continues in background with system notification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadProgressSheet(
    downloadState: ApkDownloadManager.DownloadState,
    versionName: String,
    canInstall: Boolean,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onInstall: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Side panel for landscape
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
                        .background(Color.Black.copy(alpha = 0.5f))
                        .pointerInput(Unit) { detectTapGestures { onDismiss() } }
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
                        .width(360.dp)
                        .fillMaxHeight()
                        .pointerInput(Unit) { detectTapGestures { /* consume */ } },
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = systemBarsPadding.calculateTopPadding(),
                                bottom = systemBarsPadding.calculateBottomPadding()
                            )
                    ) {
                        DownloadContent(
                            downloadState = downloadState,
                            versionName = versionName,
                            canInstall = canInstall,
                            onCancel = onCancel,
                            onInstall = onInstall,
                            onRequestPermission = onRequestPermission,
                            onDismiss = onDismiss
                        )

                        // Close button
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.close),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Bottom sheet for portrait
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DownloadContent(
                downloadState = downloadState,
                versionName = versionName,
                canInstall = canInstall,
                onCancel = onCancel,
                onInstall = onInstall,
                onRequestPermission = onRequestPermission,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun DownloadContent(
    downloadState: ApkDownloadManager.DownloadState,
    versionName: String,
    canInstall: Boolean,
    onCancel: () -> Unit,
    onInstall: () -> Unit,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (downloadState) {
            is ApkDownloadManager.DownloadState.Idle,
            is ApkDownloadManager.DownloadState.Starting -> {
                DownloadingSection(
                    progress = 0,
                    downloadedBytes = 0,
                    totalBytes = 0,
                    versionName = versionName,
                    isStarting = true,
                    onCancel = onCancel
                )
            }

            is ApkDownloadManager.DownloadState.Downloading -> {
                DownloadingSection(
                    progress = downloadState.progress,
                    downloadedBytes = downloadState.downloadedBytes,
                    totalBytes = downloadState.totalBytes,
                    versionName = versionName,
                    isStarting = false,
                    onCancel = onCancel
                )
            }

            is ApkDownloadManager.DownloadState.Completed -> {
                CompletedSection(
                    versionName = versionName,
                    canInstall = canInstall,
                    onInstall = onInstall,
                    onRequestPermission = onRequestPermission,
                    onDismiss = onDismiss
                )
            }

            is ApkDownloadManager.DownloadState.Failed -> {
                FailedSection(
                    reason = downloadState.reason,
                    onDismiss = onDismiss
                )
            }

            is ApkDownloadManager.DownloadState.Cancelled -> {
                CancelledSection(onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun DownloadingSection(
    progress: Int,
    downloadedBytes: Long,
    totalBytes: Long,
    versionName: String,
    isStarting: Boolean,
    onCancel: () -> Unit
) {
    // Icon
    StatusIcon(
        icon = Icons.Rounded.Download,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        iconColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Title
    Text(
        text = if (isStarting) stringResource(R.string.starting_download) else stringResource(R.string.downloading_update),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = stringResource(R.string.version_label, versionName),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Progress
    if (isStarting) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress / 100f,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progress"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress percentage
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Size info
            Text(
                text = "${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Info text
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = stringResource(R.string.download_background_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Cancel button
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Cancel,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.cancel_download))
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun CompletedSection(
    versionName: String,
    canInstall: Boolean,
    onInstall: () -> Unit,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Dynamically check permission, refresh on resume
    var canInstallApks by remember { mutableStateOf(canInstall) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canInstallApks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.packageManager.canRequestPackageInstalls()
                } else {
                    true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Success icon
    StatusIcon(
        icon = Icons.Rounded.CheckCircle,
        containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
        iconColor = Color(0xFF4CAF50)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.download_complete),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = stringResource(R.string.version_ready_to_install, versionName),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (canInstallApks) {
        // Install button
        Button(
            onClick = onInstall,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.InstallMobile,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.install_update))
        }
    } else {
        // Permission required
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.permission_required),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.allow_install_apps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.grant_permission))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(stringResource(R.string.later))
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun FailedSection(
    reason: String,
    onDismiss: () -> Unit
) {
    StatusIcon(
        icon = Icons.Rounded.Error,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        iconColor = MaterialTheme.colorScheme.onErrorContainer
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.download_failed),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    ) {
        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(stringResource(R.string.close))
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun CancelledSection(
    onDismiss: () -> Unit
) {
    StatusIcon(
        icon = Icons.Rounded.Cancel,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.download_cancelled),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = stringResource(R.string.try_again_settings),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(stringResource(R.string.close))
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun StatusIcon(
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.size(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = iconColor
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
