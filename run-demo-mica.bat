@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===========================================
echo   FastTheme Mica Backdrop Demo (v0.1.7)
echo ===========================================
echo.
echo Launching: Windows 11 Mica Material Demo (ESC to close)...
echo.


cd examples\Demo
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.demo.Mica" -Djava.library.path="..\..\build"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
    exit /b %ERRORLEVEL%
)

cd ..\..
