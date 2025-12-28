@echo off
REM ============================================================
REM build.bat - Build & Package Script untuk JWSP (Windows)
REM ============================================================
REM
REM Persyaratan:
REM - JDK 17+ (dengan jpackage)
REM - WiX Toolset 3.0+ (untuk .exe installer, opsional)
REM
REM Usage:
REM   build.bat compile    - Compile saja
REM   build.bat jar        - Buat JAR
REM   build.bat portable   - Buat portable ZIP
REM   build.bat installer  - Buat native installer
REM   build.bat all        - Semua
REM ============================================================

setlocal EnableDelayedExpansion

REM === CONFIGURATION ===
set APP_NAME=JWSP
set APP_VERSION=1.0.0
set MAIN_CLASS=jwsp.app.Main

set SRC_DIR=src
set BUILD_DIR=build
set DIST_DIR=dist
set CLASSES_DIR=%BUILD_DIR%\classes
set JAR_FILE=%BUILD_DIR%\%APP_NAME%.jar

REM === MAIN ===

if "%1"=="" goto help
if "%1"=="help" goto help
if "%1"=="compile" goto compile
if "%1"=="jar" goto jar
if "%1"=="portable" goto portable
if "%1"=="installer" goto installer
if "%1"=="all" goto all
if "%1"=="clean" goto clean
goto help

:help
echo JWSP Build Script (Windows)
echo.
echo Usage: build.bat [command]
echo.
echo Commands:
echo   compile    - Compile Java source files
echo   jar        - Create executable JAR
echo   portable   - Create portable distribution
echo   installer  - Create native installer (.exe)
echo   all        - Run all build steps
echo   clean      - Clean build artifacts
echo.
goto end

:clean
echo [Build] Cleaning...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
echo [Build] Clean complete
goto end

:compile
echo [Build] Checking Java...
java -version 2>nul
if errorlevel 1 (
    echo [Error] Java not found. Please install JDK 17+.
    exit /b 1
)

echo [Build] Compiling...
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

REM Compile all Java files
dir /s /b "%SRC_DIR%\*.java" > "%BUILD_DIR%\sources.txt"
javac -d "%CLASSES_DIR%" @"%BUILD_DIR%\sources.txt"

REM Copy resources
if exist "%SRC_DIR%\data" xcopy /s /i /y "%SRC_DIR%\data" "%CLASSES_DIR%\data"
if exist "%SRC_DIR%\audio" xcopy /s /i /y "%SRC_DIR%\audio" "%CLASSES_DIR%\audio"
if exist "%SRC_DIR%\themes" xcopy /s /i /y "%SRC_DIR%\themes" "%CLASSES_DIR%\themes"

REM Copy icon resource
if not exist "%CLASSES_DIR%\jwsp\app\resources" mkdir "%CLASSES_DIR%\jwsp\app\resources"
copy "%SRC_DIR%\jwsp\app\resources\icon.png" "%CLASSES_DIR%\jwsp\app\resources\"

echo [Build] Compilation complete
goto end

:jar
call :compile

echo [Build] Creating JAR...

REM Create manifest
echo Manifest-Version: 1.0 > "%BUILD_DIR%\MANIFEST.MF"
echo Main-Class: %MAIN_CLASS% >> "%BUILD_DIR%\MANIFEST.MF"
echo Application-Name: %APP_NAME% >> "%BUILD_DIR%\MANIFEST.MF"

REM Create JAR
jar cfm "%JAR_FILE%" "%BUILD_DIR%\MANIFEST.MF" -C "%CLASSES_DIR%" .

echo [Build] JAR created: %JAR_FILE%
goto end

:portable
call :jar

echo [Build] Creating portable distribution...

set PORTABLE_DIR=%DIST_DIR%\portable\%APP_NAME%
if not exist "%PORTABLE_DIR%" mkdir "%PORTABLE_DIR%"

REM Copy JAR
copy "%JAR_FILE%" "%PORTABLE_DIR%\"

REM Copy resources
if exist "%SRC_DIR%\audio" xcopy /s /i /y "%SRC_DIR%\audio" "%PORTABLE_DIR%\audio"
if exist "%SRC_DIR%\data" xcopy /s /i /y "%SRC_DIR%\data" "%PORTABLE_DIR%\data"
if not exist "%PORTABLE_DIR%\themes" mkdir "%PORTABLE_DIR%\themes"

REM Create launcher
echo @echo off > "%PORTABLE_DIR%\run.bat"
echo cd /d "%%~dp0" >> "%PORTABLE_DIR%\run.bat"
echo start javaw -jar JWSP.jar >> "%PORTABLE_DIR%\run.bat"

echo [Build] Portable created: %PORTABLE_DIR%
goto end

:installer
call :jar

echo [Build] Creating native installer...

REM Check jpackage
where jpackage >nul 2>&1
if errorlevel 1 (
    echo [Error] jpackage not found. Requires JDK 17+.
    exit /b 1
)

if not exist "%DIST_DIR%\installer" mkdir "%DIST_DIR%\installer"

REM Create input directory
set INPUT_DIR=%BUILD_DIR%\input
if not exist "%INPUT_DIR%" mkdir "%INPUT_DIR%"
copy "%JAR_FILE%" "%INPUT_DIR%\"
if exist "%SRC_DIR%\audio" xcopy /s /i /y "%SRC_DIR%\audio" "%INPUT_DIR%\audio"
if exist "%SRC_DIR%\data" xcopy /s /i /y "%SRC_DIR%\data" "%INPUT_DIR%\data"

echo [Build] Running jpackage...

jpackage ^
    --type exe ^
    --input "%INPUT_DIR%" ^
    --main-jar "%APP_NAME%.jar" ^
    --main-class "%MAIN_CLASS%" ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --description "Jadwal Waktu Sholat dan Puasa" ^
    --vendor "JWSP Developer" ^
    --dest "%DIST_DIR%\installer" ^
    --icon "jwsp.png" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "JWSP" ^
    --java-options "-Xmx256m"

if errorlevel 1 (
    echo [Warning] exe creation failed. Creating app-image instead...
    
    jpackage ^
        --type app-image ^
        --input "%INPUT_DIR%" ^
        --main-jar "%APP_NAME%.jar" ^
        --main-class "%MAIN_CLASS%" ^
        --name "%APP_NAME%" ^
        --app-version "%APP_VERSION%" ^
        --dest "%DIST_DIR%\installer"
)

echo [Build] Installer created in: %DIST_DIR%\installer\
goto end

:all
call :clean
call :compile
call :jar
call :portable
call :installer
echo.
echo ============================================================
echo BUILD COMPLETE
echo ============================================================
goto end

:end
endlocal
