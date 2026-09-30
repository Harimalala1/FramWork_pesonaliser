# Explication du framework

## 1. Présentation générale

Ce projet est un petit framework MVC Java basé sur les Servlets Jakarta.

Son rôle est de :

1. Recevoir les requêtes HTTP avec un servlet central.
2. Chercher quelle méthode Java correspond à l'URL demandée.
3. Exécuter cette méthode.
4. Récupérer un `ModelAndView`.
5. Envoyer les données vers une page JSP.

Le parcours général est :

```text
Requête HTTP
    -> FrontServletController
    -> Recherche dans urlMappings
    -> Méthode Java du contrôleur
    -> ModelAndView
    -> Attributs de requête et page JSP
```

## 2. Les annotations

### `@Controller`

L'annotation `@Controller`, définie dans `src/main/java/definition/Controller.java`, sert à identifier une classe comme contrôleur :

```java
@Controller
public class AccueilController {
}
```

Elle possède la configuration suivante :

```java
@Retention(RetentionPolicy.RUNTIME)
```

Cela signifie qu'elle reste disponible pendant l'exécution du programme. Le framework peut donc vérifier dynamiquement si une classe possède cette annotation.

### `@UrlMapping`

L'annotation `@UrlMapping`, définie dans `src/main/java/definition/UrlMapping.java`, associe une méthode Java à une URL et à une méthode HTTP :

```java 
@UrlMapping(url = "accueil", methode = "GET")
public ModelAndView afficherAccueil() {
    return new ModelAndView("accueil");
}
```

Elle contient deux paramètres obligatoires :

```java
String url();
String methode();
```

La méthode HTTP est écrite manuellement, par exemple `GET` ou `POST`.

## 3. Le démarrage de l'application

`ListenerDemarrage`, dans `src/main/java/listener/ListenerDemarrage.java`, s'exécute lorsque l'application web démarre.

Il implémente :

```java
ServletContextListener
```

La méthode principale est :

```java
contextInitialized(...)
```

### Récupération du package

Le listener récupère le package à scanner depuis la configuration de l'application :

```java
String packageName = context.getInitParameter("packageName");
```

Exemple de configuration possible dans `web.xml` :

```xml
<context-param>
    <param-name>packageName</param-name>
    <param-value>mon.application.controller</param-value>
</context-param>
```

### Recherche des contrôleurs

Le framework cherche les classes possédant `@Controller` :

```java
classNameController =
    utilitaire.getAllClassesWithAnnotationInPackage(packageName, Controller.class);
```

La liste est ensuite stockée dans le contexte global :

```java
context.setAttribute("classNameController", classNameController);
```

Cette liste est actuellement peu utilisée. Elle servait surtout dans les anciens sprints à vérifier les contrôleurs trouvés.

### Construction des mappings

Le listener crée une table de correspondance :

```java
Map<UtilMethode, UrlMethode> urlMappings = new HashMap<>();
```

Cette table représente :

```text
(URL, méthode HTTP) -> classe Java et méthode Java
```

Exemple :

```text
("accueil", "GET") -> ("AccueilController", "afficherAccueil")
```

La table est ensuite enregistrée dans le contexte de l'application :

```java
context.setAttribute("urlMappings", urlMappings);
```

Ainsi, les classes ne sont pas rescannées à chaque requête.

### Préfixe et suffixe des vues

Le listener récupère également :

```java
String prefix = context.getInitParameter("prefix");
String suffix = context.getInitParameter("suffix");
```

Si ces paramètres ne sont pas définis, les valeurs par défaut sont :

```java
prefix = "/WEB-INF/template/";
suffix = ".jsp";
```

Donc :

```java
new ModelAndView("accueil")
```

correspond par défaut à la page :

```text
/WEB-INF/template/accueil.jsp
```

## 4. Le servlet frontal

`FrontServletController`, dans `src/main/java/controller/FrontServletController.java`, est le point d'entrée des requêtes.

Il hérite de :

```java
HttpServlet
```

Les méthodes `doGet` et `doPost` appellent toutes les deux :

```java
proccessRequest(req, res);
```

Le nom `proccessRequest` contient une faute de frappe, mais cela ne bloque pas le fonctionnement car toutes les utilisations utilisent le même nom.

### Récupération de l'URL

Le servlet récupère l'URL complète :

```java
String path = req.getRequestURI().toString();
```

Puis il enlève le chemin de contexte de l'application :

```java
String contextPath = req.getContextPath();
String chemin = path.substring(contextPath.length() + 1);
```

Par exemple, pour :

```text
http://localhost:8080/monapp/accueil
```

le chemin transmis au framework devient :

```text
accueil
```

### Recherche du mapping

Le servlet récupère la méthode HTTP :

```java
req.getMethod()
```

Puis il recherche la combinaison URL + méthode HTTP dans `urlMappings` :

```java
utilitaire.lireMethodeAndClass(
    chemin,
    req.getMethod(),
    packageName,
    urlMappings
);
```

Par exemple, il recherche :

```text
("accueil", "GET")
```

### Exécution avec la réflexion Java

Lorsque le mapping existe, `Utilitaire` :

1. Récupère le nom de la classe.
2. Charge la classe avec `Class.forName`.
3. Récupère la méthode avec `getMethod`.
4. Crée une instance avec le constructeur vide.
5. Exécute la méthode avec `invoke`.

La méthode du contrôleur doit donc actuellement être publique, ne recevoir aucun paramètre et posséder un constructeur sans argument.

## 5. `UtilMethode` et `UrlMethode`

### `UtilMethode`

`UtilMethode`, dans `src/main/java/service/UtilMethode.java`, représente la clé du mapping :

```java
new UtilMethode("accueil", "GET");
```

Elle contient :

```java
private String url;
private String methode;
```

Les méthodes `equals` et `hashCode` permettent de comparer correctement deux couples URL et méthode HTTP.

Elles sont nécessaires pour utiliser `UtilMethode` comme clé dans une `HashMap`.

### `UrlMethode`

`UrlMethode`, dans `src/main/java/service/UrlMethode.java`, représente la destination du mapping :

```java
new UrlMethode("AccueilController", "afficherAccueil");
```

Elle contient :

```java
private String className;
private String methodeName;
```

Le mapping complet est donc :

```text
UtilMethode("accueil", "GET")
        -> UrlMethode("AccueilController", "afficherAccueil")
```

## 6. Le chargement des classes

Dans `Utilitaire`, la méthode `getClassesParPackage` transforme un nom de package en chemin :

```text
mon.application.controller
```

devient :

```text
mon/application/controller
```

Le framework recherche ensuite les fichiers `.class` présents dans ce dossier et les charge avec :

```java
Class.forName(...)
```

Cette version fonctionne surtout lorsque les classes sont disponibles sous forme de fichiers dans un dossier. Elle peut être limitée lorsque les classes sont uniquement à l'intérieur d'un JAR.

## 7. La détection des mappings

La méthode `getAllUrlMappingsWithUtilMethode` parcourt les classes trouvées et leurs méthodes :

```java
for (Method methode : clazz.getDeclaredMethods())
```

Pour chaque méthode possédant `@UrlMapping`, elle crée :

```java
UtilMethode utilMethode =
    new UtilMethode(ann.url(), ann.methode());

UrlMethode urlMethode =
    new UrlMethode(clazz.getSimpleName(), methode.getName());
```

### Détection des doublons

Si deux méthodes utilisent exactement le même couple URL et méthode HTTP, le framework lève une exception.

Ceci est interdit :

```java
@UrlMapping(url = "test", methode = "GET")
public ModelAndView methode1() { ... }

@UrlMapping(url = "test", methode = "GET")
public ModelAndView methode2() { ... }
```

En revanche, ceci est autorisé :

```java
@UrlMapping(url = "test", methode = "GET")
public ModelAndView get() { ... }

@UrlMapping(url = "test", methode = "POST")
public ModelAndView post() { ... }
```

La même URL peut donc être utilisée avec des méthodes HTTP différentes.

## 8. `ModelAndView`

`ModelAndView`, dans `src/main/java/service/ModelAndView.java`, contient deux informations :

```java
private Map<String, Object> model;
private String viewName;
```

### Nom de la vue

```java
new ModelAndView("accueil")
```

Le framework cherchera :

```text
prefix + "accueil" + suffix
```

Avec les valeurs par défaut :

```text
/WEB-INF/template/accueil.jsp
```

### Données transmises à la vue

```java
ModelAndView result = new ModelAndView("accueil");
result.addObject("message", "Bienvenue");
return result;
```

La JSP pourra ensuite lire cette valeur :

```jsp
${message}
```

## 9. Affichage de la JSP

La méthode `trouverChemin` parcourt le modèle :

```java
request.setAttribute(entry.getKey(), entry.getValue());
```

Chaque élément du modèle devient donc un attribut de la requête.

Ensuite, le framework construit le chemin complet :

```java
String fullPath = prefix + mv.getViewName() + suffix;
```

Enfin, il transfère la requête vers la JSP :

```java
RequestDispatcher dispatcher = request.getRequestDispatcher(fullPath);
dispatcher.forward(request, response);
```

## 10. Les types de retour acceptés

Le framework accepte un retour de type `ModelAndView` :

```java
return new ModelAndView("accueil");
```

Il accepte également une simple chaîne :

```java
return "accueil";
```

Dans ce deuxième cas, le servlet transforme la chaîne en :

```java
new ModelAndView((String) result)
```

Tout autre type de retour provoque une erreur :

```text
Type de retour non supporté
```

## 11. Exemple complet

```java
package mon.application;

import definition.Controller;
import definition.UrlMapping;
import service.ModelAndView;

@Controller
public class AccueilController {

    @UrlMapping(url = "accueil", methode = "GET")
    public ModelAndView afficherAccueil() {
        ModelAndView result = new ModelAndView("accueil");
        result.addObject("message", "Bienvenue");
        return result;
    }
}
```

Pour la requête :

```text
GET /monapp/accueil
```

le framework effectue :

```text
("accueil", "GET")
    -> AccueilController.afficherAccueil()
    -> ModelAndView("accueil")
    -> /WEB-INF/template/accueil.jsp
```

## 12. Explication des commentaires actuels

Dans `FrontServletController`, le bloc suivant est commenté :

```java
// private List<String> classNameController;
// public void init() throws ServletException {
//     String packageName = this.getInitParameter("packageName");
//     classNameController =
//         utilitaire.getAllClassesWithAnnotationInPackage(packageName,
//             Controller.class);
// }
```

C'était une ancienne approche du sprint 1. Le servlet devait scanner les contrôleurs dans sa propre méthode `init` au démarrage.

L'architecture actuelle utilise plutôt `ListenerDemarrage`, qui effectue cette initialisation au démarrage de l'application.

Un autre bloc commenté affichait les contrôleurs trouvés :

```java
// for (String className : classNameController) {
//     out.println("Class : " + className);
// }
```

Il servait principalement à vérifier que le scan fonctionnait.

Le bloc qui récupère `classNameController` depuis le contexte servait également à afficher les classes trouvées par le listener. C'était du code de test et de débogage.

Le commentaire `// sprint0` rappelle que `doGet` et `doPost` appartiennent à la première étape du projet : faire passer les requêtes par un servlet frontal.

## 13. Évolution indiquée dans le README

Le fichier `README.md` décrit l'évolution du framework :

- Sprint 0 : création du servlet frontal.
- Sprint 1 : recherche des classes au démarrage.
- Sprint 2 : annotation des méthodes avec une URL.
- Sprint 3 : détection des URLs en double.
- Sprint 4 : intégration avec Tomcat.
- Sprint 5 : ajout du préfixe et du suffixe des vues.

La méthode `getAllUrlMappings` est probablement une ancienne version. Elle utilise seulement l'URL comme clé :

```java
Map<String, UrlMethode>
```

Elle ne distingue donc pas `GET` et `POST` pour une même URL.

La méthode actuellement utilisée est la version plus complète :

```java
Map<UtilMethode, UrlMethode>
```

## 14. Limites actuelles

Le framework ne gère pas encore :

- les paramètres dans les méthodes contrôleur ;
- les paramètres d'URL comme `/user/{id}` ;
- l'injection de `HttpServletRequest` ou `HttpServletResponse` ;
- les méthodes privées ;
- les contrôleurs sans constructeur vide ;
- les erreurs HTTP propres comme `404` ou `500` ;
- le filtrage des mappings uniquement sur les classes `@Controller` ;
- le scan complet des classes contenues dans des JAR ;
- le retour `null` ;
- la validation du préfixe et du suffixe.

Un point important est que `getAllUrlMappingsWithUtilMethode` scanne actuellement les méthodes de toutes les classes du package, même si la classe ne possède pas `@Controller`. L'annotation `@Controller` est détectée et stockée, mais elle ne sert pas encore directement à filtrer les mappings.
