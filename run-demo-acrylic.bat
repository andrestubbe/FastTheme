@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===========================================
echo   FastTheme Acrylic Backdrop Demo (v0.1.7)
echo ===========================================
echo.
echo Launching: Windows 11 Acrylic Material Demo (ESC to close)...
echo.

if not exist "build\fasttheme.dll" (
    call build-dll.bat > nul 2>&1
)

cd examples\Demo
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.demo.AcrylicDemo" -Djava.library.path="..\..\build"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
