package com.rejowan.pdfreaderpro.domain.model

enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    SIZE_DESC("Size (Largest)"),
    SIZE_ASC("Size (Smallest)")
}

enum class FolderSortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    COUNT_DESC("PDF Count (High)"),
    COUNT_ASC("PDF Count (Low)")
}

enum class ViewMode {
    LIST,
    GRID
}
