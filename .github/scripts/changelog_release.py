#!/usr/bin/env python3
"""Prints the newest CHANGELOG.md entry as GitHub Actions step outputs.

The release workflow takes nothing typed in: the version is whatever the newest
"## [x.y.z] - date" heading below [Unreleased] says, and that entry's text is
the release's What's New.

Usage: changelog_release.py CHANGELOG.md >> "$GITHUB_OUTPUT"
"""
import re
import secrets
import sys

HEADING = re.compile(r"^## \[([^\]]+)\]")
VERSION = re.compile(r"^\d+\.\d+\.\d+(-[0-9A-Za-z.]+)?$")


def fail(message):
    print(f"::error file=CHANGELOG.md::{message}", file=sys.stderr)
    sys.exit(1)


def newest_entry(text):
    lines = text.splitlines()
    for i, line in enumerate(lines):
        match = HEADING.match(line)
        if not match or match.group(1).strip().lower() == "unreleased":
            continue
        notes = []
        for following in lines[i + 1:]:
            if HEADING.match(following) or following.strip() == "---":
                break
            notes.append(following)
        return match.group(1).strip(), "\n".join(notes).strip()
    return None, None


def main():
    with open(sys.argv[1], encoding="utf-8") as changelog:
        version, notes = newest_entry(changelog.read())
    if version is None:
        fail("No version entry found. Add a section like '## [2.5.0] - 2026-10-08' below [Unreleased].")
    if not VERSION.match(version):
        fail(f"'{version}' is not a version like 2.5.0 or 2.5.0-beta.1.")
    if not notes:
        fail(f"The entry for {version} has no notes.")

    delimiter = f"NOTES_{secrets.token_hex(8)}"
    print(f"version={version}")
    print(f"apk_name=PdfReaderPro-v{version}.apk")
    print(f"notes<<{delimiter}")
    print(notes)
    print(delimiter)
    print(f"Version {version}, {len(notes.splitlines())} lines of notes", file=sys.stderr)


if __name__ == "__main__":
    main()
