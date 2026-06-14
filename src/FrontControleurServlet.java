package src;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

Public class PushServerServlet extends HttpServlet { 
    @Override
    protected void doGet (HttpServletRequest req, HttpServletResponse resp)
    throws SecurityException, IOException {
        // Appeler la fonction processRequest
        processRequest(req, resp);
    }
    @Override
    protected void doPost (HttpServletRequest req, HttpServletResponse resp)
    throws SecurityException, IOException {
       // Appeler la fonction processRequest
        processRequest(req, resp);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        // Spécifier le type de contenu (optionnel mais recommandé)
        resp.setContentType("text/html;charset=UTF-8");

        // 4. Utilisation correcte de getWriter()
        PrintWriter out = resp.getWriter(); 
        
        // 5. Récupération correcte de l'URI de la requête
        String requestURI = req.getRequestURI();
        
        // 6. Affichage de l'URL/URI
        out.println("L'URI de la requête est : " + requestURI);
    }
}