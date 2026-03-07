<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="PDF Reader Pro Logo" width="120" height="120">

<h3>Modern PDF Viewer for Android</h3>

  <p>
    A feature-rich, privacy-focused PDF app built with Jetpack Compose and Material 3. View, edit, and manage PDFs with 12 powerful tools.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
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

![GitHub Release](https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro)

You can download the latest APK from here

<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest">
<img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" width="224px" align="center"/>
</a>

Check out the [releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) section for more details.

---

## Screenshots

| Shots | Shots | Shots |
|-------|-------|-------|
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp) | ![Screenshot 2](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot2.webp) | ![Screenshot 3](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot3.webp) |
| ![Screenshot 4](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot4.webp) | ![Screenshot 5](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot5.webp) | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp) |
| ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp) | ![Screenshot 8](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot8.webp) | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp) |
| ![Screenshot 10](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot10.webp) | ![Screenshot 11](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot11.webp) | ![Screenshot 12](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot12.webp) |
| ![Screenshot 13](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot13.webp) | ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) |
| ![Screenshot 16](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot16.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 18](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot18.webp) |
| ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) | ![Screenshot 20](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot20.webp) | ![Screenshot 21](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot21.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 23](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot23.webp) | ![Screenshot 24](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot24.webp) |
| ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

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
- **PDF Processing**: iText 7
- **Image Loading**: Coil
- **Data Storage**: DataStore Preferences

---

## Requirements

- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 36 (Android 16)
- **Compile SDK**: API 36
- **Gradle**: 9.4.0
- **AGP**: 9.1.0
- **Kotlin**: 2.3.10
- **Java**: 17

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

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

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

- [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) - Ask questions, share ideas
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) - Report bugs, request features
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) - Download latest versions

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

### v2.0.0 (2026-03-07) - Complete Rewrite

- Complete UI rewrite with Jetpack Compose and Material 3
- 12 PDF tools (merge, split, compress, rotate, and more)
- Enhanced reader with auto-scroll, bookmarks, and night mode
- Clean Architecture with MVVM
- Custom PDF.js-based rendering engine
- 100% Kotlin, 100% offline

See [CHANGELOG.md](CHANGELOG.md) for full version history.

---
