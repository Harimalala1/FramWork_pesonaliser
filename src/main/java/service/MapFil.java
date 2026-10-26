package service;

public class MapFil {

    private String filename;
    private String type;
    private byte[] binaire;

    public MapFil() {
    }

    public MapFil(String filename, String type, byte[] binaire) {
        this.filename = filename;
        this.type = type;
        this.binaire = binaire;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getBinaire() {
        return binaire;
    }

    public void setBinaire(byte[] binaire) {
        this.binaire = binaire;
    }
}
