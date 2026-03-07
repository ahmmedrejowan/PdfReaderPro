package com.rejowan.pdfreaderpro.presentation.screens.tools.lock

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import androidx.compose.ui.res.stringResource
import com.rejowan.pdfreaderpro.R
import org.koin.androidx.compose.koinViewModel
import java.io.File

// Accent colors
private val AccentAmber = Color(0xFFFFB74D)  // Lock theme color
private val AccentGreen = Color(0xFF81C784)
private val AccentRed = Color(0xFFEF5350)
private val AccentBlue = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    navController: NavController,
    initialFilePath: String? = null,
    viewModel: LockViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.setSourceFile(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.tool_lock_pdf))
                        state.sourceFile?.let { file ->
                            Text(
                                stringResource(R.string.pages_count, file.pageCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            when {
                state.sourceFile == null && state.result == null -> {
                    EmptyState(
                        onSelectFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
                state.result != null -> {
                    val result = requireNotNull(state.result)
                    SuccessState(
                        result = result,
                        onOpenInApp = { navController.navigateToReader(result.outputPath) },
                        onShare = {
                            val file = File(result.outputPath)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                        },
                        onLockMore = { viewModel.reset() },
                        onDone = { navController.popBackStack() }
                    )
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentAmber)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.loading_pdf))
                        }
                    }
                }
                else -> {
                    LockContent(
                        state = state,
                        onUserPasswordChange = { viewModel.setUserPassword(it) },
                        onOwnerPasswordChange = { viewModel.setOwnerPassword(it) },
                        onAllowPrintingChange = { viewModel.setAllowPrinting(it) },
                        onAllowCopyingChange = { viewModel.setAllowCopying(it) },
                        onAllowModifyingChange = { viewModel.setAllowModifying(it) },
                        onAllowAnnotationsChange = { viewModel.setAllowAnnotations(it) },
                        onOutputFileNameChange = { viewModel.setOutputFileName(it) },
                        onOverwriteChange = { viewModel.setOverwriteOriginal(it) },
                        onLock = { viewModel.lock() },
                        onClearError = { viewModel.clearError() },
                        onChangeFile = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        onPreview = { state.sourceFile?.path?.let { navController.navigateToReader(it) } }
                    )
                }
            }

            // Processing overlay
            AnimatedVisibility(
                visible = state.isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ProcessingOverlay(progress = state.progress)
            }
        }
    }
}

@Composable
private fun EmptyState(onSelectFile: () -> Unit) {
    // Floating animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "empty state float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-floatOffset).dp)
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AccentAmber.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = stringResource(R.string.cd_decorative),
                modifier = Modifier.size(40.dp),
                tint = AccentAmber
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.tool_lock_pdf),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.tool_lock_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.cd_decorative))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.select_pdf))
        }
    }
}

@Composable
private fun LockContent(
    state: LockState,
    onUserPasswordChange: (String) -> Unit,
    onOwnerPasswordChange: (String) -> Unit,
    onAllowPrintingChange: (Boolean) -> Unit,
    onAllowCopyingChange: (Boolean) -> Unit,
    onAllowModifyingChange: (Boolean) -> Unit,
    onAllowAnnotationsChange: (Boolean) -> Unit,
    onOutputFileNameChange: (String) -> Unit,
    onOverwriteChange: (Boolean) -> Unit,
    onLock: () -> Unit,
    onClearError: () -> Unit,
    onChangeFile: () -> Unit,
    onPreview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Source file card
        SourceFileCard(
            sourceFile = requireNotNull(state.sourceFile),
            onPreview = onPreview,
            onChangeFile = onChangeFile
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Owner password section (required)
        SectionLabel(stringResource(R.string.section_owner_password))
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.owner_password_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = state.ownerPassword,
            onValueChange = onOwnerPasswordChange,
            label = stringResource(R.string.owner_password),
            isRequired = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // User password section (optional)
        SectionLabel(stringResource(R.string.section_user_password))
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.user_password_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = state.userPassword,
            onValueChange = onUserPasswordChange,
            label = stringResource(R.string.user_password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Permissions section
        SectionLabel(stringResource(R.string.section_permissions))
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.permissions_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItem(
            label = stringResource(R.string.allow_printing),
            checked = state.allowPrinting,
            onCheckedChange = onAllowPrintingChange
        )

        PermissionItem(
            label = stringResource(R.string.allow_copying_content),
            checked = state.allowCopying,
            onCheckedChange = onAllowCopyingChange
        )

        PermissionItem(
            label = stringResource(R.string.allow_modifying),
            checked = state.allowModifying,
            onCheckedChange = onAllowModifyingChange
        )

        PermissionItem(
            label = stringResource(R.string.allow_annotations),
            checked = state.allowAnnotations,
            onCheckedChange = onAllowAnnotationsChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Error message
        AnimatedVisibility(visible = state.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentRed,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onClearError,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_dismiss),
                            modifier = Modifier.size(16.dp),
                            tint = AccentRed
                        )
                    }
                }
            }
        }

        // Overwrite original checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOverwriteChange(!state.overwriteOriginal) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.overwriteOriginal,
                onCheckedChange = onOverwriteChange,
                colors = CheckboxDefaults.colors(checkedColor = AccentAmber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.overwrite_original_file), style = MaterialTheme.typography.bodyMedium)
        }

        // Output filename
        AnimatedVisibility(visible = !state.overwriteOriginal) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                val filenameValidation = remember(state.outputFileName) {
                    com.rejowan.pdfreaderpro.util.InputValidation.validateFileName(state.outputFileName)
                }
                val isFilenameError = state.outputFileName.isNotEmpty() &&
                        filenameValidation is com.rejowan.pdfreaderpro.util.InputValidation.ValidationResult.Invalid

                val focusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = state.outputFileName,
                    onValueChange = onOutputFileNameChange,
                    label = { Text(stringResource(R.string.output_file_name)) },
                    suffix = { Text(stringResource(R.string.pdf_extension)) },
                    singleLine = true,
                    isError = isFilenameError,
                    supportingText = if (isFilenameError && filenameValidation is com.rejowan.pdfreaderpro.util.InputValidation.ValidationResult.Invalid) {
                        {
                            Text(
                                text = stringResource(
                                    filenameValidation.errorMessageResId,
                                    *filenameValidation.formatArgs
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lock button
        Button(
            onClick = onLock,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing
        ) {
            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.cd_decorative))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.tool_lock_pdf))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f
        ),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SourceFileCard(
    sourceFile: SourceFile,
    onPreview: () -> Unit,
    onChangeFile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    if (sourceFile.thumbnail != null) {
                        Image(
                            bitmap = sourceFile.thumbnail.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_file_thumbnail),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = stringResource(R.string.cd_pdf_file),
                            modifier = Modifier.size(28.dp),
                            tint = AccentAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sourceFile.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.pages_size_format, sourceFile.pageCount, formatFileSize(sourceFile.size)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.Visibility,
                        contentDescription = stringResource(R.string.cd_decorative),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.preview))
                }
                OutlinedButton(
                    onClick = onChangeFile,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.change_file))
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    isRequired: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Validate password
    val validationResult = remember(value) {
        com.rejowan.pdfreaderpro.util.InputValidation.validatePassword(value, isRequired)
    }
    val isError = value.isNotEmpty() && validationResult is com.rejowan.pdfreaderpro.util.InputValidation.ValidationResult.Invalid

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = {
            when {
                isError && validationResult is com.rejowan.pdfreaderpro.util.InputValidation.ValidationResult.Invalid -> {
                    Text(
                        text = stringResource(
                            validationResult.errorMessageResId,
                            *validationResult.formatArgs
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                isRequired -> {
                    Text(
                        text = stringResource(R.string.password_min_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(if (passwordVisible) R.string.cd_hide_password else R.string.cd_show_password)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PermissionItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentAmber
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SuccessState(
    result: LockResult,
    onOpenInApp: () -> Unit,
    onShare: () -> Unit,
    onLockMore: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Success icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AccentGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.cd_success),
                modifier = Modifier.size(48.dp),
                tint = AccentGreen
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.pdf_locked),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.pdf_password_protected),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // File info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = stringResource(R.string.cd_pdf_file),
                    modifier = Modifier.size(20.dp),
                    tint = AccentAmber
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        File(result.outputPath).name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.pages_size_format, result.pageCount, formatFileSize(result.fileSize)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.Lock,
                    contentDescription = stringResource(R.string.cd_lock_pdf),
                    modifier = Modifier.size(18.dp),
                    tint = AccentAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenInApp,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.open), maxLines = 1)
            }
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(R.string.cd_decorative),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.share), maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onLockMore,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.new_file), maxLines = 1)
            }
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.done), maxLines = 1)
            }
        }
    }
}

@Composable
private fun ProcessingOverlay(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(56.dp),
                    color = AccentAmber,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.locking),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentAmber
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
