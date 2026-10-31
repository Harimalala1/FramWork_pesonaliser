package singleton;

import java.util.HashMap;
import java.util.Map;

public class ControllerSingleton {

    // Instance unique du Singleton
    private static final ControllerSingleton instance =
            new ControllerSingleton();

    // Stocke une instance par classe de contrôleur
    private final Map<String, Object> controllers = new HashMap<>();

    // Constructeur privé pour empêcher les instanciations externes
    private ControllerSingleton() {
    }

    // Récupérer l'instance unique du Singleton
    public static ControllerSingleton getInstance() {
        return instance;
    }

    // Récupérer ou créer une instance du contrôleur
    public synchronized Object getController(String className)
            throws Exception {

        Object controller = controllers.get(className);

        // Si le contrôleur n'existe pas encore, on le crée
        if (controller == null) {

            Class<?> controllerClass = Class.forName(className);

            controller = controllerClass
                    .getDeclaredConstructor()
                    .newInstance();

            controllers.put(className, controller);
        }

        return controller;
    }
}
