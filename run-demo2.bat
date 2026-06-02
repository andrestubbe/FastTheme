@echo off
setlocal
echo ==========================================
echo   FastTheme v0.1.0 - Premium Demo 2
echo ==========================================
echo.

:: 1. Build native DLL
echo [+] Compiling Native Bridge...
call compile.bat

echo.
echo [+] Running Demo 2 from examples...
echo.

:: We run the example project which depends on the main FastTheme
cd examples
mvn compile exec:java -Dexec.mainClass="fasttheme.Demo2" -Djava.library.path="..\build"

echo.
echo === Demo 2 Complete ===
cd ..
pause
endlocal
