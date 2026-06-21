@echo off
:: Force Windows à trouver le JDK 21 (indispensable pour javac et jar)
SET PATH=D:\jdk21\bin;%PATH%

:: Définition des variables
set APP_NAME=framWorkPerso
set SRC_DIR=src\main\java
set WEB_DIR=src\main\webapp
set BUILD_DIR=build
set LIB_DIR=lib
set TOMCAT_WEBAPPS=D:\xampp\tomcat\webapps
set SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar

:: Nettoyage et création des répertoires temporaires
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
mkdir %BUILD_DIR%\WEB-INF\classes

:: Compilation des fichiers Java
dir /s /B %SRC_DIR%\*.java > sources.txt
javac -cp "%SERVLET_API_JAR%" -d %BUILD_DIR%\WEB-INF\classes @sources.txt
del sources.txt

:: Copier les fichiers web (web.xml, JSP, etc.)
xcopy /E /I /Y %WEB_DIR%\* %BUILD_DIR%\

:: Générer le fichier .war dans le dossier build
cd %BUILD_DIR%
jar -cvf %APP_NAME%.war *
cd ..

:: Déploiement dans Tomcat
copy /Y %BUILD_DIR%\%APP_NAME%.war "%TOMCAT_WEBAPPS%\"

echo.
echo Deploiement termine. Redemarrez Tomcat si necessaire.
echo.
pause