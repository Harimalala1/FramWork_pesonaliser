@echo off
REM Compilation du framework -> FrontServletController.jar
REM A lancer depuis la racine du projet (le dossier qui contient "src" et "lib")

echo Nettoyage...
if exist controller rmdir /s /q controller
if exist definition rmdir /s /q definition
if exist service rmdir /s /q service
if exist listener rmdir /s /q listener
if exist FrontServletController.jar del FrontServletController.jar

echo Compilation...
javac -cp "lib/servlet-api.jar" -d . src/main/java/definition/*.java src/main/java/service/*.java src/main/java/listener/*.java src/main/java/controller/*.java
if errorlevel 1 (
    echo.
    echo ERREUR : la compilation a echoue.
    pause
    exit /b 1
)

echo Creation du jar...
jar -cvf FrontServletController.jar controller definition service listener
if errorlevel 1 (
    echo.
    echo ERREUR : la creation du jar a echoue.
    pause
    exit /b 1
)

echo.
echo Termine : FrontServletController.jar
pause