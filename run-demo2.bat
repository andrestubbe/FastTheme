@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme WakeUp Demo (Demo2)
echo ===========================================
echo.
echo Starting in Standard Swing...
echo Press ENTER on the window to trigger FastTheme.
echo.

cd examples
call mvn compile exec:java -Dexec.mainClass="fasttheme.Demo2"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch. 
    echo Ensure you ran 'compile.bat' at least once.
    pause
)

cd ..
