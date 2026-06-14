#!/bin/bash

# Définition des variables
APP_NAME="FramWorkServeur"
SRC_DIR="src/java"
BUILD_DIR="build"
LIB_DIR="lib"
# Note : Le dossier TOMCAT_WEBAPPS n'est plus nécessaire pour un fichier .jar
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"

# Nettoyage et création du répertoire temporaire de build
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

# Compilation des fichiers Java
find $SRC_DIR -name "*.java" > sources.txt
# On compile directement les classes à la racine du dossier build
javac -cp $SERVLET_API_JAR -d $BUILD_DIR @sources.txt
rm sources.txt

# Générer le fichier .jar dans le dossier build
# (On n'inclut plus les fichiers de src/main/webapp car un .jar standard ne les gère pas ainsi)
cd $BUILD_DIR || exit
jar -cvf ../$APP_NAME.jar *
cd ..

# Nettoyage du dossier temporaire de build (optionnel)
rm -rf $BUILD_DIR

echo ""
echo "Compilation terminée. Le fichier $APP_NAME.jar a été généré à la racine."
echo ""