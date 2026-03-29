package Model;

/**
 * Model class representing a Menu Item.
 */
public class MenuItem {
    private int menuId;
    private String nomePiatto;
    private double prezzo;
    private String immagineURL;
    private String categoria;

    public MenuItem(int menuId, String nomePiatto, double prezzo, String immagineURL, String categoria) {
        this.menuId = menuId;
        this.nomePiatto = nomePiatto;
        this.prezzo = prezzo;
        this.immagineURL = immagineURL;
        this.categoria = categoria;
    }

    public int getMenuId() { return menuId; }
    public String getNomePiatto() { return nomePiatto; }
    public double getPrezzo() { return prezzo; }
    public String getImmagineURL() { return immagineURL; }
    public String getCategoria() { return categoria; }
}
