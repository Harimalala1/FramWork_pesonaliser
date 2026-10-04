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

    private final Utilitaire utilitaire = new Utilitaire();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        String packageName = PACKAGE_CONTROLLER;
        context.setAttribute("packageController", packageName);

        // Sprint 4 : si une exception survient, l'application ne démarre pas
        Map<UtilMethode, UrlMethode> urlMappings = new HashMap<>();
        try {
            utilitaire.getAllUrlMappingsWithUtilMethode(packageName, urlMappings);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur d'initialisation : urlMappings non initialise. Cause : " + e.getMessage(), e);
        }
        context.setAttribute("urlMappings", urlMappings);

        // Sprint 5 : prefixe et suffixe de la vue
        String prefix = context.getInitParameter("prefix");
        String suffix = context.getInitParameter("suffix");
        context.setAttribute("prefix", prefix != null ? prefix : "/WEB-INF/template/");
        context.setAttribute("suffix", suffix != null ? suffix : ".jsp");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}