@echo off
setlocal
echo ==========================================
echo   FastTheme v0.1.7 - Demo 3 (Photos-Style Chrome)
echo ==========================================
echo.

if not exist "build\fasttheme.dll" (
    echo [+] Compiling Native Bridge...
    call build-dll.bat
)

echo.
echo [+] Running Demo 3 from examples...
echo.

cd examples
call mvn compile exec:java -Dexec.mainClass="fasttheme.Demo3" -Djava.library.path="..\build"

echo.
echo === Demo 3 Complete ===
cd ..
pause
endlocal
