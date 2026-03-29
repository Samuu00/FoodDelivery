package Model;
/**
 * Model class representing a Restaurant.
 */
public class Restaurant {
    private int id;
    private String nome;
    private String descrizione;
    private String immagineURL;
    private String indirizzo;

    public Restaurant(int id, String nome, String descrizione, String immagineURL, String indirizzo) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.immagineURL = immagineURL;
        this.indirizzo = indirizzo;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public String getImmagineURL() { return immagineURL; }
    public String getIndirizzo() { return indirizzo; }
}
