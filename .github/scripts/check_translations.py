#!/usr/bin/env python3
"""Checks every translation against the English strings, in seconds.

Run on pull requests so a translator sees the exact string to fix instead of a
Gradle or lint failure. For each values-<lang>/strings.xml it checks:
  - the file is valid XML
  - every translatable English string and plural is present
  - placeholders (%s, %1$d, ...) match English
  - apostrophes are escaped (\\'), which Android's resource compiler requires
  - each plural has the quantities the language needs

Errors are printed as GitHub annotations; the exit code is 1 if any were found.

Usage: check_translations.py [res_dir]   (default app/src/main/res)
"""
import glob
import os
import re
import sys
import xml.etree.ElementTree as ET

RES = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res"

PLACEHOLDER = re.compile(r"%(\d+\$)?[-#+0,(]*\d*(\.\d+)?[sdfxXc]")

# Plural quantities each language needs (CLDR, as Android applies them).
# "many" in es/fr/it/pt only covers counts like 1,000,000, so it is a warning.
PLURALS = {
    "en": ({"one", "other"}, set()),
    "bn": ({"one", "other"}, set()),
    "de": ({"one", "other"}, set()),
    "es": ({"one", "other"}, {"many"}),
    "fr": ({"one", "other"}, {"many"}),
    "ga": ({"one", "two", "few", "many", "other"}, set()),
    "it": ({"one", "other"}, {"many"}),
    "ja": ({"other"}, set()),
    "pl": ({"one", "few", "many", "other"}, set()),
    "pt": ({"one", "other"}, {"many"}),
    "ru": ({"one", "few", "many", "other"}, set()),
    "tr": ({"one", "other"}, set()),
}

errors = 0


def annotate(level, path, line, message):
    global errors
    if level == "error":
        errors += 1
    location = f"file={path}" + (f",line={line}" if line else "")
    print(f"::{level} {location}::{message}")


def placeholders(text):
    """Placeholders by position and type, so %1$d and a lone %d compare equal."""
    found, position = [], 0
    for match in PLACEHOLDER.finditer(text.replace("%%", "")):
        position += 1
        explicit = re.match(r"%(\d+)\$", match.group(0))
        found.append((int(explicit.group(1)) if explicit else position, match.group(0)[-1]))
    return sorted(found)


def line_of(raw, name):
    for number, line in enumerate(raw.splitlines(), 1):
        if f'name="{name}"' in line:
            return number
    return None


def load(path):
    """Strings and plurals from one file, with the raw text for line numbers."""
    raw = open(path, encoding="utf-8").read()
    root = ET.fromstring(raw)
    strings, plurals = {}, {}
    for element in root:
        name = element.get("name")
        if element.tag == "string":
            strings[name] = ("".join(element.itertext()), element.get("translatable") != "false")
        elif element.tag == "plurals":
            plurals[name] = {item.get("quantity"): "".join(item.itertext()) for item in element}
    return raw, strings, plurals


def unescaped_apostrophes(raw):
    """Lines whose string or plural item text has an apostrophe without a backslash."""
    for match in re.finditer(r"<(string|item)\b[^>]*>(.*?)</\1>", raw, re.DOTALL):
        number = raw.count("\n", 0, match.start()) + 1
        text = match.group(2).strip()
        if text.startswith('"') and text.endswith('"'):
            continue  # a double-quoted string may hold plain apostrophes
        if re.search(r"(?<!\\)'", text):
            yield number, text


def main():
    english_path = os.path.join(RES, "values", "strings.xml")
    _, english, english_plurals = load(english_path)
    translatable = {name for name, (_, flag) in english.items() if flag}

    for path in sorted(glob.glob(os.path.join(RES, "values-*", "strings.xml"))):
        folder = os.path.basename(os.path.dirname(path))
        language = folder.removeprefix("values-")
        if not re.fullmatch(r"[a-z]{2,3}(-r[A-Z]{2})?", language):
            continue  # values-night and other qualifiers are not languages
        try:
            raw, strings, plurals = load(path)
        except ET.ParseError as error:
            annotate("error", path, error.position[0], f"Not valid XML: {error}")
            continue

        for name in sorted(translatable - strings.keys()):
            annotate("error", path, None, f"Missing string '{name}' (English: {english[name][0][:80]!r})")
        for name in sorted(english_plurals.keys() - plurals.keys()):
            annotate("error", path, None, f"Missing plural '{name}'")

        for name, (text, _) in strings.items():
            if name in english and placeholders(text) != placeholders(english[name][0]):
                annotate("error", path, line_of(raw, name),
                         f"'{name}' placeholders differ from English: {text!r} vs {english[name][0]!r}")

        required, optional = PLURALS.get(language.split("-")[0], ({"other"}, set()))
        for name, items in plurals.items():
            if name not in english_plurals:
                continue
            missing = required - items.keys()
            if missing:
                annotate("error", path, line_of(raw, name),
                         f"Plural '{name}' needs quantities {sorted(missing)} in {language}")
            if optional - items.keys():
                annotate("warning", path, line_of(raw, name),
                         f"Plural '{name}' could add {sorted(optional - items.keys())} for very large counts")
            expected = placeholders(english_plurals[name].get("other", ""))
            for quantity, text in items.items():
                if placeholders(text) != expected:
                    annotate("error", path, line_of(raw, name),
                             f"Plural '{name}' ({quantity}) placeholders differ from English: {text!r}")

        for number, text in unescaped_apostrophes(raw):
            annotate("error", path, number, f"Unescaped apostrophe, write \\' instead: {text[:80]!r}")

    if errors:
        print(f"{errors} translation problem(s) found.")
        return 1
    print("All translations match the English strings.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
