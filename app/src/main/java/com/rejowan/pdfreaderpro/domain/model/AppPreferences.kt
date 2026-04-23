package com.rejowan.pdfreaderpro.domain.model

data class AppPreferences(
    // App settings
    val isFirstLaunch: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultViewMode: ViewMode = ViewMode.LIST,
    val defaultSortOption: SortOption = SortOption.NAME_ASC,
    val rememberPasswords: Boolean = true,
    val updateCheckInterval: UpdateCheckInterval = UpdateCheckInterval.WEEKLY,

    // Reader settings (global)
    val readerBrightness: Float = -1f, // -1 = system default, 0-1 = custom
    val readerScrollMode: ScrollMode = ScrollMode.VERTICAL,
    val readerAutoHideToolbar: Boolean = false,
    val readerQuickZoomPreset: QuickZoomPreset = QuickZoomPreset.FIT_WIDTH,
    val readerDoubleTapZoom: Float = 2.0f,
    val readerKeepScreenOn: Boolean = false,
    val readerTheme: ReadingTheme = ReadingTheme.LIGHT,
    val readerSnapToPages: Boolean = false,
    val readerScreenOrientation: ScreenOrientation = ScreenOrientation.AUTO
)

enum class ThemeMode {
    LIGHT,
    DARK,
    BLACK,
    SYSTEM
}

enum class ScrollMode {
    VERTICAL,
    HORIZONTAL
}

enum class QuickZoomPreset {
    FIT_PAGE,
    FIT_WIDTH,
    ACTUAL_SIZE // 100%
}

enum class ReadingTheme {
    LIGHT,
    SEPIA,
    DARK,
    BLACK // AMOLED
}

enum class ScreenOrientation {
    AUTO,
    PORTRAIT,
    LANDSCAPE
}

enum class UpdateCheckInterval(val days: Int, val displayName: String) {
    NEVER(0, "Never"),
    DAILY(1, "Daily"),
    THREE_DAYS(3, "Every 3 days"),
    WEEKLY(7, "Weekly"),
    BIWEEKLY(14, "Every 2 weeks"),
    MONTHLY(30, "Monthly")
}
