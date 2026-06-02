@echo off
chcp 65001 >nul

setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme Demo (v0.1.0)
echo ===========================================
echo.
echo Launching: Standard Swing vs. FastTheme Native
echo.

:: Build and install FastTheme to local Maven repo first
echo Building and installing FastTheme...
call compile.bat
if %errorlevel% neq 0 ( pause & exit /b )

:: Run the demo from the example folder using Maven
cd examples
echo Compiling and Launching Demo...
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.Demo"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    echo Ensure you ran 'compile.bat' at least once to install FastTheme locally.
    pause
)

cd ..
