package browser;

import browser.controller.*;
import browser.dao.*;
import browser.service.Impl.*;
import browser.service.NavegacionManager;
import browser.util.Constants;

/**
 * Clase de configuración de la aplicación.
 * Se encarga de instanciar y ensamblar los componentes principales del navegador.
 */
public class AppConfig {

    /**
     * Crea e inicializa una instancia de {@link BrowserX} con sus dependencias.
     *
     * @return una instancia completamente configurada de {@link BrowserX}
     */
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
        HistorialUIController historialUIController = new HistorialUIController(historialService, Constants.ICONS_PATH);
        FavoritoUIController favoritoUIController = new FavoritoUIController(favoritoService);
        DescargaUIController descargaUIController = new DescargaUIController(descargaService);

        // Builder para construir BrowserX
        return new BrowserXBuilder()
                .setNavegacionManager(navegacionManager)
                .setHistorialController(historialUIController)
                .setFavoritoController(favoritoUIController)
                .setDescargaController(descargaUIController)
                .build();
    }
}