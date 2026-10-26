[English](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/README.md) | [Русский](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.ru.md) | [Deutsch](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.de.md) | [Español](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.es.md) | [Français](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.fr.md) | [Italiano](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.it.md) | [Português](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/readme/README.pt.md)

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/logo.png" alt="Logótipo do PDF Reader Pro" width="120" height="120">

<h3>Visualizador de PDF moderno para Android</h3>

  <p>
    Uma aplicação PDF rica em funcionalidades e focada na privacidade, criada com Jetpack Compose e Material 3. Visualize, edite e faça a gestão de PDFs com 12 ferramentas poderosas.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)
</div>

---

## Funcionalidades

- **Visualização de PDF** – Renderização fluida com zoom, deslocamento e navegação por páginas
- **12 ferramentas de PDF** – Combinar, dividir, comprimir, rodar, reordenar, remover páginas, adicionar números de página, marca de água, bloquear/desbloquear, PDF para imagens, imagens para PDF
- **Marcadores** – Guarde e faça a gestão de marcadores nos documentos
- **Índice** – Navegue através da estrutura do documento
- **Pesquisa** – Pesquisa de texto completo nos documentos
- **Deslocamento automático** – Leitura sem mãos com velocidade ajustável
- **Gestão de ficheiros** – Explore, pesquise, ordene e organize PDFs
- **Recentes e favoritos** – Acesso rápido aos seus ficheiros
- **Design Material 3** – Interface moderna com modo escuro e cores dinâmicas
- **100 % offline** – Privacidade total, sem anúncios, sem rastreamento

---

## Transferir

<p align="center">
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Get it on GitHub" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/get.png" height="72"/></a>&nbsp;&nbsp;
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/ahmmedrejowan/PdfReaderPro"><img alt="Get it on Obtainium" src="https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/obtainium.png" height="72"/></a>
</p>

<p align="center">
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/ahmmedrejowan/PdfReaderPro"/></a>
<a href="https://github.com/ahmmedrejowan/PdfReaderPro/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/ahmmedrejowan/PdfReaderPro/total"/></a>
<img alt="Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&amp;logoColor=white"/>
</p>

Transfira o APK do GitHub ou adicione a app ao Obtainium para receber atualizações automaticamente. A app também pode procurar atualizações sozinha.

---

## Capturas de ecrã

| | | | |
|---|---|---|---|
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot1.webp) | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot6.webp) | ![Screenshot 9](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot9.webp) | ![Screenshot 7](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot7.webp) |
| ![Screenshot 14](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot14.webp) | ![Screenshot 15](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot15.webp) | ![Screenshot 17](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot17.webp) | ![Screenshot 19](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot19.webp) |
| ![Screenshot 22](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot22.webp) | ![Screenshot 25](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot25.webp) | ![Screenshot 26](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot26.webp) | ![Screenshot 27](https://raw.githubusercontent.com/ahmmedrejowan/PdfReaderPro/master/files/shot27.webp) |

Há mais capturas de todos os ecrãs na [wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Screenshots).

---

## Arquitetura

O PDF Reader Pro segue os princípios da **Clean Architecture** com o padrão **MVVM**:

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
- **Linguagem**: Kotlin (100 %)
- **Arquitetura**: MVVM + Clean Architecture
- **Injeção de dependências**: Koin
- **Base de dados**: Room
- **Assíncrono**: Kotlin Coroutines + StateFlow
- **Navegação**: Jetpack Navigation Compose
- **Renderização de PDF**: WebView personalizada com PDF.js
- **Processamento de PDF**: iText 9
- **Carregamento de imagens**: Coil
- **Armazenamento de dados**: DataStore Preferences

---

## Requisitos

- **SDK mínimo**: API 24 (Android 7.0 Nougat)
- **SDK de destino**: API 37
- **Compile SDK**: API 37
- **Gradle**: 9.8.0
- **AGP**: 9.4.1
- **Kotlin**: 2.4.20
- **Java**: 21

### Permissões

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_DOCUMENTS` – Acesso a ficheiros PDF
- `WRITE_EXTERNAL_STORAGE` – Guardar PDFs processados (Android 9 e anteriores)
- `INTERNET` – Opcional, apenas para verificações de atualizações na aplicação

**Nota:** Esta aplicação não recolhe nem transmite quaisquer dados do utilizador.

---

## Compilar e executar

Para compilar e executar o projeto, siga estes passos:

1. Clone o repositório:
   ```bash
   git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
   ```
2. Abra o projeto no Android Studio.
3. Sincronize o projeto com os ficheiros Gradle.
4. Ligue o seu dispositivo Android ou inicie um emulador.
5. Clique no botão «Run» no Android Studio para compilar e executar a aplicação.

---

## Testes

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## Traduções

[![Crowdin](https://badges.crowdin.net/pdf-reader-pro/localized.svg)](https://crowdin.com/project/pdf-reader-pro)

O PDF Reader Pro está disponível em 13 idiomas:

<!-- Each badge reads progress.N from https://badges.awesome-crowdin.com/stats-17953219-941021.json, where N is the language's position in that file; check the indexes there when adding a language. -->
| Idioma | Nome em inglês | Código | Progresso | Crowdin | Contribuidores |
|---|---|---|---|---|---|
| English | English | en | Idioma de origem | [Traduzir](https://crowdin.com/project/pdf-reader-pro) | [@ahmmedrejowan](https://github.com/ahmmedrejowan) |
| বাংলা | Bengali | bn | [![Bengali translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.0.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/bn) | [@ahmmedrejowan](https://github.com/ahmmedrejowan) |
| Deutsch | German | de | [![German translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.1.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/de) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Español | Spanish | es | [![Spanish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.2.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/es-ES) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Français | French | fr | [![French translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.3.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/fr) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Gaeilge | Irish | ga | [![Irish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.4.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/ga-IE) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Italiano | Italian | it | [![Italian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.5.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/it) | [@4kvoodoo](https://github.com/4kvoodoo) |
| 日本語 | Japanese | ja | [![Japanese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.6.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/ja) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Polski | Polish | pl | [![Polish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.7.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/pl) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Português | Portuguese | pt | [![Portuguese translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.8.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/pt-PT) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Русский | Russian | ru | [![Russian translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.9.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/ru) | [@4kvoodoo](https://github.com/4kvoodoo) |
| Türkçe | Turkish | tr | [![Turkish translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.10.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/tr) | [@4kvoodoo](https://github.com/4kvoodoo) |
| 中文 (简体) | Chinese (Simplified) | zh | [![Chinese (Simplified) translation progress](https://img.shields.io/badge/dynamic/json?color=blue&label=&logo=crowdin&query=%24.progress.11.data.translationProgress&url=https%3A%2F%2Fbadges.awesome-crowdin.com%2Fstats-17953219-941021.json)](https://crowdin.com/project/pdf-reader-pro) | [Traduzir](https://crowdin.com/project/pdf-reader-pro/zh-CN) | [@ahmmedrejowan](https://github.com/ahmmedrejowan), [@Infinifar](https://github.com/Infinifar) |

As traduções são feitas pela comunidade no **[Crowdin](https://crowdin.com/project/pdf-reader-pro)**: inicie sessão, escolha o seu idioma e traduza no navegador, sem programar. Falta o seu idioma ou tem alguma dúvida? Participe na [discussão sobre traduções](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108).

---

## Acessibilidade

O PDF Reader Pro funciona com o TalkBack e com o tamanho de letra do sistema. O suporte completo a leitores de ecrã chega na primeira versão depois da 2.6.0.

| Área | Suporte |
|---|---|
| Leitor de ecrã (TalkBack) | ✅ Todos os botões têm etiqueta; as opções dizem o seu estado; títulos e avisos em direto |
| Ler PDFs em voz alta | ✅ O texto e as ligações de um PDF (não PDFs digitalizados só com imagens) |
| Tamanho de letra | ✅ Segue o tamanho de letra do sistema |
| Temas escuros e de alto contraste | ✅ Escuro, Preto (AMOLED) e Sépia |
| Gestos | ⚠️ Reordenar páginas só funciona a arrastar; o seletor de páginas oferece Ir para a página |
| Acesso por comutador e teclado | ❔ Ainda não testado |

Detalhes na [página de acessibilidade da wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki/Accessibility). Algo difícil de usar? Diga-nos na [discussão sobre acessibilidade](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/114).

---

## Contribuir

As contribuições são bem-vindas! Por favor, leia [CONTRIBUTING.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CONTRIBUTING.md) para conhecer as diretrizes.

### Início rápido

1. Faça um fork do repositório
2. Crie o seu ramo de funcionalidade (`git checkout -b feature/AmazingFeature`)
3. Confirme as suas alterações (`git commit -m 'Add some AmazingFeature'`)
4. Envie para o ramo (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

---

## Licença

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

> **Aviso**
> Esta é uma licença copyleft. Qualquer trabalho derivado também deve ser de código aberto sob a mesma licença.

---

## Comunidade

- [Wiki](https://github.com/ahmmedrejowan/PdfReaderPro/wiki) – Guias para usar e compilar a aplicação
- [Discussões](https://github.com/ahmmedrejowan/PdfReaderPro/discussions) – Faça perguntas, partilhe ideias
- [Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) – Comunique erros, solicite funcionalidades
- [Releases](https://github.com/ahmmedrejowan/PdfReaderPro/releases) – Transfira as versões mais recentes

<a href="https://www.producthunt.com/products/pdf-reader-pro-2?utm_source=badge-follow&utm_medium=badge&utm_source=badge-pdf&#0045;reader&#0045;pro&#0045;2" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/follow.svg?product_id=1273411&theme=light" alt="PDF&#0032;Reader&#0032;Pro - Modern&#0032;PDF&#0032;Viewer&#0032;for&#0032;Android | Product Hunt" style="width: 250px; height: 54px;" width="250" height="54" /></a>

---

## Autor

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [kmrejowan@gmail.com](mailto:kmrejowan@gmail.com)

---

## Agradecimentos

- [PDF.js](https://mozilla.github.io/pdf.js/) – Biblioteca de renderização de PDF da Mozilla
- [iText](https://itextpdf.com/) – Biblioteca de processamento de PDF
- [Jetpack Compose](https://developer.android.com/jetpack/compose) – Kit de ferramentas moderno para UI Android
- [Material Design 3](https://m3.material.io/) – Sistema de design
- [Koin](https://insert-koin.io/) – Framework de injeção de dependências
- [Coil](https://coil-kt.github.io/coil/) – Biblioteca de carregamento de imagens

---

## Registo de alterações

### v2.6.0 (23/10/2026)

- Voltar à página: depois de um salto, um botão leva de volta à página anterior (#88)
- Abrir as ferramentas PDF a partir do leitor no documento aberto (#105)
- Manter as cores das imagens nos temas Escuro e Preto (#87)
- Definição para ocultar ficheiros e pastas cujo nome começa por um ponto (#6)
- Os documentos curtos aparecem no centro do ecrã (#105)
- Corrigido: a primeira página e os saltos já não ficam sob a barra superior (#105)
- Corrigido: os ficheiros assinados já não desaparecem do seletor de ficheiros de outras aplicações
- Corrigidas as contagens de um só item («1 página»), e Voltar fecha os painéis laterais na horizontal
- Corrigido Tentar novamente no ecrã Unir em todos os idiomas

### v2.5.0 (09/10/2026)

- Assine documentos desenhando, escrevendo ou escolhendo uma imagem da sua assinatura, no leitor ou nas Ferramentas
- As assinaturas ficam guardadas por documento, são geridas num único painel e escritas no ficheiro ou numa cópia
- Traduções para bengali, russo, alemão, espanhol, francês, italiano, português, turco, polaco, irlandês e japonês (#91, #93, #95, #98)
- Escolha o idioma da app nas Definições, em qualquer versão do Android
- Tradutores como o Google Tradutor aparecem no menu de seleção de texto (#78)
- Progresso de impressão e guardar uma cópia sem palavra-passe
- Ecrã de falha com todos os detalhes para copiar, partilhar ou reportar no GitHub
- Corrigidos PDFs em japonês, chinês e coreano que mostravam um erro em vez de abrir (#89)
- Corrigidos o fecho de Informações do documento na horizontal e o controlo de páginas que tapava o botão de menu (#80, #79)
- Bibliotecas principais atualizadas (Kotlin 2.4.20, AGP 9.4.1, Gradle 9.8.0)

### v2.4.0 (15/08/2026)

- Realçar texto em cinco cores, guardado por documento (#41)
- Painel de realces com pesquisa, filtro por cor e salto para o realce (#41)
- Navegação seguinte/anterior entre realces (#41)
- A pesquisa dentro do PDF mostra quantas correspondências já estão realçadas (#41)
- Guardar uma cópia com os realces escritos no PDF como anotações reais (#41)
- Os realces já presentes num ficheiro são listados e pesquisáveis, apenas de leitura (#41)
- Bloquear o deslocamento horizontal ao ampliar, guardado por documento (#74)

Consulte [CHANGELOG.md](https://github.com/ahmmedrejowan/PdfReaderPro/blob/master/CHANGELOG.md) para o histórico completo de versões.

---
