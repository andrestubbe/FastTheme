@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme Demo (v0.2.0)
echo ===========================================
echo.
echo Launching: Standard Swing vs. FastTheme Native
echo.

:: Run the demo from the example folder using Maven
cd examples
echo Compiling and Launching Demo...
call mvn compile exec:java -Dexec.mainClass="fasttheme.Demo"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    echo Ensure you ran 'compile.bat' at least once to install FastTheme locally.
    pause
)

cd ..
