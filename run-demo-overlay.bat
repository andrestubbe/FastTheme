@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ==========================================
echo   FastTheme v0.1.7 - Overlay Demo (Raycast)
echo ==========================================
echo.

if not exist "build\fasttheme.dll" (
    echo [+] Compiling Native Bridge...
    call build-dll.bat > nul 2>&1
)

echo [+] Compiling FastTheme...
call mvn -q install -DskipTests > nul 2>&1

echo [+] Launching Overlay Demo...
cd examples\Demo
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.demo.Overlay" -Djava.library.path="..\..\build"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
