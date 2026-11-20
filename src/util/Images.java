package util;

import javafx.scene.image.Image;

import java.io.InputStream;

/** Carica immagini dal classpath (/img/...). Prima venivano lette da "src/Image": si rompeva fuori dall'IDE. */
public final class Images {
    private Images() {}

    /** @return l'immagine, o null se il nome è vuoto o il file non esiste */
    public static Image load(String name, double size) {
        if (name == null || name.isBlank()) return null;
        InputStream in = Images.class.getResourceAsStream("/img/" + name);
        return in == null ? null : new Image(in, size, size, true, true);
    }
}
