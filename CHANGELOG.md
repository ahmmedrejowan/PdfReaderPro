# Changelog

All notable changes to PDF Reader Pro will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

---

## [2.5.0] - 2026-10-09

### Added
- **Signatures** - Sign a document by drawing, typing or picking an image of your signature, from the reader or from Tools; signatures are kept per document, managed from one sheet, and written into the file after a confirmation or saved into a copy
- **Translations** - Russian, German, Spanish, French, Italian, Portuguese, Turkish, Polish, Irish and Japanese (#91, #93, #95, #98)
- **App Language** - The app's languages are offered in Android's per-app language setting (Android 13 and later)
- **Translate Selected Text** - Translators and other text apps, such as Google Translate, appear in the text selection menu (closes #78)
- **Print Progress** - Printing shows how far the pages have been prepared
- **Save a Copy Without the Password** - Save an unprotected copy of a password-protected document from the reader
- **Crash Details** - The crash screen shows the full details and can copy them, share them, or open a GitHub issue with the bug form filled in

### Changed
- **Dates** - Dates and times follow the app's language
- **Update Notes** - The update screen shows only what's new, formatted
- **Libraries** - Updated Gradle 9.8.0, AGP 9.4.1, Kotlin 2.4.20, Compose BOM 2026.09.00, iText 9.8.0 and BouncyCastle 1.86

### Fixed
- **Japanese, Chinese and Korean PDFs** - Documents that need character maps, such as many Japanese, Chinese and Korean files, now open instead of showing an error (closes #89)
- **Document Info in Landscape** - Document Info and File Info no longer crash in landscape (closes #80)
- **Menu Button** - The page slider no longer covers the reader's ⋮ button (closes #79)
- **Crash Screen** - The crash screen now appears after any crash instead of the app freezing
- **Try Again** - Try Again on the reader's error screen opens the document again

---

## [2.4.0] - 2026-08-15

### Added
- **Text Highlights** - Select any text and highlight it in one of five colours from the action bar that appears below the selection; highlights are saved per document and restored when the file is reopened (closes #41)
- **Highlights Panel** - Lists every highlight with its text, page and colour, searchable and filterable by colour; tapping an entry jumps to it and pulses it
- **Highlight Navigation** - Next/previous strip that steps through highlights in reading order and wraps at both ends
- **Search and Highlights** - The in-PDF search bar shows how many of the current matches are already highlighted; tapping that count opens the panel filtered to the same query
- **Save a Copy with Highlights** - Writes highlights into a new PDF as real annotations so they appear in other PDF readers; the original file is never modified, and the action is confirmed with a checkbox
- **Highlights Already in a File** - Highlights the PDF itself carries, whether exported from this app or added elsewhere, are now listed, searchable and included in navigation; they are read only, since editing them would mean rewriting the document
- **Lock Horizontal Scroll** - Setting (View Options) that holds the page where it is when zoomed in, saved per document; available in vertical scroll mode only (closes #74)

### Changed
- **Selection Menu** - Highlighting is now a single tap from the app's own bar below the selection, rather than being reached through the system menu's overflow; the system menu is unchanged

### Fixed
- **Viewer Layout** - Hardened the viewer against a latent sidebar offset that could shift the page sideways with nothing visible in the gap

---

## [2.3.0] - 2026-07-19

### Added
- **Page Slider on Scroll** - Setting (View Mode sheet) to control whether the page slider appears while scrolling when the toolbar is hidden; off by default (closes #54)

### Changed
- **In-PDF Search Navigation** - Next/previous now steps through every match across all pages reliably, and the active match is centered in the view (#40)
- **Dependency Updates** - Kotlin 2.4.10, AGP 9.2.1, Android compile/target SDK 37, Compose BOM, and other core libraries

### Fixed
- **PDF Open Failure** - PDFs whose file path contained an apostrophe no longer get stuck on the loading screen (closes #57)

---

## [2.2.0] - 2026-04-23

### Added
- **AMOLED Black Theme** - True-black variant under Appearance → Theme for OLED displays
- **Customizable Double-Tap Zoom** - Slider in Settings and the reader's zoom sheet sets the zoom level (1.1×–5×) applied on double-tap
- **Focal-Point Double-Tap** - Double-tap now zooms around the tapped location instead of anchoring to the top-left; zoom-out preserves the focal point as well
- **Hide Tools Tab** - Setting to remove the Tools tab from the bottom navigation for a reader-only experience

### Changed
- **Reader Sheet Typography** - Section headers and chip labels enlarged for legibility; More Options rows given more breathing room (closes #44)
- **Reader Chrome Theming** - Top bar, floating control bar, and sidebar now follow the active Material color scheme (fixes reader chrome not adopting Dark/Black themes)
- **Home Bottom Navigation** - Selected/unselected text colors now follow the app theme instead of the system theme (fixes invisible text in Black theme)

### Fixed
- **Remember Password** - Saved password is now auto-submitted on next open of a locked PDF; stale entries are cleaned up on silent-attempt failure (closes #43)

---

## [2.1.3] - 2026-03-24

### Fixed
- F-Droid build compatibility (removed JVM toolchain requirement)

---

## [2.1.2] - 2026-03-24

### Fixed
- Build compatibility with standard OpenJDK (removed JetBrains JDK requirement)

---

## [2.1.1] - 2026-03-24

### Added
- F-Droid metadata and fastlane structure for app store listing

---

## [2.1.0] - 2026-03-23

### Added
- **Horizontal Page Scrubber** - Page scrubber now adapts to scroll direction; horizontal scrubber appears above the bottom bar in horizontal scroll mode (closes #16)
- **Global Snap to Pages Setting** - Persist snap-to-pages preference across sessions
- **Global Screen Orientation Setting** - Lock screen to auto/portrait/landscape from settings

### Changed
- Simplified view mode options by removing unsupported spread modes and page alignment
- Streamlined reader settings UI in ViewModeSheet

### Fixed
- Fixed GitHub release workflow permissions for APK uploads

---

## [2.0.0] - 2026-03-07

### Added
- **Complete UI Rewrite** - Rebuilt entirely with Jetpack Compose and Material 3
- Comprehensive error handling with user-friendly messages
- Storage space pre-flight checks before file operations
- Retry functionality for recoverable errors
- Database indexes for improved query performance
- **PDF Tools Suite**
  - Merge multiple PDFs with page selection
  - Split PDFs by page ranges
  - Compress PDFs with quality options
  - Rotate pages (90°, 180°, 270°)
  - Reorder pages with drag-and-drop
  - Remove unwanted pages
  - Add page numbers with customizable styles
  - Add text or image watermarks
  - Password protect PDFs (lock)
  - Remove password protection (unlock)
  - Convert PDF to images
  - Convert images to PDF
- **Enhanced Reader**
  - Auto-scroll with adjustable speed
  - Page scrubber for quick navigation
  - Display options (single page, continuous scroll)
  - Zoom controls with pinch-to-zoom
  - Night mode / sepia mode
- **Bookmarks** - Save and manage bookmarks within documents
- **Table of Contents** - Navigate using document outline
- **Attachments** - View and download PDF attachments
- **Search** - Full-text search within documents
- **Responsive Layouts** - Optimized for phones, tablets, portrait, and landscape
- **Dynamic Colors** - Material You color theming on Android 12+
- **In-App Updates** - Check for and download updates from GitHub

### Changed
- **Architecture** - Migrated to Clean Architecture with MVVM
- **PDF Engine** - Replaced AndroidPdfViewer with custom PDF.js WebView
- **Database** - Migrated from SQLite to Room
- **Preferences** - Migrated from SharedPreferences to DataStore
- **DI Framework** - Updated Koin configuration with lazy singletons
- **Minimum SDK** - Raised to API 24 (Android 7.0)
- **Target SDK** - Updated to API 35 (Android 15)
- Optimized app startup time with deferred initialization
- Improved WebView memory management for large PDFs

### Fixed
- Fixed recomposition issues with stable keys in lazy lists
- Fixed input validation for passwords and filenames
- Fixed keyboard handling with proper IME actions across all text fields
- Fixed accessibility with content descriptions on all interactive elements

### Removed
- Legacy XML layouts (replaced with Compose)
- Java code (now 100% Kotlin)
- AndroidPdfViewer dependency

### Security
- Encrypted password storage using DataStore
- ProGuard/R8 obfuscation enabled
- No external network calls except for update checks
- FileProvider for secure file sharing

---

## [1.0.0] - 2024-01-15

### Added
- Initial release
- Basic PDF viewing functionality
- File browser with list/grid views
- Recent files tracking
- Favorites management
- Light/Dark theme support
- Search functionality

---

## Version History

| Version | Release Date | Highlights |
|---------|--------------|------------|
| 2.1.0 | 2026-03-23 | Horizontal scrubber, global settings |
| 2.0.0 | 2026-03-07 | Complete Compose rewrite, PDF tools |
| 1.0.0 | 2024-01-15 | Initial release |

---

[Unreleased]: https://github.com/ahmmedrejowan/PdfReaderPro/compare/v2.1.0...HEAD
[2.1.0]: https://github.com/ahmmedrejowan/PdfReaderPro/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/ahmmedrejowan/PdfReaderPro/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/ahmmedrejowan/PdfReaderPro/releases/tag/v1.0.0
