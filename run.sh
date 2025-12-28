#!/usr/bin/env bash

set -e

# =====================================
# CONFIG
# =====================================
APP_NAME="JWSP"
SRC_DIR="src"
BIN_DIR="bin"
MAIN_CLASS="jwsp.app.Main"

JAVA_URL="https://download.java.net/java/GA/jdk25.0.1/2fbf10d8c78e40bd87641c434705079d/8/GPL/openjdk-25.0.1_linux-x64_bin.tar.gz"
JAVA_ARCHIVE="/tmp/openjdk25.tar.gz"
JAVA_DIR="$HOME/Java/jdk-25.0.1"

# =====================================
# CHECK JAVA
# =====================================
if command -v java >/dev/null 2>&1; then
    echo "Java detected."
else
    echo "Java not found. Installing OpenJDK 25 (user mode)..."

    mkdir -p "$HOME/Java"

    echo "Downloading Java..."
    curl -L "$JAVA_URL" -o "$JAVA_ARCHIVE"

    echo "Extracting Java..."
    tar -xzf "$JAVA_ARCHIVE" -C "$HOME/Java"

    export JAVA_HOME="$JAVA_DIR"
    export PATH="$JAVA_HOME/bin:$PATH"

    echo "Java installed (session only)."
fi

# =====================================
# COMPILE
# =====================================
echo "Compiling JWSP..."

mkdir -p "$BIN_DIR"

SOURCE_LIST=$(mktemp)
find "$SRC_DIR" -name "*.java" > "$SOURCE_LIST"

javac -encoding UTF-8 \
  -d "$BIN_DIR" \
  -cp "lib/*" \
  @"$SOURCE_LIST"

rm "$SOURCE_LIST"

# =====================================
# RUN
# =====================================
echo "Starting JWSP..."
java -cp "$BIN_DIR:lib/*" "$MAIN_CLASS"
