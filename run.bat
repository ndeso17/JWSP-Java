@echo off
setlocal EnableDelayedExpansion

:: ================================
:: CONFIG
:: ================================
set SRC_DIR=src
set BIN_DIR=bin
set MAIN_CLASS=jwsp.app.Main

set JAVA_URL=https://download.java.net/java/GA/jdk25.0.1/2fbf10d8c78e40bd87641c434705079d/8/GPL/openjdk-25.0.1_windows-x64_bin.zip
set JAVA_ZIP=%TEMP%\openjdk25.zip
set JAVA_DIR=%USERPROFILE%\Java\jdk-25.0.1

:: ================================
:: CHECK JAVA
:: ================================
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo Java detected.
    goto COMPILE
)

echo Java not found. Installing OpenJDK 25 (user mode)...

:: ================================
:: DOWNLOAD JAVA
:: ================================
powershell -Command ^
  "Invoke-WebRequest '%JAVA_URL%' -OutFile '%JAVA_ZIP%'"

if not exist "%JAVA_ZIP%" (
    echo Failed to download Java.
    pause
    exit /b
)

:: ================================
:: EXTRACT JAVA
:: ================================
if not exist "%USERPROFILE%\Java" mkdir "%USERPROFILE%\Java"

powershell -Command ^
  "Expand-Archive '%JAVA_ZIP%' '%USERPROFILE%\Java' -Force"

:: ================================
:: TEMP ENV (NO ADMIN)
:: ================================
set JAVA_HOME=%JAVA_DIR%
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java installed (session only).

:: ================================
:: COMPILE (WINDOWS SAFE)
:: ================================
:COMPILE
echo Compiling JWSP...

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

:: Generate source list
set SOURCES=%TEMP%\jwsp_sources.txt
dir /s /b "%SRC_DIR%\*.java" > "%SOURCES%"

javac -encoding UTF-8 ^
  -d "%BIN_DIR%" ^
  -cp "lib/*" ^
  @"%SOURCES%"

if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b
)

del "%SOURCES%"

:: ================================
:: RUN (NO TERMINAL)
:: ================================
echo Starting JWSP...
javaw -cp "%BIN_DIR%;lib/*" %MAIN_CLASS%

exit
