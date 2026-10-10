@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===================================================
echo   FastTheme Live OS Theme Listener Demo (v0.1.9)
echo ===================================================
echo.
echo Launching: Windows 11 Live Theme Listener Demo...
echo Test it: Switch Windows Light/Dark mode in Windows Settings
echo (Settings -> Personalization -> Colors) while this runs!
echo.

if not exist "build\fasttheme.dll" (
    call compile.bat > nul 2>&1
)

cd examples\Demo
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.demo.ListenerDemo" -Djava.library.path="..\..\build"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
