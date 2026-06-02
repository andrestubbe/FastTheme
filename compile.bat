@echo off
setlocal EnableDelayedExpansion

:: Change to script directory
cd /d "%~dp0"

echo ===========================================
echo FastTheme JNI Bridge Build Script
echo ===========================================
echo.
echo Running in: %CD%
echo.

:: Check for Java
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
)

if not exist "%JAVA_HOME%\include\jni.h" (
    echo ERROR: Cannot find jni.h in %JAVA_HOME%\include
    echo Please check your Java installation
    pause
    exit /b 1
)

:: Use vswhere to find Visual Studio
set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"

if not exist "%VSWHERE%" (
    echo ERROR: vswhere.exe not found!
    echo Visual Studio Installer might be missing.
    echo.
    pause
    exit /b 1
)

:: Find VS installation path
for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
    set "VS_INSTALL=%%i"
)

if not defined VS_INSTALL (
    echo ERROR: Visual Studio with C++ tools not found!
    echo.
    pause
    exit /b 1
)

echo Found Visual Studio at: %VS_INSTALL%

:: Setup VS environment
set "VCVARS=%VS_INSTALL%\VC\Auxiliary\Build\vcvars64.bat"

echo Setting up Visual Studio environment...
call "%VCVARS%"
if errorlevel 1 (
    echo ERROR: Failed to setup VS environment
    pause
    exit /b 1
)

:: Create build directories
if not exist build mkdir build
if not exist build\classes mkdir build\classes

:: Compile JNI DLL
echo.
echo Compiling FastTheme JNI Bridge (C++)...
echo =====================================================
cl /LD /Fe:build\fasttheme.dll /Fo:build\ ^
    native\FastTheme.cpp ^
    user32.lib gdi32.lib shcore.lib advapi32.lib dwmapi.lib jawt.lib ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /EHsc /std:c++17 /O2 /W3 ^
    /link /LIBPATH:"%JAVA_HOME%\lib" /DEF:native\FastTheme.def

if %errorlevel% neq 0 (
    echo.
    echo =====================================================
    echo C++ COMPILATION FAILED
    echo =====================================================
    pause
    exit /b 1
)

:: Copy DLL to resources (so Maven can bundle it)
echo.
echo Copying DLL to resources...
if not exist src\main\resources\native mkdir src\main\resources\native
copy /Y build\fasttheme.dll src\main\resources\native\fasttheme.dll

:: Cleanup temporary native artifacts
del /Q build\*.obj
del /Q build\*.exp
del /Q build\*.lib

:: Compile Java and Install to Local Maven Repo
echo.
echo Compiling Java and Installing to Local Maven Repo...
call mvn install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo =====================================================
    echo MAVEN BUILD FAILED
    echo =====================================================
    echo Ensure Maven is installed and FastCore is accessible.
    pause
    exit /b 1
)

:: Success
echo.
echo =====================================================
echo BUILD SUCCESSFUL! (v0.1.0)
echo =====================================================
echo.
echo FastTheme JNI Bridge created with:
echo - Native Window Styling (Transparency, Colors)
echo - Windows 11 Immersive Dark Mode support
echo - Native HWND extraction via JAWT
echo.
echo Standard JAR: target/fasttheme-0.1.0.jar
echo Native DLL  : src/main/resources/native/fasttheme.dll
echo.
pause
