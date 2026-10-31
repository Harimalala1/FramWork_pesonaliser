package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import definition.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ModelAndView;
import service.Niveau;
import service.UrlMethode;
import service.UtilMethode;
import service.Utilitaire;
import jakarta.servlet.annotation.MultipartConfig;

// import java.util.HashMap;
// import jakarta.servlet.ServletContext;

import definition.Auth;
import definition.Role;
import java.lang.reflect.Method;
import jakarta.servlet.http.HttpSession;

import singleton.ControllerSingleton;
// @WebServlet(urlPatterns = "/", loadOnStartup = 1)
@MultipartConfig
public class FrontServletController extends HttpServlet {

    private final Utilitaire utilitaire = new Utilitaire();
    private List<String> listController;
    //sprint10
    private String sessionAuthName;
    // Sprint 10 bis
    private String sessionRoleName;

    // Sprint 1 : liste des classes annotées @Controller
    @Override
    public void init() throws ServletException {
        String packageName = (String) getServletContext().getAttribute("packageController");

        //sprint10
        sessionAuthName = getServletConfig().getInitParameter("sessionAuthName");
        if (sessionAuthName == null || sessionAuthName.trim().isEmpty()) {
            throw new ServletException(
                "Le paramètre sessionAuthName est absent de web.xml"
            );
        }

        //bis : sessionRoleName
        sessionRoleName = getServletConfig().getInitParameter("sessionRoleName");
        if (sessionRoleName == null || sessionRoleName.trim().isEmpty()) {
            throw new ServletException(
                "Le paramètre sessionRoleName est absent de web.xml"
            );
        }
        //---
        try {
            listController = utilitaire.getAllClassesWithAnnotationInPackage(
                    packageName, Controller.class, Niveau.CLASSE);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Sprint 9A :
     * Récupère l'instance existante d'un contrôleur.
     * Si elle n'existe pas,on la crée et on la conserve dans le ServletContext.
     */
    // @SuppressWarnings("unchecked")
    // private Object getControllerInstance(String className) throws Exception {

    //     ServletContext context = getServletContext();

    //     Map<String, Object> controllerInstances =
    //             (Map<String, Object>) context.getAttribute("controllerInstances");

    //     if (controllerInstances == null) {
    //         throw new IllegalStateException(
    //                 "La Map controllerInstances n'est pas initialisée.");
    //     }

    //     // Une seule instance doit être créée par classe de contrôleur.
    //     synchronized (controllerInstances) {

    //         Object instance = controllerInstances.get(className);

    //         if (instance == null) {
    //             Class<?> controllerClass = Class.forName(className);

    //             instance = controllerClass
    //                     .getDeclaredConstructor()
    //                     .newInstance();

    //             controllerInstances.put(className, instance);
    //         }

    //         return instance;
    //     }
    // }


    @SuppressWarnings("unchecked")
    private void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ex : "/emp/list" (avec le "/" initial, comme dans @UrlMapping)
        String chemin = req.getRequestURI().substring(req.getContextPath().length());
        if (chemin.isEmpty()) {
            chemin = "/";
        }

        Map<UtilMethode, UrlMethode> urlMappings = (Map<UtilMethode, UrlMethode>) getServletContext()
                .getAttribute("urlMappings");
        String prefix = (String) getServletContext().getAttribute("prefix");
        String suffix = (String) getServletContext().getAttribute("suffix");

        try {
            UrlMethode urlMethode = utilitaire.trouverUrlMethode(chemin, req.getMethod(), urlMappings);

            // Récupérer la session existante une seule fois
            HttpSession session = req.getSession(false);

            // Sprint 10 : contrôle d'authentification
            if (verifierAuth(urlMethode)) {

                boolean authentifie = session != null
                    && Boolean.TRUE.equals(session.getAttribute(sessionAuthName));

                if (!authentifie) {
                    res.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Vous devez être authentifié pour accéder à cette page."
                    );
                    return;
                }
            }

            // Sprint 10 bis : contrôle des rôles
            String[] rolesAutorises = getRolesAutorises(urlMethode);

            if (rolesAutorises != null) {

                // Vérifier la configuration
                if (sessionRoleName == null || sessionRoleName.trim().isEmpty()) {
                    throw new ServletException(
                        "Le paramètre sessionRoleName est absent de web.xml"
                    );
                }

                // Récupérer le profil de l'utilisateur
                Object profil = session == null
                    ? null
                    : session.getAttribute(sessionRoleName);

                boolean roleAutorise = false;

                // Vérifier si le profil correspond à un rôle autorisé
                if (profil instanceof String) {

                    for (String role : rolesAutorises) {

                        if (role.equals(profil)) {
                            roleAutorise = true;
                            break;
                        }
                    }
                }

                // Refuser l'accès si aucun rôle ne correspond
                if (!roleAutorise) {
                    res.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Vous n'avez pas le rôle nécessaire pour accéder à cette page."
                    );
                    return;
                }
            }

            // Object result = utilitaire.lireMethodeAndClass(urlMethode);
            // Object result = utilitaire.lireMethodeAndClass(urlMethode, req);
            // Sprint 9A : récupérer l'instance partagée du contrôleur avant, maintenant Sprint 11 : récupérer l'instance unique du contrôleur via Singleton
            Object controller = ControllerSingleton.getInstance().getController(urlMethode.getClassName());
            // Exécuter l'action sur cette instance
            Object result = utilitaire.lireMethodeAndClass(urlMethode, req, controller);

            // Sprint 6 : on teste @ApiRest AVANT le dispatch
            if (urlMethode.isApiRest()) {
                res.setContentType("application/json;charset=UTF-8");
                PrintWriter out = res.getWriter();
                if (result instanceof String) {
                    out.print(result);                      // le developpeur a deja fait le JSON
                } else {
                    out.print(utilitaire.toJson(result));   // le framework transforme en JSON
                }
                out.flush();
                return;
            }

            // Sinon : dispatch vers la vue
            if (result instanceof ModelAndView) {
                utilitaire.trouverChemin((ModelAndView) result, req, res, prefix, suffix);
            } else if (result instanceof String) {
                utilitaire.trouverChemin(new ModelAndView((String) result), req, res, prefix, suffix);
            } else {
                // Sprint 3 bis : on affiche simplement que la méthode a été appelée
                res.setContentType("text/plain;charset=UTF-8");
                res.getWriter().println("Methode appelee pour '" + chemin + "' [" + req.getMethod() + "]"
                        + (result != null ? " -> " + result : ""));
            }
        } catch (FileNotFoundException e) {
            // Sprint 2 : 404 + liste des urls disponibles
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.println(e.getMessage());
            out.println("Controllers : " + listController);
        } catch (Exception e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
        }
    }


    /// Sprint 10 : vérifier si une méthode nécessite une authentification (annotation @Auth)
    private boolean verifierAuth(UrlMethode urlMethode) throws Exception {

        Class<?> classe = Class.forName(urlMethode.getClassName());

        Method[] methodes = classe.getDeclaredMethods();

        boolean methodeTrouvee = false;

        for (Method methode : methodes) {

            if (methode.getName().equals(urlMethode.getMethodeName())) {

                methodeTrouvee = true;

                if (methode.isAnnotationPresent(Auth.class)) {
                    return true;
                }
            }
        }

        if (!methodeTrouvee) {
            throw new NoSuchMethodException(
                "Méthode introuvable : " + urlMethode.getMethodeName()
            );
        }

        return false;
    }

    // Sprint 10 bis : récupérer les rôles autorisés d'une méthode
    private String[] getRolesAutorises(UrlMethode urlMethode) throws Exception {

        Class<?> classe = Class.forName(urlMethode.getClassName());

        Method[] methodes = classe.getDeclaredMethods();

        for (Method methode : methodes) {

            if (methode.getName().equals(urlMethode.getMethodeName())) {

                Role annotationRole = methode.getAnnotation(Role.class);

                // Si la méthode n'a pas @Role, elle reste accessible
                if (annotationRole == null) {
                    return null;
                }

                // La méthode possède @Role : récupérer les rôles autorisés
                return annotationRole.value();
            }
        }

        throw new NoSuchMethodException(
            "Méthode introuvable : " + urlMethode.getMethodeName()
        );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }
}