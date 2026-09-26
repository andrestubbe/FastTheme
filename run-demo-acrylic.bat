@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme Acrylic Backdrop Demo (v0.1.5)
echo ===========================================
echo.
echo Launching: Windows 11 Acrylic Material Demo (ESC to close)...
echo.

cd examples
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.AcrylicDemo"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    echo Ensure you ran 'compile.bat' at least once to install FastTheme locally.
    pause
)

cd ..
