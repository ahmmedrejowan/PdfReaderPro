<p align="center"><img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.svg" width="100%" align="center"/></p>
<p align="center"> <a href="https://www.android.com"><img src="https://img.shields.io/badge/platform-Android-yellow.svg" alt="platform"></a>
 <a href="https://android-arsenal.com/api?level=24"><img src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat" alt="API"></a> <a href="https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE"><img src="https://img.shields.io/github/license/ahmmedrejowan/PdfReaderPro" alt="GitHub license"></a> </p>

 <p align="center"> <a href="https://github.com/ahmmedrejowan/PdfReaderPro/issues"><img src="https://img.shields.io/github/issues/ahmmedrejowan/PdfReaderPro" alt="GitHub issues"></a> <a href="https://github.com/ahmmedrejowan/PdfReaderPro/network"><img src="https://img.shields.io/github/forks/ahmmedrejowan/PdfReaderPro" alt="GitHub forks"></a> <a href="https://github.com/ahmmedrejowan/PdfReaderPro/stargazers"><img src="https://img.shields.io/github/stars/ahmmedrejowan/PdfReaderPro" alt="GitHub stars"></a> <a href="https://github.com/ahmmedrejowan/PdfReaderPro/graphs/contributors"> <img src="https://img.shields.io/github/contributors/ahmmedrejowan/PdfReaderPro" alt="GitHub contributors"></a>   </p>
<hr>

## About

PDF Reader Pro is a modern, feature-rich PDF viewer for Android built with Jetpack Compose and Material 3. It offers a clean, intuitive interface with powerful PDF tools, all while respecting user privacy with no ads and no tracking.

**Version 2.0** is a complete rewrite using modern Android development practices including Kotlin, Jetpack Compose, Clean Architecture, and a custom PDF.js-based rendering engine.

## Features

### Core Features
- **PDF Viewing** - Smooth, responsive PDF rendering with zoom, scroll, and page navigation
- **File Management** - Browse, search, sort, and organize PDF files
- **Bookmarks** - Save and manage bookmarks within documents
- **Table of Contents** - Navigate documents using built-in outlines
- **Recent Files** - Quick access to recently viewed documents
- **Favorites** - Mark and access your favorite PDFs

### PDF Tools
- **Merge PDFs** - Combine multiple PDF files into one
- **Split PDF** - Extract pages or split into multiple files
- **Compress** - Reduce PDF file size
- **Rotate Pages** - Rotate individual or all pages
- **Reorder Pages** - Rearrange page order
- **Remove Pages** - Delete unwanted pages
- **Add Page Numbers** - Insert page numbers with customizable position
- **Add Watermark** - Text or image watermarks
- **Lock/Unlock** - Password protect or remove protection
- **PDF to Images** - Convert pages to images
- **Images to PDF** - Create PDF from images

### User Experience
- **Material 3 Design** - Modern, clean interface with dynamic colors
- **Dark/Light Theme** - System default or manual selection
- **Responsive Layouts** - Optimized for phones and tablets, portrait and landscape
- **Auto-Scroll** - Hands-free reading with adjustable speed
- **Search** - Full-text search within documents
- **Share** - Share PDFs with other apps

## Screenshots

| Shots | Shots | Shots |
| ----- | ----- | ----- |
| ![Screenshot 1](https://raw.githubusercontent.com/8ane/PDFReaderPro/main/Shots/Shots%20(1).png) | ![Screenshot 2](https://raw.githubusercontent.com/8ane/PDFReaderPro/main/Shots/Shots%20(2).png) | ![Screenshot 5](https://raw.githubusercontent.com/8ane/PDFReaderPro/main/Shots/Shots%20(5).png) |
| ![Screenshot 6](https://raw.githubusercontent.com/8ane/PDFReaderPro/main/Shots/Shots%20(6).png) | ![Screenshot 3](https://raw.githubusercontent.com/8ane/PDFReaderPro/main/Shots/Shots%20(3).png) | ![Screenshot 4](https://raw.githubusercontent.com/8ane/PDFReaderPro/main/Shots/Shots%20(4).png) |

## Download

Download the latest release from GitHub:

<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest">
<img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/1.png" width="256px" align="center"/>
</a>

<br>

Check out the [releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) section for all versions and changelogs.

## Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 with Dynamic Colors |
| **Architecture** | Clean Architecture + MVVM |
| **Dependency Injection** | Koin |
| **Database** | Room |
| **Preferences** | DataStore |
| **PDF Rendering** | Custom PDF.js WebView |
| **PDF Processing** | iText 7 |
| **Async** | Kotlin Coroutines + Flow |
| **Image Loading** | Coil |
| **Minimum SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 35 (Android 15) |

## Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or higher
- Android SDK with API 35

### Build Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   cd PdfReaderPro
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build the project**
   ```bash
   # Debug build
   ./gradlew assembleDebug

   # Release build (requires signing configuration)
   ./gradlew assembleRelease
   ```

4. **Run tests**
   ```bash
   # Unit tests
   ./gradlew testDebugUnitTest

   # All tests
   ./gradlew test
   ```

### Project Structure
```
app/src/main/java/com/rejowan/pdfreaderpro/
├── appClasses/        # Application class, global setup
├── di/                # Koin dependency injection modules
├── data/
│   ├── local/         # Room database, DAOs, entities
│   └── repository/    # Repository implementations
├── domain/
│   ├── model/         # Domain models
│   └── repository/    # Repository interfaces
├── presentation/
│   ├── components/    # Reusable UI components
│   ├── navigation/    # Navigation graph
│   ├── screens/       # Screen composables
│   └── theme/         # Material 3 theming
└── util/              # Utilities and helpers
```

## Known Issues

See [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for a list of known issues and workarounds.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and release notes.

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

### How to Contribute
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Guidelines
- Follow the existing code style and architecture
- Write unit tests for new features
- Update documentation as needed
- Keep pull requests focused and small

For major changes, please open an issue first to discuss what you would like to change.

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

```
Copyright 2024-2026 ahmmedrejowan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Acknowledgments

- [PDF.js](https://mozilla.github.io/pdf.js/) - Mozilla's PDF rendering library
- [iText](https://itextpdf.com/) - PDF processing library
- [Koin](https://insert-koin.io/) - Dependency injection framework
- [Coil](https://coil-kt.github.io/coil/) - Image loading library
