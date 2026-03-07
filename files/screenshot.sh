#!/bin/bash

# Screenshot script using adb
# Saves to files/shot{n}.png with auto-increment

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Find next available number
NUM=1
while [ -f "$SCRIPT_DIR/shot$NUM.png" ]; do
    ((NUM++))
done

FILENAME="shot$NUM.png"
FILEPATH="$SCRIPT_DIR/$FILENAME"

# Take screenshot via adb
adb exec-out screencap -p > "$FILEPATH"

if [ $? -eq 0 ] && [ -s "$FILEPATH" ]; then
    echo "Saved: $FILENAME"
else
    rm -f "$FILEPATH"
    echo "Failed to capture screenshot. Is device connected?"
    exit 1
fi
