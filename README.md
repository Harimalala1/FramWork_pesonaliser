# FramWork_pesonaliser 

## Etape 1: Sprint 0
Front Controlleur Servelet
On vas creer une servelt qu'on vas transformer en .jar
On vas obliger tout les requets a passer par ce servelt

Qu'importe les URL dans le projet web elle doit tomber sur 
Procesucerequest() -> outprint(URL)

Commenent
Declarer le .jar du servelt dans web.xml
Il faut faire en sorte que le sripte change en .jar  

Doget->processerequest 

## Etape 2 
Chargemenet de classe 
au demarage de l'apli web on va mettre une methode (optionnel)

Pour le premier appel du methode servlet : on le met dans le methode init() (ou on utilise param)  ou on utilise le context listner . Mais pour l'instant on utilise init
elle parcour tout les class du class path 

- Creer une class anotation mg.etu.anotation.controleur qui agit avec les class et puis dans init on ajoute une code qui parcour tout les class dans le class path de l'appli web  

Dans init on ajoute une liste de string 
dans processrequest on fais un bouclet du tableau et apres offcontroleeur

on cheerche ou sont tout les paqueget ou il y a les controleurs

## Etape 3

Tsy mila url associer @ class fa methode associer @ url
Rah manana clas dep.controleur anoter @ anotation 

cote fram work micreer class d'anotation 

rah misy anotation de alaina ny url 

Mandalo ao @ processs request daholy ohany ny url fa jerena hoe supporter sa tsy supportter le izy -> refa supporter de affichena daholy izy rehetra