package service;

public class HandlerResult {
    private Object value;
    private boolean webApi;

    public HandlerResult(Object value, boolean webApi) {
        this.value = value;
        this.webApi = webApi;
    }

    public Object getValue() {
        return value;
    }

    public boolean isWebApi() {
        return webApi;
    }
}