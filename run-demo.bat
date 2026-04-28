@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme Comparison Demo (v0.1.0)
echo ===========================================
echo.
echo Launching Side-by-Side:
echo [1] Standard Swing Window
echo [2] FastTheme Styled Window
echo.

:: Run the demo from the example folder using Maven
cd examples\01-window-demo
echo Compiling and Launching Comparison Demo...
call mvn compile exec:java
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    echo Ensure you ran 'compile.bat' at least once to install FastTheme locally.
    pause
)

cd ..\..
