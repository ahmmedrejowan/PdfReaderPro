# Changelog

All notable changes to PDF Reader Pro will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
