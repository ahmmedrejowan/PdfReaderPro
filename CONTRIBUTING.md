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

1. Copy `app/src/main/res/values/strings.xml` to `values-<code>/strings.xml` (for example `values-it`), or edit an existing language
2. Translate the text only; keep every placeholder (`%s`, `%1$d`) and write apostrophes as `\'`
3. Add the language code to `AppLanguage.supported` so it appears in the app's language setting
4. Open the Pull Request against the `feature` branch

A quick translation check runs on every PR and points to any string that would break the build.

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

## Questions?

- Need help? Ask in [Discussions Q&A](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/q-a)
- Found a bug? Open an [Issue](https://github.com/ahmmedrejowan/PdfReaderPro/issues)
- Have an idea? Share in [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/ideas)

---

Thanks again for contributing!
