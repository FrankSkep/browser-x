package browser;

import browser.controller.DescargaController;
import browser.controller.FavoritoController;
import browser.controller.HistorialController;
import browser.dao.DescargaDAO;
import browser.dao.FavoritoDAO;
import browser.dao.HistorialDAO;
import browser.service.Impl.DescargaServiceImpl;
import browser.service.Impl.FavoritoServiceImpl;
import browser.service.Impl.HistorialServiceImpl;
import browser.service.NavegacionManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import browser.util.Constants;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // Inicialización JavaFX

            // Instanciación de dependencias

            // DAOs
            HistorialDAO historialDAO = new HistorialDAO();
            FavoritoDAO favoritoDAO = new FavoritoDAO();
            DescargaDAO descargaDAO = new DescargaDAO();

            // Servicios
            NavegacionManager navegacionManager = NavegacionManager.getInstance();
            HistorialServiceImpl historialService = new HistorialServiceImpl(historialDAO);
            FavoritoServiceImpl favoritoService = new FavoritoServiceImpl(favoritoDAO);
            DescargaServiceImpl descargaService = new DescargaServiceImpl(descargaDAO);
            
            // Controladores
            HistorialController historialController = new HistorialController(historialService, Constants.ICONS_PATH);
            FavoritoController favoritoController = new FavoritoController(favoritoService);
            DescargaController descargaController = new DescargaController(descargaService);

            // Inyección de dependencias en BrowserX
            new BrowserX(
                    navegacionManager,
                    historialController,
                    favoritoController,
                    descargaController
            );
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(Platform::exit);
        }));
    }
}