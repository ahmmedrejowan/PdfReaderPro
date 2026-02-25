package com.rejowan.pdfreaderpro.presentation.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.presentation.components.EmptySearchState
import com.rejowan.pdfreaderpro.presentation.components.FileOptionsSheet
import com.rejowan.pdfreaderpro.presentation.components.LoadingState
import com.rejowan.pdfreaderpro.presentation.components.PdfListItem
import com.rejowan.pdfreaderpro.presentation.navigation.navigateToReader
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: SearchViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var selectedFile by remember { mutableStateOf<PdfFile?>(null) }
    var selectedFileFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialQuery.isNotBlank()) {
            viewModel.setSearchQuery(initialQuery)
        }
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search PDFs...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isSearching -> {
                    LoadingState()
                }
                searchQuery.isBlank() -> {
                    SearchHint()
                }
                searchResults.isEmpty() -> {
                    EmptySearchState(query = searchQuery)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = searchResults,
                            key = { it.id }
                        ) { file ->
                            PdfListItem(
                                pdfFile = file,
                                onClick = {
                                    navController.navigateToReader(file.path)
                                },
                                onOptionsClick = {
                                    selectedFile = file
                                    scope.launch {
                                        selectedFileFavorite = viewModel.isFavorite(file.path)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // File Options Sheet
    selectedFile?.let { file ->
        FileOptionsSheet(
            pdfFile = file,
            isFavorite = selectedFileFavorite,
            onDismiss = { selectedFile = null },
            onFavoriteClick = { viewModel.toggleFavorite(file) },
            onShareClick = { /* TODO */ },
            onRenameClick = { /* TODO */ },
            onInfoClick = { /* TODO */ },
            onDeleteClick = { /* TODO */ }
        )
    }
}

@Composable
private fun SearchHint() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Search Tips",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "• Search by file name\n• Search by folder name\n• Use partial matches",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
