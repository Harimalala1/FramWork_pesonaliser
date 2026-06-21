@echo off
setlocal

set "APP_NAME=framework"
set "VERSION=1.0"
set "BUILD_DIR=build"
set "OUTPUT_JAR=%BUILD_DIR%\framWorkPerso.jar"

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

call mvn clean package -DskipTests
if errorlevel 1 exit /b 1

if exist "%OUTPUT_JAR%" del /f /q "%OUTPUT_JAR%"

copy /y "target\%APP_NAME%-%VERSION%.jar" "%OUTPUT_JAR%"
if errorlevel 1 exit /b 1

echo.
echo JAR cree avec succes: %OUTPUT_JAR%
echo.
pause