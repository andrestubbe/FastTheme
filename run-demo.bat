@echo off

setlocal

echo ===========================================
echo FastTheme Demo (v0.1.0)
echo ===========================================
echo.
echo Launching: Standard Swing vs. FastTheme Native
echo Dependencies resolved from JitPack
echo.

cd examples
echo Compiling and Launching Demo...
call mvn compile exec:java -Dexec.mainClass="fasttheme.Demo"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Demo failed to launch.
    pause
)

cd ..
