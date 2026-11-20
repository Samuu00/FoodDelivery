package ui;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import java.util.Map;

/**
 * Icone a tratto (stile Lucide, griglia 24×24) disegnate con SVGPath: nitide a ogni dimensione,
 * colorabili da CSS (classe "icon", proprietà -fx-stroke). Sostituiscono emoji e PNG.
 */
public final class Icons {
    private Icons() {}

    private static final Map<String, String> PATHS = Map.ofEntries(
            Map.entry("search", "M4 11a7 7 0 1 0 14 0a7 7 0 1 0 -14 0 M20 20l-3.5-3.5"),
            Map.entry("pin", "M12 21s-7-6.2-7-11.5A7 7 0 0 1 19 9.5C19 14.8 12 21 12 21z M9.5 9.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0"),
            Map.entry("cart", "M3 4h2l2.4 11.2a1 1 0 0 0 1 .8h8.9a1 1 0 0 0 1-.8L20 8H6.2 M7.6 20a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0 M15.6 20a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0"),
            Map.entry("chevron-left", "M15 18l-6-6 6-6"),
            Map.entry("chevron-down", "M6 9l6 6 6-6"),
            Map.entry("plus", "M12 5v14M5 12h14"),
            Map.entry("minus", "M5 12h14"),
            Map.entry("check", "M20 6L9 17l-5-5"),
            Map.entry("lock", "M6 11h12a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2z M8 11V7a4 4 0 0 1 8 0v4"),
            Map.entry("download", "M12 3v12M7 10l5 5 5-5M5 21h14"),
            Map.entry("eye", "M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"),
            Map.entry("eye-off", "M3 3l18 18 M10.6 5.1A10 10 0 0 1 12 5c6.4 0 10 7 10 7a17 17 0 0 1-3.2 4.1 M6.6 6.6A17 17 0 0 0 2 12s3.6 7 10 7a9.7 9.7 0 0 0 5.4-1.6 M9.9 9.9a3 3 0 0 0 4.2 4.2"),
            Map.entry("sparkle", "M12 3v4M12 17v4M3 12h4M17 12h4M6 6l2.5 2.5M15.5 15.5L18 18M6 18l2.5-2.5M15.5 8.5L18 6"),
            Map.entry("alert", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0 M12 7v6M12 17h.01"),
            Map.entry("cash", "M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z M9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0"),
            Map.entry("x", "M18 6L6 18M6 6l12 12"),
            Map.entry("bag", "M6 7h12l1 13H5L6 7z M9 7V6a3 3 0 0 1 6 0v1")
    );

    /** @param extraClass classe CSS aggiuntiva per il colore, es. "icon-cta"; può essere null */
    public static StackPane of(String name, double size, String extraClass) {
        SVGPath p = new SVGPath();
        p.setContent(PATHS.get(name));
        p.getStyleClass().add("icon");
        if (extraClass != null) p.getStyleClass().add(extraClass);
        double k = size / 24.0;
        p.setScaleX(k);
        p.setScaleY(k);
        StackPane box = new StackPane(new Group(p));
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        box.setMouseTransparent(true);
        return box;
    }

    public static StackPane of(String name, double size) { return of(name, size, null); }
}
