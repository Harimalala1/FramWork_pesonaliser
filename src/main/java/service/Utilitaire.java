package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import definition.ApiRest;
import definition.UrlMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Utilitaire {

    // ===================== Sprint 1 =====================
    // Retourne les classes du package qui ont l'annotation au niveau demandé
    public List<String> getAllClassesWithAnnotationInPackage(String packageName,
            Class<? extends Annotation> annotationClass, Niveau niveau) throws Exception {
        List<String> classNames = new ArrayList<>();

        for (Class<?> clazz : getClassesParPackage(packageName)) {
            boolean trouve = false;
            if (niveau == Niveau.CLASSE) {
                trouve = clazz.isAnnotationPresent(annotationClass);
            } else if (niveau == Niveau.METHODE) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(annotationClass)) {
                        trouve = true;
                        break;
                    }
                }
            } else if (niveau == Niveau.VARIABLE) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.isAnnotationPresent(annotationClass)) {
                        trouve = true;
                        break;
                    }
                }
            }
            if (trouve) {
                classNames.add(clazz.getName());
            }
        }
        return classNames;
    }

    public static List<Class<?>> getClassesParPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String chemin = packageName.replace(".", "/");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<java.net.URL> ressources = classLoader.getResources(chemin);

        while (ressources.hasMoreElements()) {
            File dossier = new File(ressources.nextElement().toURI());
            File[] fichiers = dossier.listFiles();
            if (fichiers == null) {
                continue;
            }
            for (File fichier : fichiers) {
                if (fichier.getName().endsWith(".class")) {
                    String nomClasse = fichier.getName().replace(".class", "");
                    classes.add(Class.forName(packageName + "." + nomClasse));
                }
            }
        }
        return classes;
    }

    // ===================== Sprint 3 / 4 =====================
    // Remplit la map au démarrage (void, la map est passée en paramètre)
    public void getAllUrlMappingsWithUtilMethode(String packageName, Map<UtilMethode, UrlMethode> urlMappings)
            throws Exception {

        for (Class<?> clazz : getClassesParPackage(packageName)) {
            for (Method methode : clazz.getDeclaredMethods()) {
                if (!methode.isAnnotationPresent(UrlMapping.class)) {
                    continue;
                }
                UrlMapping ann = methode.getAnnotation(UrlMapping.class);
                UtilMethode cle = new UtilMethode(ann.url(), ann.methode());

                // equals() surdéfini : détecte les doublons url + methode
                if (urlMappings.containsKey(cle)) {
                    UrlMethode existant = urlMappings.get(cle);
                    throw new Exception("Erreur : l'url '" + cle.getUrl() + "' avec la methode '"
                            + cle.getMethode() + "' existe deja dans la classe '"
                            + existant.getClassName() + "' et la methode '"
                            + existant.getMethodeName() + "'");
                }
                // Sprint 6 : @ApiRest(json = true) => reponse JSON, sans vue
                ApiRest api = methode.getAnnotation(ApiRest.class);
                boolean apiRest = api != null && api.json();

                urlMappings.put(cle, new UrlMethode(clazz.getName(), methode.getName(), apiRest));
            }
        }
    }

    // ===================== Sprint 2 / 3 bis / 4 =====================
    // Trouve l'UrlMethode liée à l'url + méthode HTTP (404 si introuvable)
    public UrlMethode trouverUrlMethode(String url, String httpMethode,
            Map<UtilMethode, UrlMethode> urlMappings) throws Exception {

        if (urlMappings == null) {
            throw new Exception("Erreur : urlMappings est null. Verifiez que ListenerDemarrage est declare.");
        }

        UrlMethode urlMethode = urlMappings.get(new UtilMethode(url, httpMethode));
        if (urlMethode == null) {
            // Sprint 2 : url non trouvée => 404 + liste de toutes les urls disponibles
            throw new FileNotFoundException("Erreur 404 : '" + url + "' [" + httpMethode
                    + "] non trouvee. URLs disponibles : " + listerUrls(urlMappings));
        }
        return urlMethode;
    }

    // Invoque la méthode et retourne sa valeur de retour 
    //ancien lireMethodeAndClass sans paramètre HttpServletRequest
    //
    // public Object lireMethodeAndClass(UrlMethode urlMethode) throws Exception {
    //     Class<?> classMethod = Class.forName(urlMethode.getClassName());
    //     Method methode = classMethod.getMethod(urlMethode.getMethodeName());
    //     Object instance = classMethod.getDeclaredConstructor().newInstance();
    //     return methode.invoke(instance);
    // }

    
    public Object lireMethodeAndClass(
        UrlMethode urlMethode,
        HttpServletRequest req) throws Exception {

        Class<?> classMethod =
                Class.forName(urlMethode.getClassName());

        Object instance = classMethod.getDeclaredConstructor().newInstance();

        // Récupérer les paramètres de la méthode
        Method methode = null;

        for (Method m : classMethod.getMethods()) {
            if (m.getName().equals(urlMethode.getMethodeName())) {
                methode = m;
                break;
            }
        }
        if (methode == null) {
            throw new NoSuchMethodException(urlMethode.getMethodeName());
        }
            // Préparer les arguments
            Class<?>[] types = methode.getParameterTypes();
            Object[] arguments = new Object[types.length];

            for (int i = 0; i < types.length; i++) {
                // String nomParametre = methode.getParameters()[i].getName();
                // String valeur = req.getParameter(nomParametre);

    
            String nomParametre = methode.getParameters()[i].getName();
            String valeur = req.getParameter(nomParametre);
            // if (valeur == null) {
            //     throw new IllegalArgumentException(
            //         "Paramètre absent du formulaire : " + nomParametre
            //     );
            // }
                if (types[i] == String.class) {
                    if (valeur == null) {
                        throw new IllegalArgumentException(
                            "Paramètre absent du formulaire : " + nomParametre
                        );
                    }
                    arguments[i] = valeur;

                } else if (types[i] == int.class
                        || types[i] == Integer.class) {
                    if (valeur == null) {
                        throw new IllegalArgumentException(
                            "Paramètre absent du formulaire : " + nomParametre
                        );
                    }
                    arguments[i] = Integer.parseInt(valeur);

                } else {
                    arguments[i] = remplirObjet(types[i], req);
                }
                
                if (types[i] == String.class) {
                    arguments[i] = valeur;
                } else if (types[i] == int.class
                        || types[i] == Integer.class) {
                    arguments[i] = Integer.parseInt(valeur);
                } else {
                    // throw new IllegalArgumentException(
                    //         "Type non supporté : " + types[i]);
                        // Sprint 7 bis : binding vers un objet Java
                        arguments[i] = remplirObjet(types[i], req);
                }
            }
        return methode.invoke(instance, arguments);
    }
    //sprint 7 bis : argument mivadika objet exemple(eleve) pour la clase developpeur
    private Object remplirObjet(Class<?> classe, HttpServletRequest req)
            throws Exception {

        Object objet = classe.getDeclaredConstructor().newInstance();

        for (java.lang.reflect.Field champ : classe.getDeclaredFields()) {
            String nom = champ.getName();
            String valeur = req.getParameter(nom);

            if (valeur == null) {
                continue;
            }

            champ.setAccessible(true);

            if (champ.getType() == String.class) {
                champ.set(objet, valeur);
            } else if (champ.getType() == int.class
                    || champ.getType() == Integer.class) {
                champ.set(objet, Integer.parseInt(valeur));
            } else {
                throw new IllegalArgumentException(
                    "Type d'attribut non supporté : " + champ.getType()
                );
            }
        }

        return objet;
    }


    public List<String> listerUrls(Map<UtilMethode, UrlMethode> urlMappings) {
        List<String> urls = new ArrayList<>();
        for (UtilMethode u : urlMappings.keySet()) {
            urls.add("[" + u.getUrl() + ", " + u.getMethode() + "]");
        }
        return urls;
    }

    // ===================== Sprint 5 =====================
    // Envoie le model dans la requête puis forward vers la vue (prefix + vue + suffix)
    public void trouverChemin(ModelAndView mv, HttpServletRequest request,
            HttpServletResponse response, String prefix, String suffix)
            throws ServletException, IOException {

        for (Map.Entry<String, Object> entry : mv.getModel().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        String fullPath = prefix + mv.getViewName() + suffix;
        RequestDispatcher dispatcher = request.getRequestDispatcher(fullPath);
        dispatcher.forward(request, response);
    }

    // ===================== Sprint 6 : objet -> JSON =====================
    public String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String || value instanceof Character || value instanceof Enum) {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append(toJson(String.valueOf(entry.getKey())));
                json.append(":");
                json.append(toJson(entry.getValue()));
                first = false;
            }
            return json.append("}").toString();
        }
        if (value instanceof Iterable) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Object element : (Iterable<?>) value) {
                if (!first) {
                    json.append(",");
                }
                json.append(toJson(element));
                first = false;
            }
            return json.append("]").toString();
        }
        if (value.getClass().isArray()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < Array.getLength(value); i++) {
                if (i > 0) {
                    json.append(",");
                }
                json.append(toJson(Array.get(value, i)));
            }
            return json.append("]").toString();
        }
        return objetToJson(value);
    }

    // Objet du développeur : on lit ses attributs (nom -> valeur)
    private String objetToJson(Object obj) {
        Class<?> clazz = obj.getClass();
        if (clazz.getName().startsWith("java.")) {
            // Date, LocalDate, ... : simple texte
            return toJson(obj.toString());
        }
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        try {
            for (Field champ : clazz.getDeclaredFields()) {
                int mod = champ.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || champ.isSynthetic()) {
                    continue;
                }
                champ.setAccessible(true);
                if (!first) {
                    json.append(",");
                }
                json.append(toJson(champ.getName()));
                json.append(":");
                json.append(toJson(champ.get(obj)));
                first = false;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Erreur toJson : " + e.getMessage(), e);
        }
        return json.append("}").toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}