package controller;

import java.io.*;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import service.HandlerResult;
import service.ModelAndView;
import service.Utilitaire;
import definition.*;

@Controller
public class FrontServletController extends HttpServlet {
    //pas utiliser pour l'instant
    // private List<String> classNameController;
    // public void init() throws ServletException {
    // String packageName = this.getInitParameter("packageName");

    // try {
    // classNameController =
    // utilitaire.getAllClassesWithAnnotationInPackage(packageName,
    // Controller.class);
    // } catch (Exception e) {
    // throw new ServletException(e);
    // }
    // }

    private Utilitaire utilitaire = new Utilitaire();

    public void proccessRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
        res.setContentType("text/plain;charset=UTF-8");

        String path = req.getRequestURI().toString();
        PrintWriter out = res.getWriter();

        String contextPath = req.getContextPath();
        String chemin = path.substring(contextPath.length() + 1);

        String packageName = this.getInitParameter("packageName");

        Map<service.UtilMethode, service.UrlMethode> urlMappings = (Map<service.UtilMethode, service.UrlMethode>) getServletContext()
                .getAttribute("urlMappings");
        if (urlMappings == null) {
            throw new Exception("Erreur : urlMappings est null...");
        }
        String prefix = (String) getServletContext().getAttribute("prefix");
        String suffix = (String) getServletContext().getAttribute("suffix");

        try {
            // Object result = utilitaire.lireMethodeAndClass(...);
            HandlerResult handlerResult = (HandlerResult) utilitaire.lireMethodeAndClass(
                    chemin,
                    req.getMethod(),
                    packageName,
                    urlMappings);

            Object result = handlerResult.getValue();

            if (handlerResult.isWebApi()) {
                res.setContentType("application/json;charset=UTF-8");
                out.println(toJson(result));
                return;
            }

            ModelAndView mv;
            if (result instanceof ModelAndView) {
                mv = (ModelAndView) result;
            } else if (result instanceof String) {
                mv = new ModelAndView((String) result);
            } else {
                throw new ServletException("Type de retour non supporté : " + result);
            }

            utilitaire.trouverChemin(mv, req, res, prefix, suffix);
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Resultat de l'url : " + e.getMessage());
        }

        // for (String className : classNameController) {
        // out.println("Class : " + className);
        // }

        // List<String> classNameControllerFromListener = (List<String>)
        // getServletContext()
        // .getAttribute("classNameController");

        // if (classNameControllerFromListener == null) {
        // out.println("Class from listener is null");
        // } else {
        // for (String className : classNameControllerFromListener) {
        // out.println("Class from listener : " + className);
        // }
        // }
    }
    
    // sprint0
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            proccessRequest(req, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            proccessRequest(req, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String || value instanceof Character) {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append(toJson(entry.getKey().toString()));
                json.append(":");
                json.append(toJson(entry.getValue()));
                first = false;
            }
            return json.append("}").toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Object element : iterable) {
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
            for (int index = 0; index < Array.getLength(value); index++) {
                if (index > 0) {
                    json.append(",");
                }
                json.append(toJson(Array.get(value, index)));
            }
            return json.append("]").toString();
        }
        return toJson(value.toString());
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
