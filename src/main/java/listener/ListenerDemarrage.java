
package listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import service.UrlMethode;
import service.UtilMethode;
import service.Utilitaire;

@WebListener
public class ListenerDemarrage implements ServletContextListener {

    // Package où se trouvent les controllers (à adapter à ton projet)
    private static final String PACKAGE_CONTROLLER = "developpeur";

    // Nom de l'attribut qui contiendra les instances des contrôleurs
    public static final String CONTROLLER_INSTANCES = "controllerInstances";

    private final Utilitaire utilitaire = new Utilitaire();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();

        String packageName = PACKAGE_CONTROLLER;
        context.setAttribute("packageController", packageName);

        // Sprint 4 : découverte et enregistrement des URL
        Map<UtilMethode, UrlMethode> urlMappings = new HashMap<>();

        try {
            utilitaire.getAllUrlMappingsWithUtilMethode(
                    packageName, urlMappings);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur d'initialisation : urlMappings non initialise. Cause : "
                            + e.getMessage(),
                    e);
        }

        context.setAttribute("urlMappings", urlMappings);

        // Sprint 5 : préfixe et suffixe de la vue
        String prefix = context.getInitParameter("prefix");
        String suffix = context.getInitParameter("suffix");

        context.setAttribute(
                "prefix",
                prefix != null ? prefix : "/WEB-INF/template/");

        context.setAttribute(
                "suffix",
                suffix != null ? suffix : ".jsp");

        // Sprint 9A : stockage partagé des instances des contrôleurs
        Map<String, Object> controllerInstances = new HashMap<>();
        context.setAttribute(CONTROLLER_INSTANCES, controllerInstances);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
