package service;

import java.util.Objects;

public class UtilMethode {
    private String url;
    private String methode;

    public UtilMethode(String url, String methode) {
        this.url = url;
        this.methode = methode == null ? null : methode.toUpperCase();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode == null ? null : methode.toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UtilMethode that = (UtilMethode) obj;
        return Objects.equals(url, that.url) && Objects.equals(methode, that.methode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methode);
    }
}