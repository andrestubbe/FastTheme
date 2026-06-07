@echo off
chcp 65001 >nul
cd /d "%~dp0"
setlocal

echo ⚡ Building Project...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark failed. & pause & exit /b %ERRORLEVEL% )


echo 🚀 Running Demo...
cd examples
call mvn -q compile exec:java -Dexec.mainClass="fasttheme.Demo2" -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark failed. & pause & exit /b %ERRORLEVEL% )

cd ..
endlocal
