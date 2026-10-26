[English](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/README.md) | [Русский](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.ru.md) | [Deutsch](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.de.md) | [Español](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.es.md) | [Français](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.fr.md) | [Italiano](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.it.md) | [Português](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.pt.md)

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="PDF Reader Pro" width="120" height="120">

<h3>Modern PDF Viewer for Android</h3>

  <p>
    A feature-rich, privacy-focused PDF app built with Jetpack Compose and Material 3. View, edit, and manage PDFs with 12 powerful tools.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)
</div>

---

## Features

- **PDF Viewing** - Smooth rendering with zoom, scroll, and page navigation
- **12 PDF Tools** - Merge, split, compress, rotate, reorder, remove pages, add page numbers, watermark, lock/unlock, PDF to images, images to PDF
- **Bookmarks** - Save and manage bookmarks within documents
- **Table of Contents** - Navigate using document outlines
- **Search** - Full-text search within documents
- **Auto-Scroll** - Hands-free reading with adjustable speed
- **File Management** - Browse, search, sort, and organize PDFs
- **Recent & Favorites** - Quick access to your files
- **Material 3 Design** - Modern UI with dark mode and dynamic colors
- **100% Offline** - Complete privacy, no ads, no tracking

---

## Download

<p align="center">
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Get it on GitHub" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" height="72"/></a>&nbsp;&nbsp;
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/ahmmedrejowan/PdfReaderPro"><img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/obtainium.png" height="72"/></a>
</p>

<p align="center">
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro"/></a>
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/ahmmedrejowan/PdfReaderPro/total"/></a>
<img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&amp;logoColor=white"/>
</p>

Get the APK from GitHub, or add the app to Obtainium for automatic updates. The app can also check for updates itself.

---

## Screenshots

| | | | |
|---|---|---|---|
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp) | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp) | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp) | ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp) |
| ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

More screenshots of every screen are in the [wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Screenshots).

---

## Architecture

PDF Reader Pro follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/                      # Data layer
│   ├── local/
│   │   ├── database/          # Room database
│   │   └── preferences/       # DataStore preferences
│   └── repository/            # Repository implementations
│
├── domain/                    # Domain layer
│   ├── model/                 # Domain models
│   └── repository/            # Repository interfaces
│
├── presentation/              # Presentation layer (UI)
│   ├── components/            # Reusable Compose components
│   ├── navigation/            # Navigation graph
│   ├── screens/               # UI screens
│   │   ├── home/              # Home with file browser
│   │   ├── reader/            # PDF reader
│   │   ├── tools/             # PDF tools
│   │   └── settings/          # Settings
│   └── theme/                 # Material 3 theming
│
├── di/                        # Koin dependency injection
└── util/                      # Utilities
```

### Tech Stack

- **UI Framework**: Jetpack Compose (100% Compose UI)
- **Language**: Kotlin (100%)
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Koin
- **Database**: Room
- **Async**: Kotlin Coroutines + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **PDF Rendering**: Custom PDF.js WebView
- **PDF Processing**: iText 9
- **Image Loading**: Coil
- **Data Storage**: DataStore Preferences

---

## Requirements

- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 37
- **Compile SDK**: API 37
- **Gradle**: 9.8.0
- **AGP**: 9.4.1
- **Kotlin**: 2.4.20
- **Java**: 21

### Permissions

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_DOCUMENTS` - Access PDF files
- `WRITE_EXTERNAL_STORAGE` - Save processed PDFs (Android 9 and below)
- `INTERNET` - Optional, only for in-app update checks

**Note:** This app does not collect or transmit any user data.

---

## Build & Run

To build and run the project, follow these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Connect your Android device or start an emulator.
5. Click on the "Run" button in Android Studio to build and run the app.

---

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## Translations

[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)

PDF Reader Pro is available in 13 languages:

<!-- Each badge reads progress.N from https://badges.awesome-crowdin.com/stats-17953219-941021.json, where N is the language's position in that file; check the indexes there when adding a language. -->
| Language | English name | Code | Progress | Crowdin | Contributors |
|---|---|---|---|---|---|
| English | English | en | Source language | [Translate](https://crowdin.com/project/pdf-reader-pro) | [@ahmmedrejowan](https://github.com/ahmmedrejowan) |
| বাংলা | Bengali | bn | [![Bengali translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.0.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/bn) | [@ahmmedrejowan](https://github.com/ahmmedrejowan) |
| Deutsch | German | de | [![German translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.1.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/de) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Español | Spanish | es | [![Spanish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.2.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/es-ES) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Français | French | fr | [![French translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.3.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/fr) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Gaeilge | Irish | ga | [![Irish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.4.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/ga-IE) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Italiano | Italian | it | [![Italian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.5.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/it) | [@4kvoodoo](https://github.com/4kvoodoo) |
| 日本語 | Japanese | ja | [![Japanese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.6.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/ja) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Polski | Polish | pl | [![Polish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.7.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/pl) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Português | Portuguese | pt | [![Portuguese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.8.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/pt-PT) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Русский | Russian | ru | [![Russian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.9.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/ru) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Türkçe | Turkish | tr | [![Turkish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.10.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/tr) | [@4kvoodoo](https://github.com/4kvoodoo) |
| 中文 (简体) | Chinese (Simplified) | zh | [![Chinese (Simplified) translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.11.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Translate](https://crowdin.com/project/pdf-reader-pro/zh-CN) | [@ahmmedrejowan](https://github.com/ahmmedrejowan), [@Infinifar](https://github.com/Infinifar) |

Translations are made by the community on **[Crowdin](https://crowdin.com/project/pdf-reader-pro)**: sign in, pick your language and translate in the browser, no coding needed. Missing your language or have a question? Join the [translation discussion](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108).

---

## Accessibility

PDF Reader Pro works with TalkBack and the system font size. Full screen reader support arrives in the first release after 2.6.0.

| Area | Support |
|---|---|
| Screen reader (TalkBack) | ✅ Every button is labelled; options read their state; headings and live updates |
| Reading PDFs aloud | ✅ The text and links of a PDF (not scanned, image-only PDFs) |
| Font size | ✅ Follows the system font size |
| Dark and high-contrast themes | ✅ Dark, Black (AMOLED) and Sepia |
| Gestures | ⚠️ Reordering pages is drag only; the page scrubber has Jump to Page instead |
| Switch Access and keyboard | ❔ Not tested yet |

Details are on the [Accessibility wiki page](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Accessibility). Something hard to use? Tell us in the [accessibility discussion](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/114).

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CONTRIBUTING.md) for guidelines.

### Quick Start

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

```
Copyright (C) 2024-2026 K M Rejowan Ahmmed

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
```

> **Warning**
> This is a copyleft license. Any derivative work must also be open source under the same license.

---

## Community

- [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki) - Guides for using and building the app
- [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) - Ask questions, share ideas
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) - Report bugs, request features
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) - Download latest versions

<a href="https://www.producthunt.com/products/pdf-reader-pro-2?utm_source=badge-follow&utm_medium=badge&utm_source=badge-pdf&#0045;reader&#0045;pro&#0045;2" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/follow.svg?product_id=1273411&theme=light" alt="PDF&#0032;Reader&#0032;Pro - Modern&#0032;PDF&#0032;Viewer&#0032;for&#0032;Android | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>

---

## Author

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [kmrejowan@gmail.com](mailto:kmrejowan@gmail.com)

---

## Acknowledgments

- [PDF.js](https://mozilla.github.io/pdf.js/) - Mozilla's PDF rendering library
- [iText](https://itextpdf.com/) - PDF processing library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Koin](https://insert-koin.io/) - Dependency injection framework
- [Coil](https://coil-kt.github.io/coil/) - Image loading library

---

## Changelog

### v2.6.0 (2026-10-23)

- Back to page: after a jump, a chip takes you back to the page you left (#88)
- Open the PDF tools from the reader on the open document (#105)
- Keep image colors in the Dark and Black reading themes (#87)
- A setting to hide files and folders whose names start with a dot (#6)
- Short documents sit in the middle of the screen (#105)
- Fixed the first page and jumps landing under the top bar (#105)
- Fixed signed files disappearing from other apps' file pickers
- Fixed counts for a single item ("1 page"), and Back now closes side panels in landscape
- Fixed Retry on the Merge screen in every language

### v2.5.0 (2026-10-09)

- Sign documents by drawing, typing or picking an image of your signature, from the reader or Tools
- Signatures are kept per document, managed from one sheet, and written into the file or a copy
- Bangla, Russian, German, Spanish, French, Italian, Portuguese, Turkish, Polish, Irish and Japanese translations (#91, #93, #95, #98)
- Choose the app's language in Settings, on any Android version
- Translators like Google Translate appear in the text selection menu (#78)
- Print progress, and saving a copy without the password
- Crash screen with full details to copy, share or report on GitHub
- Fixed Japanese, Chinese and Korean PDFs that showed an error instead of opening (#89)
- Fixed Document Info crashing in landscape and the page slider covering the menu button (#80, #79)
- Updated core libraries (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0)

### v2.4.0 (2026-08-15)

- Highlight text in five colours, saved per document (#41)
- Highlights panel with search, colour filter and jump-to-highlight (#41)
- Next/previous navigation between highlights (#41)
- In-PDF search shows how many matches are already highlighted (#41)
- Save a copy with highlights written into the PDF as real annotations (#41)
- Highlights already present in a file are listed and searchable, read only (#41)
- Lock horizontal scroll when zoomed in, saved per document (#74)

See [CHANGELOG.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CHANGELOG.md) for full version history.

---
