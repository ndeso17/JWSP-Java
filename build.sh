#!/bin/bash
# ============================================================
# build.sh - Build & Package Script untuk JWSP
# ============================================================
# 
# Script ini mengompilasi dan menyiapkan aplikasi untuk:
# - Portable distribution (ZIP)
# - Native installer (deb/exe) via jpackage
#
# Persyaratan:
# - JDK 17+ (dengan jpackage)
# - Linux: dpkg-deb (untuk .deb)
# - Windows: WiX Toolset 3.0+ (untuk .exe installer)
#
# Usage:
#   ./build.sh compile    # Compile saja
#   ./build.sh jar        # Buat JAR
#   ./build.sh portable   # Buat portable ZIP
#   ./build.sh installer  # Buat native installer
#   ./build.sh all        # Semua
# ============================================================

set -e

# === CONFIGURATION ===
APP_NAME="JWSP"
APP_VERSION="1.0.0"
APP_DESCRIPTION="Jadwal Waktu Sholat & Puasa"
APP_VENDOR="JWSP Developer"
MAIN_CLASS="jwsp.app.Main"

# Directories
SRC_DIR="src"
BUILD_DIR="build"
DIST_DIR="dist"
CLASSES_DIR="$BUILD_DIR/classes"
JAR_FILE="$BUILD_DIR/$APP_NAME.jar"

# === FUNCTIONS ===

show_help() {
    echo "JWSP Build Script"
    echo ""
    echo "Usage: ./build.sh [command]"
    echo ""
    echo "Commands:"
    echo "  compile    - Compile Java source files"
    echo "  jar        - Create executable JAR"
    echo "  portable   - Create portable ZIP distribution"
    echo "  installer  - Create native installer (deb/exe)"
    echo "  all        - Run all build steps"
    echo "  clean      - Clean build artifacts"
    echo ""
}

check_java() {
    echo "[Build] Checking Java version..."
    
    if ! command -v java &> /dev/null; then
        echo "[Error] Java not found. Please install JDK 17+."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo "[Build] Java version: $JAVA_VERSION"
    
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "[Warning] jpackage requires JDK 17+. Current: $JAVA_VERSION"
    fi
}

clean() {
    echo "[Build] Cleaning..."
    rm -rf "$BUILD_DIR" "$DIST_DIR"
    echo "[Build] Clean complete"
}

compile() {
    echo "[Build] Compiling..."
    
    mkdir -p "$CLASSES_DIR"
    
    # Compile all Java files
    find "$SRC_DIR" -name "*.java" > "$BUILD_DIR/sources.txt"
    javac -d "$CLASSES_DIR" @"$BUILD_DIR/sources.txt"
    
    # Copy resources
    cp -r "$SRC_DIR/data" "$CLASSES_DIR/" 2>/dev/null || true
    cp -r "$SRC_DIR/audio" "$CLASSES_DIR/" 2>/dev/null || true
    cp -r "$SRC_DIR/themes" "$CLASSES_DIR/" 2>/dev/null || true
    
    # Copy icon resource
    mkdir -p "$CLASSES_DIR/jwsp/app/resources"
    cp "$SRC_DIR/jwsp/app/resources/icon.png" "$CLASSES_DIR/jwsp/app/resources/"
    
    echo "[Build] Compilation complete"
}

create_jar() {
    echo "[Build] Creating JAR..."
    
    # Create manifest
    mkdir -p "$BUILD_DIR"
    cat > "$BUILD_DIR/MANIFEST.MF" << EOF
Manifest-Version: 1.0
Main-Class: $MAIN_CLASS
Application-Name: $APP_NAME
Implementation-Version: $APP_VERSION
EOF
    
    # Create JAR
    jar cfm "$JAR_FILE" "$BUILD_DIR/MANIFEST.MF" -C "$CLASSES_DIR" .
    
    echo "[Build] JAR created: $JAR_FILE"
}

create_portable() {
    echo "[Build] Creating portable distribution..."
    
    PORTABLE_DIR="$DIST_DIR/portable/$APP_NAME"
    mkdir -p "$PORTABLE_DIR"
    
    # Copy JAR
    cp "$JAR_FILE" "$PORTABLE_DIR/"
    
    # Copy external resources
    cp -r "$SRC_DIR/audio" "$PORTABLE_DIR/" 2>/dev/null || true
    cp -r "$SRC_DIR/data" "$PORTABLE_DIR/" 2>/dev/null || true
    mkdir -p "$PORTABLE_DIR/themes"
    
    # Create launcher scripts
    
    # Linux launcher
    cat > "$PORTABLE_DIR/run.sh" << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
java -jar JWSP.jar
EOF
    chmod +x "$PORTABLE_DIR/run.sh"
    
    # Windows launcher
    cat > "$PORTABLE_DIR/run.bat" << 'EOF'
@echo off
cd /d "%~dp0"
start javaw -jar JWSP.jar
EOF
    
    # README
    cat > "$PORTABLE_DIR/README.txt" << EOF
JWSP - Jadwal Waktu Sholat & Puasa
Version: $APP_VERSION

CARA MENJALANKAN:
- Linux: ./run.sh
- Windows: Double-click run.bat atau JWSP.jar

PERSYARATAN:
- Java Runtime Environment (JRE) 17+

AUDIO:
- Letakkan file audio (MP3/WAV) di folder audio/
- Subfolders: adzan/, sirine/, tarkhim/

TEMA KUSTOM:
- Buat file .theme di folder themes/
- Format: key=value (lihat dokumentasi)

CATATAN:
- Aplikasi berjalan di system tray
- Tutup jendela tidak menghentikan aplikasi
- Gunakan menu tray untuk keluar
EOF
    
    # Create ZIP
    cd "$DIST_DIR/portable"
    zip -r "$APP_NAME-$APP_VERSION-portable.zip" "$APP_NAME"
    cd - > /dev/null
    
    echo "[Build] Portable created: $DIST_DIR/portable/$APP_NAME-$APP_VERSION-portable.zip"
}

create_installer() {
    echo "[Build] Creating native installer..."
    
    # Check jpackage
    if ! command -v jpackage &> /dev/null; then
        echo "[Error] jpackage not found. Requires JDK 17+."
        exit 1
    fi
    
    mkdir -p "$DIST_DIR/installer"
    
    # Create app image with bundled JRE
    INSTALLER_TYPE="app-image"
    
    # Detect OS
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        INSTALLER_TYPE="deb"
        ICON_FILE=""
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        INSTALLER_TYPE="exe"
        ICON_FILE=""
    fi
    
    # Create input directory
    INPUT_DIR="$BUILD_DIR/input"
    mkdir -p "$INPUT_DIR"
    cp "$JAR_FILE" "$INPUT_DIR/"
    
    # Copy external resources to input
    cp -r "$SRC_DIR/audio" "$INPUT_DIR/" 2>/dev/null || true
    cp -r "$SRC_DIR/data" "$INPUT_DIR/" 2>/dev/null || true
    mkdir -p "$INPUT_DIR/themes"
    
    echo "[Build] Running jpackage..."
    
    jpackage \
        --type "$INSTALLER_TYPE" \
        --input "$INPUT_DIR" \
        --main-jar "$APP_NAME.jar" \
        --main-class "$MAIN_CLASS" \
        --name "$APP_NAME" \
        --app-version "$APP_VERSION" \
        --description "$APP_DESCRIPTION" \
        --vendor "$APP_VENDOR" \
        --dest "$DIST_DIR/installer" \
        --java-options "-Xmx256m" \
        --icon "jwsp.png" \
        --linux-shortcut \
        --linux-app-category "Utility" \
        2>&1 || {
            echo "[Warning] jpackage failed, creating app-image instead..."
            
            jpackage \
                --type "app-image" \
                --input "$INPUT_DIR" \
                --main-jar "$APP_NAME.jar" \
                --main-class "$MAIN_CLASS" \
                --name "$APP_NAME" \
                --app-version "$APP_VERSION" \
                --dest "$DIST_DIR/installer"
        }
    
    echo "[Build] Installer created in: $DIST_DIR/installer/"
}

build_all() {
    check_java
    clean
    compile
    create_jar
    create_portable
    create_installer
    
    echo ""
    echo "============================================================"
    echo "BUILD COMPLETE"
    echo "============================================================"
    echo ""
    echo "Outputs:"
    echo "  JAR:       $JAR_FILE"
    echo "  Portable:  $DIST_DIR/portable/"
    echo "  Installer: $DIST_DIR/installer/"
    echo ""
}

# === MAIN ===

case "${1:-help}" in
    compile)
        check_java
        compile
        ;;
    jar)
        check_java
        compile
        create_jar
        ;;
    portable)
        check_java
        compile
        create_jar
        create_portable
        ;;
    installer)
        check_java
        compile
        create_jar
        create_installer
        ;;
    all)
        build_all
        ;;
    clean)
        clean
        ;;
    *)
        show_help
        ;;
esac
