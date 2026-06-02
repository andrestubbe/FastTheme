@echo off
setlocal
chcp 65001 >nul

cd /d "%~dp0"

echo ==========================================
echo   FastTheme v0.1.0 - Premium Demo 2
echo ==========================================
echo.
echo Dependencies resolved from JitPack
echo.

cd examples
echo [+] Running Demo 2...
echo.
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.Demo2"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo 2 failed to launch.
    pause
)

echo.
echo === Demo 2 Complete ===
cd ..
pause
endlocal
