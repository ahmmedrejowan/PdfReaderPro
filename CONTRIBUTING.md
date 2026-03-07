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

## Questions?

- Need help? Ask in [Discussions Q&A](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/q-a)
- Found a bug? Open an [Issue](https://github.com/ahmmedrejowan/PdfReaderPro/issues)
- Have an idea? Share in [Discussions](https://github.com/ahmmedrejowan/PdfReaderPro/discussions/categories/ideas)

---

Thanks again for contributing!
