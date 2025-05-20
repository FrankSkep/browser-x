package browser;

import browser.dao.DescargaDAO;
import browser.dao.FavoritoDAO;
import browser.dao.HistorialDAO;
import browser.service.Impl.DescargaServiceImpl;
import browser.service.Impl.FavoritoServiceImpl;
import browser.service.Impl.HistorialServiceImpl;
import browser.service.NavegacionManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicialización JavaFX

            // Instanciación de dependencias
            NavegacionManager navegacionManager = NavegacionManager.getInstance();
            HistorialServiceImpl historialService = new HistorialServiceImpl(new HistorialDAO());
            FavoritoServiceImpl favoritoService = new FavoritoServiceImpl(new FavoritoDAO());
            DescargaServiceImpl descargaService = new DescargaServiceImpl(new DescargaDAO());

            // Inyección de dependencias en BrowserX
            new BrowserX(
                    navegacionManager,
                    historialService,
                    favoritoService,
                    descargaService
            );
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(Platform::exit);
        }));
    }
}