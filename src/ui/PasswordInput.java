package ui;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

/** Campo password con "mostra/nascondi". Usabile in FXML: &lt;PasswordInput promptText="..."/&gt; */
public class PasswordInput extends StackPane {
    private final PasswordField hidden = new PasswordField();
    private final TextField shown = new TextField();
    private final Button eye = new Button();

    public PasswordInput() {
        getStyleClass().add("password-input");
        shown.textProperty().bindBidirectional(hidden.textProperty());
        shown.promptTextProperty().bind(hidden.promptTextProperty());
        shown.setVisible(false);
        eye.getStyleClass().setAll("eye");
        eye.setGraphic(Icons.of("eye", 20, "icon-muted"));
        eye.setFocusTraversable(false);
        eye.setTooltip(new Tooltip("Mostra o nascondi la password"));
        eye.setAccessibleText("Mostra password");
        eye.setOnAction(e -> toggle());
        StackPane.setAlignment(eye, Pos.CENTER_RIGHT);
        getChildren().addAll(hidden, shown, eye);
    }

    private void toggle() {
        boolean show = !shown.isVisible();
        shown.setVisible(show);
        hidden.setVisible(!show);
        eye.setGraphic(Icons.of(show ? "eye-off" : "eye", 20, "icon-muted"));
        TextField f = show ? shown : hidden;
        f.requestFocus();
        f.end();
    }

    public String getText() { return hidden.getText(); }
    public void setText(String t) { hidden.setText(t); }
    public StringProperty textProperty() { return hidden.textProperty(); }
    public String getPromptText() { return hidden.getPromptText(); }
    public void setPromptText(String t) { hidden.setPromptText(t); }

    public void setInvalid(boolean invalid) {
        for (TextField f : new TextField[]{hidden, shown}) {
            f.getStyleClass().remove("invalid");
            if (invalid) f.getStyleClass().add("invalid");
        }
    }
}
