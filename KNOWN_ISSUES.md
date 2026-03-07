# Known Issues

This document lists known issues and limitations in PDF Reader Pro, along with workarounds where available.

## PDF Rendering

### Large PDFs may cause slowdowns
**Issue:** PDFs with more than 500 pages or very large file sizes (>100MB) may experience slower scrolling and rendering.

**Workaround:**
- Use the page scrubber for faster navigation
- Split large PDFs into smaller files using the Split tool
- Close other apps to free up memory

**Status:** Under investigation

---

### Some PDF forms are not interactive
**Issue:** Complex PDF forms with JavaScript or advanced form fields may not be fully interactive.

**Workaround:** Use a desktop PDF editor for form filling.

**Status:** Limitation of PDF.js

---

### Certain fonts may not render correctly
**Issue:** PDFs using non-standard or custom embedded fonts may display with fallback fonts.

**Workaround:** If text appears incorrect, try opening the PDF on a desktop reader to verify the content.

**Status:** Limitation of PDF.js font handling

---

## PDF Tools

### Password-protected PDFs cannot be merged
**Issue:** The merge tool does not support password-protected PDF files.

**Workaround:** Use the Unlock tool to remove password protection first, then merge.

**Status:** By design (security consideration)

---

### Watermark positioning on rotated pages
**Issue:** Watermarks may appear in unexpected positions on pages that were previously rotated.

**Workaround:** Rotate pages to the correct orientation before adding watermarks.

**Status:** Known limitation

---

## File Access

### External storage permission required
**Issue:** On Android 10 and below, the app requires storage permissions to access PDF files. On Android 11+, the app uses scoped storage.

**Workaround:** Grant the requested permissions when prompted.

**Status:** Android platform requirement

---

### Some cloud storage files not accessible
**Issue:** PDFs opened from certain cloud storage apps may not be editable with PDF tools.

**Workaround:** Download the file locally first, then open it from the device storage.

**Status:** Android content provider limitation

---

## Performance

### Initial app launch may be slow
**Issue:** The first launch after installation may take a few seconds while initializing.

**Workaround:** Subsequent launches will be faster.

**Status:** Normal behavior for initial setup

---

### Memory usage with multiple tabs
**Issue:** Opening multiple large PDFs in quick succession may increase memory usage.

**Workaround:** Close PDFs when finished viewing to free memory.

**Status:** Under optimization

---

## Accessibility

### Some icons lack content descriptions
**Issue:** Not all decorative icons have accessibility labels, which may affect screen reader users.

**Workaround:** Use the text labels that accompany most controls.

**Status:** Being addressed in future updates

---

## Reporting New Issues

If you encounter an issue not listed here, please report it:

1. **Check existing issues:** [GitHub Issues](https://github.com/ahmmedrejowan/PdfReaderPro/issues)
2. **Create a new issue** with:
   - Device model and Android version
   - App version
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Sample PDF (if the issue is file-specific)

---

## Fixed Issues

Issues that have been fixed in recent releases:

| Issue | Fixed In | Description |
|-------|----------|-------------|
| Null pointer crash on corrupted PDFs | v2.0.0 | Added proper error handling |
| Storage full errors not handled | v2.0.0 | Added pre-flight storage checks |
| Bookmark loss on app restart | v2.0.0 | Fixed Room database migration |
| Landscape layout issues | v2.0.0 | Implemented responsive sheets |

---

*Last updated: 2026-03-07*
