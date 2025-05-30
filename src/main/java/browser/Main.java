package browser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicialización JavaFX
            AppConfig.createBrowserX(); // Crea la instancia de BrowserX con sus dependencias
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(Platform::exit);
        }));
    }
}