@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⚡ Building Project...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark failed. & pause & exit /b %ERRORLEVEL% )


echo 🚀 Running Demo...
cd examples
call mvn compile exec:java -Dexec.mainClass="fasttheme.Demo" -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Benchmark failed. & pause & exit /b %ERRORLEVEL% )

cd ..
