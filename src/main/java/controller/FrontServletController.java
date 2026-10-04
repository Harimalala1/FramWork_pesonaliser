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

@WebServlet(urlPatterns = "/", loadOnStartup = 1)
public class FrontServletController extends HttpServlet {

    private final Utilitaire utilitaire = new Utilitaire();
    private List<String> listController;

    // Sprint 1 : liste des classes annotées @Controller
    @Override
    public void init() throws ServletException {
        String packageName = (String) getServletContext().getAttribute("packageController");
        try {
            listController = utilitaire.getAllClassesWithAnnotationInPackage(
                    packageName, Controller.class, Niveau.CLASSE);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

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
            Object result = utilitaire.lireMethodeAndClass(urlMethode);

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }
}