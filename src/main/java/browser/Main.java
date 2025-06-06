package browser;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::initApp);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(Platform::exit);
        }));
    }

    private static void initApp() {
        new JFXPanel(); // Inicializa JavaFX
        AppConfig.createBrowserX();
    }
}