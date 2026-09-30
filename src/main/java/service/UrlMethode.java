package service;

public class UrlMethode {
    private String className;
    private String methodeName;
    private boolean webApi;

    public UrlMethode(String className, String methodeName, boolean webApi) {
        this.className = className;
        this.methodeName = methodeName;
        this.webApi = webApi;
    }

    /*
    public UrlMethode(String className, String methodeName) {
        this.className = className;
        this.methodeName = methodeName;
    }
    */

    public String getClassName() {
        return className;
    }

    public String getMethodeName() {
        return methodeName;
    }

    public boolean isWebApi() {
        return webApi;
    }
}
