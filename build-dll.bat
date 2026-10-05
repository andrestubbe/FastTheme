@echo off
setlocal EnableDelayedExpansion

set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"

for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
    set "VS_INSTALL=%%i"
)

call "%VS_INSTALL%\VC\Auxiliary\Build\vcvars64.bat"

if not exist build mkdir build

cl /LD /Fe:build\fasttheme.dll /Fo:build\ ^
    native\FastTheme.cpp ^
    user32.lib gdi32.lib shcore.lib advapi32.lib dwmapi.lib jawt.lib ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /EHsc /std:c++17 /O2 /W3 ^
    /link /LIBPATH:"%JAVA_HOME%\lib" /DEF:native\FastTheme.def

if errorlevel 1 exit /b 1

if not exist "release" mkdir release
if not exist "src\main\resources\native" mkdir "src\main\resources\native"
if not exist "src\main\resources\win32-x86-64" mkdir "src\main\resources\win32-x86-64"
if not exist "target\classes\native" mkdir "target\classes\native"
set "FASTCORE_DIR=%USERPROFILE%\.fastcore\native\fasttheme"
if not exist "!FASTCORE_DIR!" mkdir "!FASTCORE_DIR!"

copy /Y build\fasttheme.dll release\fasttheme.dll >nul
copy /Y build\fasttheme.dll src\main\resources\fasttheme.dll >nul
copy /Y build\fasttheme.dll src\main\resources\native\fasttheme.dll >nul
copy /Y build\fasttheme.dll src\main\resources\win32-x86-64\fasttheme.dll >nul
copy /Y build\fasttheme.dll target\classes\native\fasttheme.dll >nul 2>&1
copy /Y build\fasttheme.dll target\classes\fasttheme.dll >nul 2>&1
copy /Y build\fasttheme.dll "!FASTCORE_DIR!\fasttheme.dll" >nul

del /Q build\*.obj build\*.exp build\*.lib >nul 2>&1
echo Native FastTheme DLL successfully compiled!
