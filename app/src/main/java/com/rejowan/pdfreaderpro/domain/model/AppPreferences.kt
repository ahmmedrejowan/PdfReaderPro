package com.rejowan.pdfreaderpro.domain.model

data class AppPreferences(
    val isFirstLaunch: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val defaultViewMode: ViewMode = ViewMode.LIST,
    val defaultSortOption: SortOption = SortOption.NAME_ASC,
    val keepScreenOn: Boolean = false,
    val rememberPasswords: Boolean = true,
    val defaultScrollDirection: ScrollDirection = ScrollDirection.VERTICAL
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class AccentColor(val colorName: String) {
    DEFAULT("Purple"),
    OCEAN("Ocean"),
    SUNSET("Sunset"),
    FOREST("Forest"),
    ROSE("Rose")
}

enum class ScrollDirection {
    VERTICAL,
    HORIZONTAL
}
