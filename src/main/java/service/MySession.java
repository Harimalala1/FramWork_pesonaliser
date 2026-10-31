package service;

import java.util.HashMap;
import java.util.Map;

public class MySession {

    private Map<String, Object> donnees = new HashMap<>();

    /**
     * Sprint 9B : ajouter ou modifier une donnée de session.
     */
    public void add(String nom, Object valeur) {
        donnees.put(nom, valeur);
    }

    /**
     * Sprint 9B : récupérer une donnée de session.
     */
    public Object get(String nom) {
        return donnees.get(nom);
    }

    /**
     * Sprint 9B : supprimer une donnée de session.
     */
    public void remove(String nom) {
        donnees.remove(nom);
    }

    /**
     * Sprint 9B : vérifier si une donnée existe.
     */
    public boolean contains(String nom) {
        return donnees.containsKey(nom);
    }

    /**
     * Sprint 9B : accéder aux données pour la synchronisation
     * avec HttpSession dans les prochaines étapes.
     */
    public Map<String, Object> getDonnees() {
        return donnees;
    }

    /**
     * Sprint 9B : remplacer les données de session.
     */
    public void setDonnees(Map<String, Object> donnees) {
        this.donnees = donnees != null ? donnees : new HashMap<>();
    }
}
