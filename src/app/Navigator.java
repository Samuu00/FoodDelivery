package app;

import controller.ShellController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Navigazione dell'app: una sola Scene.
 * - Schermate "auth" (splash, login, registrazione) occupano tutta la finestra.
 * - Le pagine dell'app vengono mostrate dentro la shell (header, footer, menu, carrello) con cronologia per "Indietro".
 */
public final class Navigator {

    /** Le pagine che implementano Page vengono aggiornate quando si torna indietro su di loro. */
    public interface Page { void onShow(); }

    private record Entry(String name, Node view, Object controller) {}

    private static final String APP_CSS = Navigator.class.getResource("/css/app.css").toExternalForm();
    private static final String DARK_CSS = Navigator.class.getResource("/css/dark.css").toExternalForm();

    private static Scene scene;
    private static ShellController shell;
    private static Entry current;
    private static final Deque<Entry> history = new ArrayDeque<>();
    private static boolean dark;

    private Navigator() {}

    public static void init(Stage stage) {
        scene = new Scene(new StackPane(), 1280, 800);
        scene.getStylesheets().add(APP_CSS);
        stage.setScene(scene);
    }

    public static Scene scene() { return scene; }

    public static ShellController shell() { return shell; }

    /** Mostra una schermata a tutta finestra (fuori dalla shell) e azzera la cronologia. */
    public static <T> T showFullScreen(String view) {
        FXMLLoader loader = loader(view);
        Parent root = load(loader);
        history.clear();
        current = null;
        shell = null;
        scene.setRoot(root);
        fadeIn(root);
        return loader.getController();
    }

    /** Dopo il login: carica la shell e apre la home. */
    public static void startApp() {
        FXMLLoader loader = loader("shell");
        Parent root = load(loader);
        shell = loader.getController();
        scene.setRoot(root);
        fadeIn(root);
        home();
    }

    /** Apre una pagina dentro la shell e la aggiunge alla cronologia. */
    public static <T> T go(String view) {
        FXMLLoader loader = loader(view);
        Node node = load(loader);
        if (current != null) history.push(current);
        current = new Entry(view, node, loader.getController());
        shell.show(node, view);
        return loader.getController();
    }

    public static void back() {
        if (history.isEmpty()) return;
        current = history.pop();
        shell.show(current.view(), current.name());
        if (current.controller() instanceof Page p) p.onShow();
    }

    public static <T> T home() {
        history.clear();
        current = null;
        return go("home");
    }

    /** Dopo un'azione conclusiva (es. pagamento) non si torna indietro al checkout. */
    public static void forgetHistory() {
        history.clear();
    }

    public static boolean canGoBack() { return !history.isEmpty(); }

    public static boolean isDark() { return dark; }

    public static void setDark(boolean on) {
        dark = on;
        if (on) { if (!scene.getStylesheets().contains(DARK_CSS)) scene.getStylesheets().add(DARK_CSS); }
        else scene.getStylesheets().remove(DARK_CSS);
    }

    /** Stessi fogli di stile della scena, per Alert e Dialog (che hanno una finestra propria). */
    public static java.util.List<String> stylesheets() { return scene.getStylesheets(); }

    private static FXMLLoader loader(String view) {
        return new FXMLLoader(Navigator.class.getResource("/view/" + view + ".fxml"));
    }

    private static <T> T load(FXMLLoader loader) {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile caricare " + loader.getLocation(), e);
        }
    }

    private static void fadeIn(Node n) {
        FadeTransition f = new FadeTransition(Duration.millis(350), n);
        f.setFromValue(0);
        f.setToValue(1);
        f.play();
    }
}
