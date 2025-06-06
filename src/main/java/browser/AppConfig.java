package browser;

import browser.controller.*;
import browser.dao.*;
import browser.service.Impl.*;
import browser.service.NavegacionManager;
import browser.util.Constants;

public class AppConfig {
    public static BrowserX createBrowserX() {
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

        // Builder para construir BrowserX
        return new BrowserXBuilder()
                .setNavegacionManager(navegacionManager)
                .setHistorialController(historialController)
                .setFavoritoController(favoritoController)
                .setDescargaController(descargaController)
                .build();
    }
}