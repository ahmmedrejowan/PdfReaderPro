[English](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/README.md) | [Русский](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.ru.md) | [Deutsch](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.de.md) | [Español](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.es.md) | [Français](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.fr.md) | [Italiano](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.it.md) | [Português](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.pt.md)

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="PDF Reader Pro" width="120" height="120">

<h3>Visualizzatore PDF moderno per Android</h3>

  <p>
    Un\'app PDF ricca di funzionalità e incentrata sulla privacy, creata con Jetpack Compose e Material 3. Visualizza, modifica e gestisci i PDF con 12 potenti strumenti.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)

</div>

---

## Funzionalità

- **Visualizzazione PDF** – Rendering fluido con zoom, scorrimento e navigazione tra le pagine
- **12 strumenti PDF** – Unisci, dividi, comprimi, ruota, riordina, rimuovi pagine, aggiungi numeri di pagina, filigrana, blocca/sblocca, PDF in immagini, immagini in PDF
- **Segnalibri** – Salva e gestisci i segnalibri all\'interno dei documenti
- **Indice** – Naviga usando la struttura del documento
- **Ricerca** – Ricerca full-text all\'interno dei documenti
- **Scorrimento automatico** – Lettura a mani libere con velocità regolabile
- **Gestione file** – Sfoglia, cerca, ordina e organizza i PDF
- **Recenti e preferiti** – Accesso rapido ai tuoi file
- **Design Material 3** – Interfaccia moderna con modalità scura e colori dinamici
- **100% offline** – Privacy totale, nessuna pubblicità, nessun tracciamento

---

## Download

<p align="center"><a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Get it on GitHub" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" height="72"/></a>&nbsp;&nbsp;
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/ahmmedrejowan/PdfReaderPro"><img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/obtainium.png" height="72"/></a>
</p>

<p align="center"><a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro"/></a>
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/ahmmedrejowan/PdfReaderPro/total"/></a>
<img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&amp;logoColor=white"/>
</p>

Scarica l'APK da GitHub oppure aggiungi l'app a Obtainium per ricevere gli aggiornamenti automaticamente. L'app può anche cercare gli aggiornamenti da sola.

---

## Screenshot

|                                                                                                         |                                                                                                         |                                                                                                         |                                                                                                         |
| ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp)   | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp)   | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp)   | ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp)   |
| ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

Altri screenshot di ogni schermata sono nella [wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Screenshots).

---

## Architettura

PDF Reader Pro segue i principi della **Clean Architecture** con il pattern **MVVM**:

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

### Stack tecnologico

- **Framework UI**: Jetpack Compose (100% Compose UI)
- **Linguaggio**: Kotlin (100%)
- **Architettura**: MVVM + Clean Architecture
- **Iniezione delle dipendenze**: Koin
- **Database**: Room
- **Asincrono**: Kotlin Coroutines + StateFlow
- **Navigazione**: Jetpack Navigation Compose
- **Rendering PDF**: WebView personalizzata basata su PDF.js
- **Elaborazione PDF**: iText 9
- **Caricamento immagini**: Coil
- **Archiviazione dati**: DataStore Preferences

---

## Requisiti

- **SDK minimo**: API 24 (Android 7.0 Nougat)
- **SDK di destinazione**: API 37
- **Compile SDK**: API 37
- **Gradle**: 9.8.0
- **AGP**: 9.4.1
- **Kotlin**: 2.4.20
- **Java**: 21

### Autorizzazioni

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_DOCUMENTS` – Accesso ai file PDF
- `WRITE_EXTERNAL_STORAGE` – Salvataggio dei PDF elaborati (Android 9 e precedenti)
- `INTERNET` – Opzionale, solo per i controlli di aggiornamento nell\'app

**Nota:** Questa app non raccoglie né trasmette alcun dato dell\'utente.

---

## Compilazione ed esecuzione

Per compilare ed eseguire il progetto, segui questi passaggi:

1. Clona il repository:
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   ```
2. Apri il progetto in Android Studio.
3. Sincronizza il progetto con i file Gradle.
4. Collega il tuo dispositivo Android o avvia un emulatore.
5. Fai clic sul pulsante «Run» in Android Studio per compilare ed eseguire l\'app.

---

## Test

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## Traduzioni

[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)

PDF Reader Pro è disponibile in 13 lingue:

<!-- Each badge reads progress.N from https://badges.awesome-crowdin.com/stats-17953219-941021.json, where N is the language's position in that file; check the indexes there when adding a language. -->

| Lingua                     | Nome in inglese                         | Codice | Avanzamento                                                                                                                                                                                                                                                                                                                              | Crowdin                                                     | Contributori                                                                                                             |
| -------------------------- | --------------------------------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| English                    | English                                 | en     | Lingua di origine                                                                                                                                                                                                                                                                                                                        | [Traduci](https://crowdin.com/project/pdf-reader-pro)       | [@ahmmedrejowan](https://github.com/ahmmedrejowan)                                                          |
| বাংলা                      | Bengali                                 | bn     | [![Bengali translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.0.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduci](https://crowdin.com/project/pdf-reader-pro/bn)    | [@ahmmedrejowan](https://github.com/ahmmedrejowan)                                                          |
| Deutsch                    | German                                  | de     | [![German translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.1.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traduci](https://crowdin.com/project/pdf-reader-pro/de)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Español                    | Spanish                                 | es     | [![Spanish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.2.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduci](https://crowdin.com/project/pdf-reader-pro/es-ES) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Français                   | French                                  | fr     | [![French translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.3.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traduci](https://crowdin.com/project/pdf-reader-pro/fr)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Gaeilge                    | Irish                                   | ga     | [![Irish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.4.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                    | [Traduci](https://crowdin.com/project/pdf-reader-pro/ga-IE) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Italiano                   | Italian                                 | it     | [![Italian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.5.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduci](https://crowdin.com/project/pdf-reader-pro/it)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| 日本語                        | Japanese                                | ja     | [![Japanese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.6.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                 | [Traduci](https://crowdin.com/project/pdf-reader-pro/ja)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Polski                     | Polish                                  | pl     | [![Polish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.7.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traduci](https://crowdin.com/project/pdf-reader-pro/pl)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Português                  | Portuguese                              | pt     | [![Portuguese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.8.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                               | [Traduci](https://crowdin.com/project/pdf-reader-pro/pt-PT) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Русский                    | Russian                                 | ru     | [![Russian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.9.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduci](https://crowdin.com/project/pdf-reader-pro/ru)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Türkçe                     | Turkish                                 | tr     | [![Turkish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.10.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                 | [Traduci](https://crowdin.com/project/pdf-reader-pro/tr)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| 中文 (简体) | Chinese (Simplified) | zh     | [![Chinese (Simplified) translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.11.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduci](https://crowdin.com/project/pdf-reader-pro/zh-CN) | [@ahmmedrejowan](https://github.com/ahmmedrejowan), [@Infinifar](https://github.com/Infinifar) |

Le traduzioni sono realizzate dalla community su **[Crowdin](https://crowdin.com/project/pdf-reader-pro)**: accedi, scegli la tua lingua e traduci nel browser, senza programmare. Manca la tua lingua o hai una domanda? Partecipa alla [discussione sulle traduzioni](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108).

---

## Accessibilità

PDF Reader Pro funziona con TalkBack e con la dimensione del carattere di sistema. Il supporto completo agli screen reader arriva nella prima versione dopo la 2.6.0.

| Area                                        | Supporto                                                                                                |
| ------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| Screen reader (TalkBack) | ✅ Tutti i pulsanti hanno un'etichetta; le opzioni dicono il loro stato; titoli e annunci in tempo reale |
| Lettura ad alta voce dei PDF                | ✅ Il testo e i link di un PDF (non i PDF scansionati fatti solo di immagini)         |
| Dimensione del carattere                    | ✅ Segue la dimensione del carattere di sistema                                                          |
| Temi scuri e ad alto contrasto              | ✅ Scuro, Nero (AMOLED) e Seppia                                                      |
| Gesti                                       | ⚠️ Riordinare le pagine si fa solo trascinando; il cursore delle pagine offre Vai alla pagina           |
| Accesso con interruttori e tastiera         | ❔ Non ancora testato                                                                                    |

Dettagli nella [pagina Accessibilità della wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Accessibility). Qualcosa è difficile da usare? Diccelo nella [discussione sull'accessibilità](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/114).

---

## Contribuire

I contributi sono benvenuti! Leggi [CONTRIBUTING.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CONTRIBUTING.md) per le linee guida.

### Avvio rapido

1. Fai un fork del repository
2. Crea il tuo branch di funzionalità (`git checkout -b feature/AmazingFeature`)
3. Esegui il commit delle modifiche (`git commit -m 'Add some AmazingFeature'`)
4. Esegui il push sul branch (`git push origin feature/AmazingFeature`)
5. Apri una Pull Request

---

## Licenza

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

> **Avviso**
> Questa è una licenza copyleft. Qualsiasi opera derivata deve essere anch\'essa open source sotto la stessa licenza.

---

## Community

- [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki) – Guide per usare e compilare l'app
- [Discussioni](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) – Fai domande, condividi idee
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) – Segnala bug, richiedi funzionalità
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) – Scarica le versioni più recenti

<a href="https://www.producthunt.com/products/pdf-reader-pro-2?utm_source=badge-follow&utm_medium=badge&utm_source=badge-pdf&#0045;reader&#0045;pro&#0045;2" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/follow.svg?product_id=1273411&theme=light" alt="PDF&#0032;Reader&#0032;Pro - Modern&#0032;PDF&#0032;Viewer&#0032;for&#0032;Android | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>

---

## Autore

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [kmrejowan@gmail.com](mailto:kmrejowan@gmail.com)

---

## Ringraziamenti

- [PDF.js](https://mozilla.github.io/pdf.js/) – Libreria di rendering PDF di Mozilla
- [iText](https://itextpdf.com/) – Libreria di elaborazione PDF
- [Jetpack Compose](https://developer.android.com/jetpack/compose) – Toolkit moderno per l\'interfaccia Android
- [Material Design 3](https://m3.material.io/) – Sistema di design
- [Koin](https://insert-koin.io/) – Framework per l\'iniezione delle dipendenze
- [Coil](https://coil-kt.github.io/coil/) – Libreria per il caricamento delle immagini

---

## Registro delle modifiche

### v2.6.0 (23/10/2026)

- Torna alla pagina: dopo un salto, un chip riporta alla pagina precedente (#88)
- Apri gli strumenti PDF dal lettore sul documento aperto (#105)
- Mantieni i colori delle immagini nei temi Scuro e Nero (#87)
- Impostazione per nascondere file e cartelle il cui nome inizia con un punto (#6)
- I documenti brevi sono al centro dello schermo (#105)
- Corretto: la prima pagina e i salti non finiscono più sotto la barra superiore (#105)
- Corretto: i file firmati non spariscono più dal selettore di file delle altre app
- Corretti i conteggi per un solo elemento («1 pagina»), e Indietro chiude i pannelli laterali in orizzontale
- Corretto Riprova nella schermata Unisci in tutte le lingue

### v2.5.0 (09/10/2026)

- Firma i documenti disegnando, digitando o scegliendo un'immagine della tua firma, dal lettore o da Strumenti
- Le firme sono salvate per documento, gestite da un unico pannello e scritte nel file o in una copia
- Traduzioni in bengalese, russo, tedesco, spagnolo, francese, italiano, portoghese, turco, polacco, irlandese e giapponese (#91, #93, #95, #98)
- Scegli la lingua dell'app nelle Impostazioni, su qualsiasi versione di Android
- I traduttori come Google Traduttore compaiono nel menu di selezione del testo (#78)
- Avanzamento della stampa e salvataggio di una copia senza password
- Schermata di arresto anomalo con tutti i dettagli da copiare, condividere o segnalare su GitHub
- Corretti i PDF in giapponese, cinese e coreano che mostravano un errore invece di aprirsi (#89)
- Corretti l'arresto di Info documento in orizzontale e il cursore delle pagine che copriva il pulsante del menu (#80, #79)
- Librerie principali aggiornate (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0)

### v2.4.0 (15/08/2026)

- Evidenzia il testo in cinque colori, salvato per documento (#41)
- Pannello delle evidenziazioni con ricerca, filtro per colore e salto all\'evidenziazione (#41)
- Navigazione successiva/precedente tra le evidenziazioni (#41)
- La ricerca all\'interno del PDF mostra quante corrispondenze sono già evidenziate (#41)
- Salva una copia con le evidenziazioni scritte nel PDF come annotazioni reali (#41)
- Le evidenziazioni già presenti in un file vengono elencate e sono ricercabili, in sola lettura (#41)
- Blocca lo scorrimento orizzontale quando si è ingranditi, salvato per documento (#74)

Consulta [CHANGELOG.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CHANGELOG.md) per la cronologia completa delle versioni.

---
