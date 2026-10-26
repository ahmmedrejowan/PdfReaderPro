[English](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/README.md) | [Русский](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.ru.md) | [Deutsch](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.de.md) | [Español](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.es.md) | [Français](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.fr.md) | [Italiano](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.it.md) | [Português](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.pt.md)

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="PDF Reader Pro" width="120" height="120">

<h3>Visor de PDF moderno para Android</h3>

  <p>
    Una aplicación PDF rica en funciones y centrada en la privacidad, creada con Jetpack Compose y Material 3. Visualiza, edita y gestiona PDF con 12 potentes herramientas.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)

</div>

---

## Funciones

- **Visualización de PDF** – Renderizado fluido con zoom, desplazamiento y navegación por páginas
- **12 herramientas PDF** – Combinar, dividir, comprimir, rotar, reordenar, eliminar páginas, añadir números de página, marca de agua, bloquear/desbloquear, PDF a imágenes, imágenes a PDF
- **Marcadores** – Guarda y gestiona marcadores dentro de los documentos
- **Tabla de contenidos** – Navega usando la estructura del documento
- **Búsqueda** – Búsqueda de texto completo dentro de los documentos
- **Desplazamiento automático** – Lectura sin manos con velocidad ajustable
- **Gestión de archivos** – Explora, busca, ordena y organiza PDF
- **Recientes y favoritos** – Acceso rápido a tus archivos
- **Diseño Material 3** – Interfaz moderna con modo oscuro y colores dinámicos
- **100 % sin conexión** – Privacidad total, sin anuncios, sin seguimiento

---

## Descargar

<p align="center"><a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Get it on GitHub" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" height="72"/></a>&nbsp;&nbsp;
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/ahmmedrejowan/PdfReaderPro"><img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/obtainium.png" height="72"/></a>
</p>

<p align="center"><a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro"/></a>
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/ahmmedrejowan/PdfReaderPro/total"/></a>
<img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&amp;logoColor=white"/>
</p>

Descarga el APK desde GitHub o añade la app a Obtainium para recibir actualizaciones automáticas. La app también puede buscar actualizaciones por sí misma.

---

## Capturas de pantalla

|                                                                                                         |                                                                                                         |                                                                                                         |                                                                                                         |
| ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp)   | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp)   | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp)   | ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp)   |
| ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

Hay más capturas de todas las pantallas en la [wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Screenshots).

---

## Arquitectura

PDF Reader Pro sigue los principios de **Clean Architecture** con el patrón **MVVM**:

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

### Stack tecnológico

- **Framework de UI**: Jetpack Compose (100 % Compose UI)
- **Lenguaje**: Kotlin (100 %)
- **Arquitectura**: MVVM + Clean Architecture
- **Inyección de dependencias**: Koin
- **Base de datos**: Room
- **Asincronía**: Kotlin Coroutines + StateFlow
- **Navegación**: Jetpack Navigation Compose
- **Renderizado de PDF**: WebView personalizado con PDF.js
- **Procesamiento de PDF**: iText 9
- **Carga de imágenes**: Coil
- **Almacenamiento de datos**: DataStore Preferences

---

## Requisitos

- **SDK mínimo**: API 24 (Android 7.0 Nougat)
- **SDK objetivo**: API 37
- **Compile SDK**: API 37
- **Gradle**: 9.8.0
- **AGP**: 9.4.1
- **Kotlin**: 2.4.20
- **Java**: 21

### Permisos

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_DOCUMENTS` – Acceso a archivos PDF
- `WRITE_EXTERNAL_STORAGE` – Guardar PDF procesados (Android 9 y anteriores)
- `INTERNET` – Opcional, solo para comprobaciones de actualizaciones dentro de la app

**Nota:** Esta aplicación no recopila ni transmite ningún dato del usuario.

---

## Compilar y ejecutar

Para compilar y ejecutar el proyecto, sigue estos pasos:

1. Clona el repositorio:
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   ```
2. Abre el proyecto en Android Studio.
3. Sincroniza el proyecto con los archivos Gradle.
4. Conecta tu dispositivo Android o inicia un emulador.
5. Haz clic en el botón «Run» en Android Studio para compilar y ejecutar la app.

---

## Pruebas

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## Traducciones

[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)

PDF Reader Pro está disponible en 13 idiomas:

<!-- Each badge reads progress.N from https://badges.awesome-crowdin.com/stats-17953219-941021.json, where N is the language's position in that file; check the indexes there when adding a language. -->

| Idioma                     | Nombre en inglés                        | Código | Progreso                                                                                                                                                                                                                                                                                                                                 | Crowdin                                                      | Colaboradores                                                                                                            |
| -------------------------- | --------------------------------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------ |
| English                    | English                                 | en     | Idioma de origen                                                                                                                                                                                                                                                                                                                         | [Traducir](https://crowdin.com/project/pdf-reader-pro)       | [@ahmmedrejowan](https://github.com/ahmmedrejowan)                                                          |
| বাংলা                      | Bengali                                 | bn     | [![Bengali translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.0.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traducir](https://crowdin.com/project/pdf-reader-pro/bn)    | [@ahmmedrejowan](https://github.com/ahmmedrejowan)                                                          |
| Deutsch                    | German                                  | de     | [![German translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.1.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traducir](https://crowdin.com/project/pdf-reader-pro/de)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Español                    | Spanish                                 | es     | [![Spanish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.2.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traducir](https://crowdin.com/project/pdf-reader-pro/es-ES) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Français                   | French                                  | fr     | [![French translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.3.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traducir](https://crowdin.com/project/pdf-reader-pro/fr)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Gaeilge                    | Irish                                   | ga     | [![Irish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.4.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                    | [Traducir](https://crowdin.com/project/pdf-reader-pro/ga-IE) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Italiano                   | Italian                                 | it     | [![Italian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.5.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traducir](https://crowdin.com/project/pdf-reader-pro/it)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| 日本語                        | Japanese                                | ja     | [![Japanese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.6.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                 | [Traducir](https://crowdin.com/project/pdf-reader-pro/ja)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Polski                     | Polish                                  | pl     | [![Polish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.7.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traducir](https://crowdin.com/project/pdf-reader-pro/pl)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Português                  | Portuguese                              | pt     | [![Portuguese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.8.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                               | [Traducir](https://crowdin.com/project/pdf-reader-pro/pt-PT) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Русский                    | Russian                                 | ru     | [![Russian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.9.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traducir](https://crowdin.com/project/pdf-reader-pro/ru)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Türkçe                     | Turkish                                 | tr     | [![Turkish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.10.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                 | [Traducir](https://crowdin.com/project/pdf-reader-pro/tr)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| 中文 (简体) | Chinese (Simplified) | zh     | [![Chinese (Simplified) translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.11.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traducir](https://crowdin.com/project/pdf-reader-pro/zh-CN) | [@ahmmedrejowan](https://github.com/ahmmedrejowan), [@Infinifar](https://github.com/Infinifar) |

Las traducciones las hace la comunidad en **[Crowdin](https://crowdin.com/project/pdf-reader-pro)**: inicia sesión, elige tu idioma y traduce en el navegador, sin programar. ¿Falta tu idioma o tienes alguna pregunta? Únete a la [discusión sobre traducciones](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108).

---

## Accesibilidad

PDF Reader Pro funciona con TalkBack y con el tamaño de letra del sistema. La compatibilidad completa con lectores de pantalla llega en la primera versión posterior a la 2.6.0.

| Área                                             | Compatibilidad                                                                                |
| ------------------------------------------------ | --------------------------------------------------------------------------------------------- |
| Lector de pantalla (TalkBack) | ✅ Todos los botones tienen etiqueta; las opciones dicen su estado; títulos y avisos en vivo   |
| Leer PDFs en voz alta                            | ✅ El texto y los enlaces de un PDF (no PDFs escaneados solo de imágenes)   |
| Tamaño de letra                                  | ✅ Sigue el tamaño de letra del sistema                                                        |
| Temas oscuros y de alto contraste                | ✅ Oscuro, Negro (AMOLED) y Sepia                                           |
| Gestos                                           | ⚠️ Reordenar páginas solo se hace arrastrando; el deslizador de páginas ofrece Ir a la página |
| Accesibilidad con interruptores y teclado        | ❔ Aún sin probar                                                                              |

Más detalles en la [página de accesibilidad de la wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Accessibility). ¿Algo difícil de usar? Cuéntanoslo en la [discusión sobre accesibilidad](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/114).

---

## Contribuir

¡Las contribuciones son bienvenidas! Por favor, lee [CONTRIBUTING.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CONTRIBUTING.md) para conocer las directrices.

### Inicio rápido

1. Haz un fork del repositorio
2. Crea tu rama de funcionalidad (`git checkout -b feature/AmazingFeature`)
3. Confirma tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Sube a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## Licencia

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

> **Advertencia**
> Esta es una licencia copyleft. Cualquier trabajo derivado también debe ser de código abierto bajo la misma licencia.

---

## Comunidad

- [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki) – Guías para usar y compilar la app
- [Debates](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) – Haz preguntas, comparte ideas
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) – Informa de errores, solicita funciones
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) – Descarga las últimas versiones

<a href="https://www.producthunt.com/products/pdf-reader-pro-2?utm_source=badge-follow&utm_medium=badge&utm_source=badge-pdf&#0045;reader&#0045;pro&#0045;2" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/follow.svg?product_id=1273411&theme=light" alt="PDF&#0032;Reader&#0032;Pro - Modern&#0032;PDF&#0032;Viewer&#0032;for&#0032;Android | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>

---

## Autor

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [kmrejowan@gmail.com](mailto:kmrejowan@gmail.com)

---

## Agradecimientos

- [PDF.js](https://mozilla.github.io/pdf.js/) – Biblioteca de renderizado de PDF de Mozilla
- [iText](https://itextpdf.com/) – Biblioteca de procesamiento de PDF
- [Jetpack Compose](https://developer.android.com/jetpack/compose) – Kit de herramientas moderno para UI de Android
- [Material Design 3](https://m3.material.io/) – Sistema de diseño
- [Koin](https://insert-koin.io/) – Framework de inyección de dependencias
- [Coil](https://coil-kt.github.io/coil/) – Biblioteca de carga de imágenes

---

## Registro de cambios

### v2.6.0 (23/10/2026)

- Volver a la página: tras un salto, un botón te lleva a la página que dejaste (#88)
- Abre las herramientas PDF desde el lector con el documento abierto (#105)
- Mantener los colores de las imágenes en los temas Oscuro y Negro (#87)
- Ajuste para ocultar archivos y carpetas cuyo nombre empieza por un punto (#6)
- Los documentos cortos se muestran en el centro de la pantalla (#105)
- Corregido: la primera página y los saltos ya no quedan bajo la barra superior (#105)
- Corregido: los archivos firmados ya no desaparecen del selector de archivos de otras apps
- Corregidos los recuentos de un solo elemento («1 página»), y Atrás cierra los paneles laterales en horizontal
- Corregido Reintentar en la pantalla Combinar en todos los idiomas

### v2.5.0 (09/10/2026)

- Firma documentos dibujando, escribiendo o eligiendo una imagen de tu firma, desde el lector o desde Herramientas
- Las firmas se guardan por documento, se gestionan desde un solo panel y se escriben en el archivo o en una copia
- Traducciones al bengalí, ruso, alemán, español, francés, italiano, portugués, turco, polaco, irlandés y japonés (#91, #93, #95, #98)
- Elige el idioma de la app en Ajustes, en cualquier versión de Android
- Traductores como Google Traductor aparecen en el menú de selección de texto (#78)
- Progreso de impresión y guardado de una copia sin contraseña
- Pantalla de fallo con todos los detalles para copiar, compartir o informar en GitHub
- Corregidos los PDF en japonés, chino y coreano que mostraban un error en lugar de abrirse (#89)
- Corregido el cierre de Información del documento en horizontal y el control de páginas que tapaba el botón de menú (#80, #79)
- Bibliotecas principales actualizadas (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0)

### v2.4.0 (15/08/2026)

- Resaltar texto en cinco colores, guardado por documento (#41)
- Panel de resaltados con búsqueda, filtro por color y salto al resaltado (#41)
- Navegación siguiente/anterior entre resaltados (#41)
- La búsqueda dentro del PDF muestra cuántas coincidencias ya están resaltadas (#41)
- Guardar una copia con los resaltados escritos en el PDF como anotaciones reales (#41)
- Los resaltados ya presentes en un archivo se listan y se pueden buscar, solo lectura (#41)
- Bloquear el desplazamiento horizontal al ampliar, guardado por documento (#74)

Consulta [CHANGELOG.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CHANGELOG.md) para ver el historial completo de versiones.

---
