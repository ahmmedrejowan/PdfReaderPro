# Contributing to PDF Reader Pro

Hey there! Thanks for wanting to contribute to PDF Reader Pro. Whether it's a bug fix, new feature, or just a typo correction - every contribution helps make this app better for everyone.

## Ways to Contribute

### Found a Bug?

1. Search [existing issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues) first - maybe it's already reported
2. If not, open a new issue using the Bug Report template
3. The more details you provide, the easier it is to fix!

### Have an Idea?

We love hearing new ideas! Share them in [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/ideas) or open a Feature Request issue.

### Want to Code?

Awesome! Here's how:

1. Fork the repo
2. Create a branch: `git checkout -b feature/your-feature`
3. Make your changes
4. Test it works
5. Open a Pull Request

Don't worry about getting everything perfect - we can work through it together in the PR.

### Want to Translate?

Translations are managed on **[Crowdin](https://crowdin.com/project/pdf-reader-pro)**. Sign in, pick your language and translate in the browser; no Git or Android Studio needed. Crowdin collects the work into a pull request against the `feature` branch, where a quick translation check makes sure every placeholder and plural still fits.

Please translate there rather than editing the `strings.xml` files directly, since Crowdin is where the translations live and changes made only in the repository can be overwritten.

Missing your language, or not sure about a string? Ask in the [translation discussion](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/108) and it can be added. Maintainers add it as a target language in Crowdin and its code to `AppLanguage.supported`, so it appears in the app's language setting.

## Setting Up Locally

**You'll need:**
- Android Studio (Ladybug or newer)
- JDK 17

**Quick start:**
```bash
git clone https://github.com/ahmmedrejowan/PdfReaderPro.git
cd PdfReaderPro
./gradlew assembleDebug
```

## Code Style

We try to keep things consistent:

- **Kotlin** - Follow standard [Kotlin conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose** - Use `remember`, proper state hoisting, keep composables small
- **Architecture** - Clean Architecture with MVVM

For commits, we use conventional format like:
- `feat(reader): add night mode`
- `fix(tools): fix crash on merge`

But don't stress too much about this - we can always squash and clean up commits later.

## Project Structure

```
app/src/main/java/com/rejowan/pdfreaderpro/
├── data/            # Database, repositories
├── domain/          # Models, interfaces
├── presentation/    # UI (screens, components, viewmodels)
└── util/            # Helpers
```

## Releasing (maintainers)

Every release, full or pre-release, starts with a docs commit as the last commit before it. That commit updates everything a version change touches:

- `versionName` and `versionCode` in `app/build.gradle.kts` (the code goes up for every release, pre-releases included)
- a new `## [x.y.z] - YYYY-MM-DD` entry at the top of `CHANGELOG.md`, below `[Unreleased]`, such as `## [2.5.0]` or `## [2.5.0-beta.1]`
- the in-app changelog (`ChangelogContent` in `SettingsScreenContent.kt`)
- the changelog section of every README

Then run **Actions > Release > Run workflow** and pick **Full release** or **Pre-release**. Nothing else is typed in: the version is the newest `CHANGELOG.md` entry and its text becomes the release's What's New. The run stops if that version doesn't match `versionName`, and skips without building anything if the version is already released.

Full releases are marked Latest and offered by the in-app update check. Pre-releases are signed the same way but are never offered to users, since the update check only looks at the latest full release.

## Upgrading PDF.js (maintainers)

The reader runs Mozilla's PDF.js **5.3.31** from `app/src/main/assets/com/rejowan/mozilla/`. A few
features need small edits inside the vendored files themselves, because they run before, or deep
inside, code the app can reach from its own scripts (`pdfjs/helper_main.js`,
`pdfjs/helper_methods.js`).

**When PDF.js is upgraded, these edits are lost.** Re-apply each one below, then update the version
in `BundledPdfJsPatchesTest`, which fails until every patch is back in place.

Each edit in the code carries a comment starting with `PDF Reader Pro:` (except the option
defaults, which are plain values), so a search for that text finds them.

### 1. Signature editor turned on

**File:** `pdfjs/pdf_viewer.mjs`
**Feature:** Signatures (draw, type or image), added in 2.5.0

PDF.js ships a full signature editor but keeps it behind `enableSignatureEditor`, which defaults to
`false`. The option is read once while the viewer starts, before any app script runs, so the
default is flipped in the bundle. It appears twice; change both:

- in `defaultOptions` (the `AppOptions` schema): `enableSignatureEditor: { value: true, ... }`
- in `BasePreferences` defaults: `enableSignatureEditor: true,`

### 2. Images keep their colors in dark themes

**File:** `build/pdf.mjs`
**Feature:** Keep Image Colors (Display Settings, Dark and Black themes)

The dark and black reading themes invert whole pages with a CSS filter, pictures included. To undo
that for pictures only, each image is drawn through `globalThis.pdfReaderImageFilter` (an inversion)
when it is set, and the page's own inversion turns it back. `helper_methods.js` sets the variable in
`updateImageFilter()` and re-renders the pages.

In `CanvasGraphics`, just before the `drawImageAtIntegerCoords(...)` call in both
`paintInlineImageXObject` and `paintInlineImageXObjectGroup` (which `paintImageXObject` and
`paintImageXObjectRepeat` go through):

```js
// PDF Reader Pro: see pdfReaderImageFilter in helper_methods.js.
if (globalThis.pdfReaderImageFilter) {
  ctx.filter = globalThis.pdfReaderImageFilter;
}
```

PDF.js's own `pageColors` option looked like a fit, but in this version it filters the whole
finished canvas, images included, so it cannot keep their colors.

### 3. Jumps land below the floating top bar

**File:** `pdfjs/pdf_viewer.mjs`
**Feature:** Go to page, contents, bookmarks, Back to page and links land below the top bar

PDF.js scrolls a target to the very top of the view, under the reader's floating bars. While they
show, the reader passes their height through `PdfViewer.setTopInset`, stored as
`globalThis.pdfReaderTopInset`. In the module-level `scrollIntoView(element, spot, ...)` function,
just before `parent.scrollTop = offsetY;`:

```js
// PDF Reader Pro: land below the floating top bar (see setTopInset in helper_methods.js).
offsetY -= globalThis.pdfReaderTopInset || 0;
```

### Also from the PDF.js release: character maps

Not an edit, but easy to miss: `web/cmaps/` holds the character maps that Japanese, Chinese and
Korean PDFs need (#89). They come from the `pdfjs-dist` package, not the viewer download, so copy
them from the same version when upgrading.

## Questions?

- Need help? Ask in [Discussions Q&A](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/q-a)
- Found a bug? Open an [Issue](https://github.com/ahmmedrejowan/PdfReaderPro/issues)
- Have an idea? Share in [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/ideas)

---

Thanks again for contributing!
