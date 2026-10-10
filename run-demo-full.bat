@echo off
setlocal enabledelayedexpansion
chcp 65001 > nul
cd /d "%~dp0"

echo ==============================================================
echo   FastGraphics Full — Seamless Desktop Canvas Demo
echo ==============================================================
echo.

cd examples\Demo
if not exist "cp.txt" (
    echo [+] Generating classpath...
    call mvn -q dependency:build-classpath "-Dmdep.outputFile=cp.txt"
)

set /p CP=<cp.txt
set "FASTCORE_JAR=%USERPROFILE%\.m2\repository\com\github\andrestubbe\FastCore\0.1.1\FastCore-0.1.1.jar"
set "FULL_CP=target\classes;!FASTCORE_JAR!;!CP!"

echo [+] Compiling Demo...
call mvn -q compile
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [+] Launching DemoFull...
java --enable-preview --enable-native-access=ALL-UNNAMED "-Djava.library.path=..\..\build;..\..\dll;." -cp "!FULL_CP!" fasttheme.demo.DemoFull

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo exited with error code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
