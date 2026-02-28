package com.rejowan.pdfreaderpro.presentation.screens.reader.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageJumpSheet(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        PageJumpSideSheet(
            currentPage = currentPage,
            totalPages = totalPages,
            onPageSelected = onPageSelected,
            onDismiss = onDismiss
        )
    } else {
        PageJumpBottomSheet(
            currentPage = currentPage,
            totalPages = totalPages,
            onPageSelected = onPageSelected,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageJumpBottomSheet(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        PageJumpContent(
            currentPage = currentPage,
            totalPages = totalPages,
            onPageSelected = onPageSelected,
            onDismiss = onDismiss,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PageJumpSideSheet(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                PageJumpContent(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageSelected = onPageSelected,
                    onDismiss = onDismiss,
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
private fun PageJumpContent(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pageInput by remember {
        val text = currentPage.toString()
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isValidPage = pageInput.text.toIntOrNull()?.let { it in 1..totalPages } ?: false

    fun submitPage() {
        pageInput.text.toIntOrNull()?.let { page ->
            if (page in 1..totalPages) {
                keyboardController?.hide()
                onPageSelected(page)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Numbers,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Jump to Page",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enter a page number (1 - $totalPages)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input field
        OutlinedTextField(
            value = pageInput,
            onValueChange = { input ->
                if (input.text.all { it.isDigit() } || input.text.isEmpty()) {
                    pageInput = input
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "Page number",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = { submitPage() }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            isError = pageInput.text.isNotEmpty() && !isValidPage,
            shape = RoundedCornerShape(8.dp)
        )

        // Error text
        val showError = pageInput.text.isNotEmpty() && !isValidPage
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Please enter a number between 1 and $totalPages",
            style = MaterialTheme.typography.labelSmall,
            color = if (showError) MaterialTheme.colorScheme.error else Color.Transparent
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = { submitPage() },
                modifier = Modifier.weight(1f),
                enabled = isValidPage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Go", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}
