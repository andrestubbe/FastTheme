@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme Mica Backdrop Demo (v0.1.5)
echo ===========================================
echo.
echo Launching: Windows 11 Mica Material Demo
echo.

cd examples
echo Compiling and Launching MicaDemo...
call mvn compile exec:java -Dexec.mainClass="fasttheme.MicaDemo"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    echo Ensure you ran 'compile.bat' at least once to install FastTheme locally.
    pause
)

cd ..
