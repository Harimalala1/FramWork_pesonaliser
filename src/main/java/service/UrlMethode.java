package service;

public class UrlMethode {
    private String className;   // nom complet de la classe (package inclus)
    private String methodeName;
    private boolean apiRest;    // Sprint 6 : true si la methode a @ApiRest(json = true)

    public UrlMethode(String className, String methodeName, boolean apiRest) {
        this.className = className;
        this.methodeName = methodeName;
        this.apiRest = apiRest;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodeName() {
        return methodeName;
    }

    public boolean isApiRest() {
        return apiRest;
    }
}