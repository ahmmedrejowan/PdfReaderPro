[English](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/README.md) | [Русский](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.ru.md) | [Deutsch](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.de.md) | [Español](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.es.md) | [Français](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.fr.md) | [Italiano](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.it.md) | [Português](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.pt.md)

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="PDF Reader Pro Logo" width="120" height="120">

<h3>Moderner PDF-Viewer für Android</h3>

  <p>
    Eine funktionsreiche, datenschutzorientierte PDF-App, entwickelt mit Jetpack Compose und Material 3. Anzeigen, bearbeiten und verwalten Sie PDFs mit 12 leistungsstarken Werkzeugen.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)
</div>

---

## Funktionen

- **PDF-Anzeige** – Flüssiges Rendering mit Zoom, Scrollen und Seitennavigation
- **12 PDF-Werkzeuge** – Zusammenführen, Aufteilen, Komprimieren, Drehen, Neu anordnen, Seiten entfernen, Seitennummern hinzufügen, Wasserzeichen, Sperren/Entsperren, PDF in Bilder, Bilder in PDF
- **Lesezeichen** – Lesezeichen in Dokumenten speichern und verwalten
- **Inhaltsverzeichnis** – Navigation mithilfe der Dokumentstruktur
- **Suche** – Volltextsuche innerhalb von Dokumenten
- **Automatisches Scrollen** – Freihändiges Lesen mit einstellbarer Geschwindigkeit
- **Dateiverwaltung** – PDFs durchsuchen, suchen, sortieren und organisieren
- **Zuletzt verwendet & Favoriten** – Schnellzugriff auf Ihre Dateien
- **Material 3 Design** – Moderne Benutzeroberfläche mit Dunkelmodus und dynamischen Farben
- **100 % offline** – Vollständiger Datenschutz, keine Werbung, kein Tracking

---

## Herunterladen

<p align="center">
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Get it on GitHub" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" height="72"/></a>&nbsp;&nbsp;
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/ahmmedrejowan/PdfReaderPro"><img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/obtainium.png" height="72"/></a>
</p>

<p align="center">
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro"/></a>
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/ahmmedrejowan/PdfReaderPro/total"/></a>
<img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&amp;logoColor=white"/>
</p>

Lade die APK von GitHub herunter oder füge die App zu Obtainium hinzu, um Updates automatisch zu erhalten. Die App kann auch selbst nach Updates suchen.

---

## Screenshots

| | | | |
|---|---|---|---|
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp) | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp) | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp) | ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp) |
| ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

Weitere Screenshots aller Bildschirme findest du im [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Screenshots).

---

## Architektur

PDF Reader Pro folgt den Prinzipien der **Clean Architecture** mit dem **MVVM**-Muster:

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

### Tech-Stack

- **UI-Framework**: Jetpack Compose (100 % Compose UI)
- **Sprache**: Kotlin (100 %)
- **Architektur**: MVVM + Clean Architecture
- **Abhängigkeitsinjektion**: Koin
- **Datenbank**: Room
- **Asynchronität**: Kotlin Coroutines + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **PDF-Rendering**: Benutzerdefinierter PDF.js WebView
- **PDF-Verarbeitung**: iText 9
- **Bildladen**: Coil
- **Datenspeicherung**: DataStore Preferences

---

## Anforderungen

- **Minimales SDK**: API 24 (Android 7.0 Nougat)
- **Ziel-SDK**: API 37
- **Compile SDK**: API 37
- **Gradle**: 9.8.0
- **AGP**: 9.4.1
- **Kotlin**: 2.4.20
- **Java**: 21

### Berechtigungen

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_DOCUMENTS` – Zugriff auf PDF-Dateien
- `WRITE_EXTERNAL_STORAGE` – Verarbeitete PDFs speichern (Android 9 und niedriger)
- `INTERNET` – Optional, nur für In-App-Update-Prüfungen

**Hinweis:** Diese App sammelt oder überträgt keine Benutzerdaten.

---

## Erstellen & Ausführen

Um das Projekt zu erstellen und auszuführen, befolgen Sie diese Schritte:

1. Klonen Sie das Repository:
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   ```
2. Öffnen Sie das Projekt in Android Studio.
3. Synchronisieren Sie das Projekt mit den Gradle-Dateien.
4. Verbinden Sie Ihr Android-Gerät oder starten Sie einen Emulator.
5. Klicken Sie in Android Studio auf die Schaltfläche „Run“, um die App zu erstellen und auszuführen.

---

## Testen

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## Übersetzungen

[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)

PDF Reader Pro ist in 13 Sprachen verfügbar:

<!-- Each badge reads progress.N from https://badges.awesome-crowdin.com/stats-17953219-941021.json, where N is the language's position in that file; check the indexes there when adding a language. -->
| Sprache | Englischer Name | Code | Fortschritt | Crowdin | Mitwirkende |
|---|---|---|---|---|---|
| English | English | en | Ausgangssprache | [Übersetzen](https://crowdin.com/project/pdf-reader-pro) | [@ahmmedrejowan](https://github.com/ahmmedrejowan) |
| বাংলা | Bengali | bn | [![Bengali translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.0.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/bn) | [@ahmmedrejowan](https://github.com/ahmmedrejowan) |
| Deutsch | German | de | [![German translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.1.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/de) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Español | Spanish | es | [![Spanish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.2.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/es-ES) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Français | French | fr | [![French translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.3.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/fr) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Gaeilge | Irish | ga | [![Irish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.4.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/ga-IE) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Italiano | Italian | it | [![Italian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.5.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/it) | [@4kvoodoo](https://github.com/4kvoodoo) |
| 日本語 | Japanese | ja | [![Japanese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.6.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/ja) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Polski | Polish | pl | [![Polish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.7.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/pl) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Português | Portuguese | pt | [![Portuguese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.8.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/pt-PT) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Русский | Russian | ru | [![Russian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.9.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/ru) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Türkçe | Turkish | tr | [![Turkish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.10.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/tr) | [@4kvoodoo](https://github.com/4kvoodoo) |
| 中文 (简体) | Chinese (Simplified) | zh | [![Chinese (Simplified) translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.11.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Übersetzen](https://crowdin.com/project/pdf-reader-pro/zh-CN) | [@ahmmedrejowan](https://github.com/ahmmedrejowan), [@Infinifar](https://github.com/Infinifar) |

Die Übersetzungen entstehen in der Community auf **[Crowdin](https://crowdin.com/project/pdf-reader-pro)**: anmelden, Sprache wählen und direkt im Browser übersetzen, ganz ohne Programmierkenntnisse. Deine Sprache fehlt oder du hast eine Frage? Schau in der [Übersetzungsdiskussion](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108) vorbei.

---

## Barrierefreiheit

PDF Reader Pro funktioniert mit TalkBack und der Schriftgröße des Systems. Die volle Screenreader-Unterstützung kommt mit der ersten Version nach 2.6.0.

| Bereich | Unterstützung |
|---|---|
| Screenreader (TalkBack) | ✅ Alle Schaltflächen beschriftet; Optionen nennen ihren Zustand; Überschriften und Live-Meldungen |
| PDFs vorlesen | ✅ Text und Links einer PDF (keine gescannten, reinen Bild-PDFs) |
| Schriftgröße | ✅ Folgt der Schriftgröße des Systems |
| Dunkle und kontrastreiche Designs | ✅ Dunkel, Schwarz (AMOLED) und Sepia |
| Gesten | ⚠️ Seiten neu anordnen nur per Ziehen; der Seitenregler bietet stattdessen „Zu Seite springen“ |
| Schalterzugriff und Tastatur | ❔ Noch nicht getestet |

Details auf der [Wiki-Seite zur Barrierefreiheit](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Accessibility). Etwas schwer zu bedienen? Schreib uns in der [Diskussion zur Barrierefreiheit](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/114).

---

## Mitwirken

Beiträge sind willkommen! Bitte lesen Sie [CONTRIBUTING.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CONTRIBUTING.md) für Richtlinien.

### Schnellstart

1. Forken Sie das Repository
2. Erstellen Sie Ihren Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. Committen Sie Ihre Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Pushen Sie in den Branch (`git push origin feature/AmazingFeature`)
5. Öffnen Sie einen Pull Request

---

## Lizenz

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

> **Warnung**
> Dies ist eine Copyleft-Lizenz. Jedes abgeleitete Werk muss ebenfalls unter derselben Lizenz quelloffen sein.

---

## Community

- [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki) – Anleitungen zur Nutzung und Entwicklung der App
- [Diskussionen](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) – Stellen Sie Fragen, teilen Sie Ideen
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) – Melden Sie Fehler, fordern Sie Funktionen an
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) – Laden Sie die neuesten Versionen herunter

<a href="https://www.producthunt.com/products/pdf-reader-pro-2?utm_source=badge-follow&utm_medium=badge&utm_source=badge-pdf&#0045;reader&#0045;pro&#0045;2" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/follow.svg?product_id=1273411&theme=light" alt="PDF&#0032;Reader&#0032;Pro - Modern&#0032;PDF&#0032;Viewer&#0032;for&#0032;Android | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>

---

## Autor

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [kmrejowan@gmail.com](mailto:kmrejowan@gmail.com)

---

## Danksagungen

- [PDF.js](https://mozilla.github.io/pdf.js/) – Mozillas PDF-Rendering-Bibliothek
- [iText](https://itextpdf.com/) – PDF-Verarbeitungsbibliothek
- [Jetpack Compose](https://developer.android.com/jetpack/compose) – Modernes Android-UI-Toolkit
- [Material Design 3](https://m3.material.io/) – Designsystem
- [Koin](https://insert-koin.io/) – Framework für Abhängigkeitsinjektion
- [Coil](https://coil-kt.github.io/coil/) – Bibliothek zum Laden von Bildern

---

## Änderungsprotokoll

### v2.6.0 (23.10.2026)

- Zurück zur Seite: Nach einem Sprung führt ein Chip zurück zur vorherigen Seite (#88)
- PDF-Werkzeuge direkt im Reader für das geöffnete Dokument (#105)
- Bildfarben in den Lesethemen Dunkel und Schwarz beibehalten (#87)
- Einstellung zum Ausblenden von Dateien und Ordnern, deren Name mit einem Punkt beginnt (#6)
- Kurze Dokumente stehen in der Mitte des Bildschirms (#105)
- Erste Seite und Sprünge landen nicht mehr unter der oberen Leiste (#105)
- Signierte Dateien verschwinden nicht mehr aus der Dateiauswahl anderer Apps
- Anzahlen für ein einzelnes Element korrigiert („1 Seite“), und Zurück schließt Seitenleisten im Querformat
- Wiederholen auf dem Zusammenführen-Bildschirm funktioniert in jeder Sprache

### v2.5.0 (09.10.2026)

- Dokumente unterschreiben: Unterschrift zeichnen, tippen oder als Bild wählen, im Reader oder unter Werkzeuge
- Unterschriften werden pro Dokument gespeichert, in einem Bereich verwaltet und in die Datei oder eine Kopie geschrieben
- Übersetzungen ins Bengalische, Russische, Deutsche, Spanische, Französische, Italienische, Portugiesische, Türkische, Polnische, Irische und Japanische (#91, #93, #95, #98)
- Die App-Sprache lässt sich in den Einstellungen wählen, auf jeder Android-Version
- Übersetzer wie Google Übersetzer erscheinen im Menü für markierten Text (#78)
- Fortschrittsanzeige beim Drucken und Speichern einer Kopie ohne Passwort
- Absturzbildschirm mit allen Details zum Kopieren, Teilen oder Melden auf GitHub
- Japanische, chinesische und koreanische PDFs, die einen Fehler zeigten, öffnen sich jetzt (#89)
- Absturz der Dokumentinfo im Querformat und vom Seitenregler verdeckte Menütaste behoben (#80, #79)
- Kernbibliotheken aktualisiert (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0)

### v2.4.0 (15.08.2026)

- Text in fünf Farben hervorheben, pro Dokument gespeichert (#41)
- Hervorhebungsbereich mit Suche, Farbfilter und Sprung zur Hervorhebung (#41)
- Vor-/Zurück-Navigation zwischen Hervorhebungen (#41)
- Die PDF-interne Suche zeigt an, wie viele Übereinstimmungen bereits hervorgehoben sind (#41)
- Eine Kopie mit in das PDF geschriebenen Hervorhebungen als echte Anmerkungen speichern (#41)
- Bereits in einer Datei vorhandene Hervorhebungen werden aufgelistet und sind durchsuchbar, nur lesbar (#41)
- Horizontales Scrollen bei Zoom sperren, pro Dokument gespeichert (#74)

Die vollständige Versionshistorie finden Sie in [CHANGELOG.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CHANGELOG.md).

---
