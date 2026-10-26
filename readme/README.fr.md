[English](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/README.md) | [Русский](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.ru.md) | [Deutsch](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.de.md) | [Español](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.es.md) | [Français](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.fr.md) | [Italiano](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.it.md) | [Português](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.pt.md)

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="PDF Reader Pro Logo" width="120" height="120">

<h3>Visionneuse PDF moderne pour Android</h3>

  <p>
    Une application PDF riche en fonctionnalités et axée sur la confidentialité, conçue avec Jetpack Compose et Material 3. Consultez, modifiez et gérez vos PDF avec 12 outils puissants.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)

</div>

---

## Fonctionnalités

- **Affichage PDF** – Rendu fluide avec zoom, défilement et navigation par pages
- **12 outils PDF** – Fusionner, diviser, compresser, pivoter, réorganiser, supprimer des pages, ajouter des numéros de page, filigrane, verrouiller/déverrouiller, PDF en images, images en PDF
- **Signets** – Enregistrez et gérez des signets dans les documents
- **Table des matières** – Naviguez grâce à la structure du document
- **Recherche** – Recherche en texte intégral dans les documents
- **Défilement automatique** – Lecture mains libres avec vitesse réglable
- **Gestion des fichiers** – Parcourez, recherchez, triez et organisez vos PDF
- **Récents et favoris** – Accès rapide à vos fichiers
- **Design Material 3** – Interface moderne avec mode sombre et couleurs dynamiques
- **100 % hors ligne** – Confidentialité totale, sans publicité, sans suivi

---

## Téléchargement

<p align="center"><a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Get it on GitHub" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" height="72"/></a>&nbsp;&nbsp;
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/ahmmedrejowan/PdfReaderPro"><img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/obtainium.png" height="72"/></a>
</p>

<p align="center"><a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro"/></a>
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/ahmmedrejowan/PdfReaderPro/total"/></a>
<img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&amp;logoColor=white"/>
</p>

Téléchargez l'APK depuis GitHub, ou ajoutez l'app à Obtainium pour recevoir les mises à jour automatiquement. L'app peut aussi vérifier elle-même les mises à jour.

---

## Captures d'écran

|                                                                                                         |                                                                                                         |                                                                                                         |                                                                                                         |
| ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp)   | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp)   | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp)   | ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp)   |
| ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

D'autres captures de chaque écran sont dans le [wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Screenshots).

---

## Architecture

PDF Reader Pro suit les principes de **Clean Architecture** avec le modèle **MVVM** :

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

### Stack technique

- **Framework UI** : Jetpack Compose (100 % Compose UI)
- **Langage** : Kotlin (100 %)
- **Architecture** : MVVM + Clean Architecture
- **Injection de dépendances** : Koin
- **Base de données** : Room
- **Asynchrone** : Kotlin Coroutines + StateFlow
- **Navigation** : Jetpack Navigation Compose
- **Rendu PDF** : WebView personnalisée basée sur PDF.js
- **Traitement PDF** : iText 9
- **Chargement d'images** : Coil
- **Stockage des données** : DataStore Preferences

---

## Prérequis

- **SDK minimum** : API 24 (Android 7.0 Nougat)
- **SDK cible** : API 37
- **Compile SDK** : API 37
- **Gradle** : 9.8.0
- **AGP** : 9.4.1
- **Kotlin** : 2.4.20
- **Java** : 21

### Autorisations

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_DOCUMENTS` – Accès aux fichiers PDF
- `WRITE_EXTERNAL_STORAGE` – Enregistrer les PDF traités (Android 9 et antérieurs)
- `INTERNET` – Facultatif, uniquement pour les vérifications de mise à jour dans l'application

**Remarque :** Cette application ne collecte ni ne transmet aucune donnée utilisateur.

---

## Compilation et exécution

Pour compiler et exécuter le projet, suivez ces étapes :

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   ```
2. Ouvrez le projet dans Android Studio.
3. Synchronisez le projet avec les fichiers Gradle.
4. Connectez votre appareil Android ou démarrez un émulateur.
5. Cliquez sur le bouton « Run » dans Android Studio pour compiler et exécuter l'application.

---

## Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## Traductions

[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)

PDF Reader Pro est disponible en 13 langues :

<!-- Each badge reads progress.N from https://badges.awesome-crowdin.com/stats-17953219-941021.json, where N is the language's position in that file; check the indexes there when adding a language. -->

| Langue                     | Nom en anglais                          | Code | Progression                                                                                                                                                                                                                                                                                                                              | Crowdin                                                      | Contributeurs                                                                                                            |
| -------------------------- | --------------------------------------- | ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------ |
| English                    | English                                 | en   | Langue source                                                                                                                                                                                                                                                                                                                            | [Traduire](https://crowdin.com/project/pdf-reader-pro)       | [@ahmmedrejowan](https://github.com/ahmmedrejowan)                                                          |
| বাংলা                      | Bengali                                 | bn   | [![Bengali translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.0.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduire](https://crowdin.com/project/pdf-reader-pro/bn)    | [@ahmmedrejowan](https://github.com/ahmmedrejowan)                                                          |
| Deutsch                    | German                                  | de   | [![German translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.1.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traduire](https://crowdin.com/project/pdf-reader-pro/de)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Español                    | Spanish                                 | es   | [![Spanish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.2.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduire](https://crowdin.com/project/pdf-reader-pro/es-ES) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Français                   | French                                  | fr   | [![French translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.3.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traduire](https://crowdin.com/project/pdf-reader-pro/fr)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Gaeilge                    | Irish                                   | ga   | [![Irish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.4.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                    | [Traduire](https://crowdin.com/project/pdf-reader-pro/ga-IE) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Italiano                   | Italian                                 | it   | [![Italian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.5.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduire](https://crowdin.com/project/pdf-reader-pro/it)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| 日本語                        | Japanese                                | ja   | [![Japanese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.6.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                 | [Traduire](https://crowdin.com/project/pdf-reader-pro/ja)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Polski                     | Polish                                  | pl   | [![Polish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.7.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                   | [Traduire](https://crowdin.com/project/pdf-reader-pro/pl)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Português                  | Portuguese                              | pt   | [![Portuguese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.8.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                               | [Traduire](https://crowdin.com/project/pdf-reader-pro/pt-PT) | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Русский                    | Russian                                 | ru   | [![Russian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.9.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                  | [Traduire](https://crowdin.com/project/pdf-reader-pro/ru)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| Türkçe                     | Turkish                                 | tr   | [![Turkish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.10.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro)                                 | [Traduire](https://crowdin.com/project/pdf-reader-pro/tr)    | [@4kvoodoo](https://github.com/4kvoodoo)                                                                    |
| 中文 (简体) | Chinese (Simplified) | zh   | [![Chinese (Simplified) translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.11.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduire](https://crowdin.com/project/pdf-reader-pro/zh-CN) | [@ahmmedrejowan](https://github.com/ahmmedrejowan), [@Infinifar](https://github.com/Infinifar) |

Les traductions sont réalisées par la communauté sur **[Crowdin](https://crowdin.com/project/pdf-reader-pro)** : connectez-vous, choisissez votre langue et traduisez dans le navigateur, sans coder. Votre langue manque ou vous avez une question ? Rejoignez la [discussion sur les traductions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108).

---

## Accessibilité

PDF Reader Pro fonctionne avec TalkBack et la taille de police du système. La prise en charge complète des lecteurs d'écran arrive dans la première version après la 2.6.0.

| Domaine                                       | Support                                                                                               |
| --------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Lecteur d'écran (TalkBack) | ✅ Tous les boutons ont un libellé ; les options annoncent leur état ; titres et annonces en direct    |
| Lecture des PDF à voix haute                  | ✅ Le texte et les liens d'un PDF (pas les PDF numérisés composés d'images)         |
| Taille de police                              | ✅ Suit la taille de police du système                                                                 |
| Thèmes sombres et à fort contraste            | ✅ Sombre, Noir (AMOLED) et Sépia                                                   |
| Gestures                                      | ⚠️ Réorganiser les pages se fait uniquement par glisser ; le curseur de pages propose Aller à la page |
| Switch Access et clavier                      | ❔ Pas encore testé                                                                                    |

Détails sur la [page Accessibilité du wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Accessibility). Quelque chose de difficile à utiliser ? Dites-le-nous dans la [discussion sur l'accessibilité](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/114).

---

## Contribution

Les contributions sont les bienvenues ! Veuillez lire [CONTRIBUTING.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CONTRIBUTING.md) pour connaître les directives.

### Démarrage rapide

1. Forkez le dépôt
2. Créez votre branche de fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Validez vos modifications (`git commit -m 'Add some AmazingFeature'`)
4. Poussez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

---

## Licence

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

> **Avertissement**
> Il s'agit d'une licence copyleft. Toute œuvre dérivée doit également être open source sous la même licence.

---

## Communauté

- [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki) – Guides pour utiliser et compiler l'application
- [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) – Posez des questions, partagez des idées
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) – Signalez des bugs, demandez des fonctionnalités
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) – Téléchargez les dernières versions

<a href="https://www.producthunt.com/products/pdf-reader-pro-2?utm_source=badge-follow&utm_medium=badge&utm_source=badge-pdf&#0045;reader&#0045;pro&#0045;2" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/follow.svg?product_id=1273411&theme=light" alt="PDF&#0032;Reader&#0032;Pro - Modern&#0032;PDF&#0032;Viewer&#0032;for&#0032;Android | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>

---

## Auteur

**K M Rejowan Ahmmed**

- GitHub : [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email : [kmrejowan@gmail.com](mailto:kmrejowan@gmail.com)

---

## Remerciements

- [PDF.js](https://mozilla.github.io/pdf.js/) – Bibliothèque de rendu PDF de Mozilla
- [iText](https://itextpdf.com/) – Bibliothèque de traitement PDF
- [Jetpack Compose](https://developer.android.com/jetpack/compose) – Boîte à outils moderne pour l'interface Android
- [Material Design 3](https://m3.material.io/) – Système de design
- [Koin](https://insert-koin.io/) – Framework d'injection de dépendances
- [Coil](https://coil-kt.github.io/coil/) – Bibliothèque de chargement d'images

---

## Journal des modifications

### v2.6.0 (23/10/2026)

- Retour à la page : après un saut, une puce ramène à la page quittée (#88)
- Ouvrir les outils PDF depuis le lecteur sur le document ouvert (#105)
- Conserver les couleurs des images avec les thèmes Sombre et Noir (#87)
- Option pour masquer les fichiers et dossiers dont le nom commence par un point (#6)
- Les documents courts s'affichent au centre de l'écran (#105)
- Corrigé : la première page et les sauts ne passent plus sous la barre du haut (#105)
- Corrigé : les fichiers signés ne disparaissent plus du sélecteur de fichiers des autres applis
- Corrigé : les décomptes d'un seul élément (« 1 page »), et Retour ferme les panneaux latéraux en paysage
- Corrigé : Réessayer sur l'écran Fusionner dans toutes les langues

### v2.5.0 (09/10/2026)

- Signez vos documents en dessinant, en tapant ou en choisissant une image de votre signature, depuis le lecteur ou les Outils
- Les signatures sont conservées par document, gérées depuis un seul panneau et écrites dans le fichier ou dans une copie
- Traductions en bengali, russe, allemand, espagnol, français, italien, portugais, turc, polonais, irlandais et japonais (#91, #93, #95, #98)
- Choisissez la langue de l'application dans les Paramètres, sur toutes les versions d'Android
- Les traducteurs comme Google Traduction apparaissent dans le menu de sélection de texte (#78)
- Progression de l'impression et enregistrement d'une copie sans mot de passe
- Écran de plantage avec tous les détails à copier, partager ou signaler sur GitHub
- Correction des PDF en japonais, chinois et coréen qui affichaient une erreur au lieu de s'ouvrir (#89)
- Correction du plantage des Infos du document en paysage et du curseur de pages qui masquait le bouton de menu (#80, #79)
- Bibliothèques principales mises à jour (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0)

### v2.4.0 (15/08/2026)

- Surligner du texte en cinq couleurs, enregistré par document (#41)
- Panneau des surlignages avec recherche, filtre par couleur et saut vers le surlignage (#41)
- Navigation précédent/suivant entre les surlignages (#41)
- La recherche dans le PDF indique combien de correspondances sont déjà surlignées (#41)
- Enregistrer une copie avec les surlignages écrits dans le PDF comme de véritables annotations (#41)
- Les surlignages déjà présents dans un fichier sont listés et consultables, en lecture seule (#41)
- Verrouiller le défilement horizontal en zoom, enregistré par document (#74)

Consultez [CHANGELOG.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CHANGELOG.md) pour l'historique complet des versions.

---
