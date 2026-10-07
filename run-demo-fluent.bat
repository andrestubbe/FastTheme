@echo off
setlocal enabledelayedexpansion
chcp 65001 > nul
cd /d "%~dp0"

echo ==============================================================
echo   FastTheme + FastWindow + FastDirectX - Fluent Mica Demo
echo ==============================================================
echo.


cd examples\Demo
if not exist "cp.txt" (
    echo [+] Generating classpath...
    call mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
)

set /p CP=<cp.txt
set "FASTCORE_JAR=%USERPROFILE%\.m2\repository\com\github\andrestubbe\FastCore\0.1.1\FastCore-0.1.1.jar"
set "FULL_CP=target\classes;!FASTCORE_JAR!;!CP!"

echo [+] Launching DemoFluentFastWindow...
java --enable-preview --enable-native-access=ALL-UNNAMED "-Djava.library.path=..\..\build;..\..\dll;." -cp "!FULL_CP!" fasttheme.demo.DemoFluentFastWindow

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo exited with error code %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
