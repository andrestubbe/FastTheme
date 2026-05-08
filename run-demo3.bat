@echo off
setlocal
cd /d "%~dp0"

echo ===========================================
echo FastTheme Demo 3 (Advanced Transitions)
echo ===========================================
echo.
echo Launching: Smooth Transition Showcase
echo.

:: Run the demo from the example folder using Maven
cd examples
echo Compiling and Launching Demo 3...
call mvn compile exec:java -Dexec.mainClass="fasttheme.Demo3"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo 3 failed to launch. 
    echo Ensure you ran 'compile.bat' at least once to install FastTheme locally.
    pause
)

cd ..
