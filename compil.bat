@echo off
REM Compile le framework pour Tomcat 8.5 (javax.servlet) SANS modifier tes sources.
REM Les sources restent en jakarta.servlet : une copie temporaire est convertie en javax.servlet.
REM A lancer depuis la racine du projet du framework (le dossier qui contient "src").

set TOMCAT_API=D:\xampp\tomcat\lib\servlet-api.jar

if not exist "%TOMCAT_API%" (
    echo ERREUR : %TOMCAT_API% introuvable
    pause
    exit /b 1
)

echo Nettoyage...
if exist tmp-javax rmdir /s /q tmp-javax
if exist tmp-classes rmdir /s /q tmp-classes
if exist FrontServletController.jar del FrontServletController.jar

echo Conversion jakarta -^> javax dans une copie temporaire...
xcopy src\main\java tmp-javax /e /i /q >nul
powershell -NoProfile -Command "Get-ChildItem tmp-javax -Recurse -Filter *.java | ForEach-Object { [IO.File]::WriteAllText($_.FullName, ([IO.File]::ReadAllText($_.FullName) -replace 'jakarta\.servlet','javax.servlet')) }"

echo Compilation...
mkdir tmp-classes
dir /s /b tmp-javax\*.java > sources.txt
javac -encoding UTF-8 -cp "%TOMCAT_API%" -d tmp-classes @sources.txt
set ERR=%errorlevel%
del sources.txt
if not "%ERR%"=="0" (
    echo.
    echo ERREUR : la compilation a echoue.
    pause
    exit /b 1
)

echo Creation du jar...
jar -cvf FrontServletController.jar -C tmp-classes .

rmdir /s /q tmp-javax
rmdir /s /q tmp-classes

echo.
echo Termine : FrontServletController.jar ^(version Tomcat 8.5 / javax^)
pause