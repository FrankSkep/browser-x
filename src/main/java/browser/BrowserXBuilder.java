package browser;

import browser.controller.DescargaUIController;
import browser.controller.FavoritoUIController;
import browser.controller.HistorialUIController;
import browser.service.NavegacionManager;

/**
 * Builder para la creación de instancias de {@link BrowserX}.
 * Permite configurar los controladores y el gestor de navegación antes de construir el navegador.
 */
public class BrowserXBuilder {
    private NavegacionManager navegacionManager;
    private HistorialUIController historialUIController;
    private FavoritoUIController favoritoUIController;
    private DescargaUIController descargaUIController;

    /**
     * Establece el gestor de navegación.
     *
     * @param navegacionManager instancia de {@link NavegacionManager}
     * @return esta instancia de {@code BrowserXBuilder}
     */
    public BrowserXBuilder setNavegacionManager(NavegacionManager navegacionManager) {
        this.navegacionManager = navegacionManager;
        return this;
    }

    /**
     * Establece el controlador de historial.
     *
     * @param historialUIController instancia de {@link HistorialUIController}
     * @return esta instancia de {@code BrowserXBuilder}
     */
    public BrowserXBuilder setHistorialController(HistorialUIController historialUIController) {
        this.historialUIController = historialUIController;
        return this;
    }

    /**
     * Establece el controlador de favoritos.
     *
     * @param favoritoUIController instancia de {@link FavoritoUIController}
     * @return esta instancia de {@code BrowserXBuilder}
     */
    public BrowserXBuilder setFavoritoController(FavoritoUIController favoritoUIController) {
        this.favoritoUIController = favoritoUIController;
        return this;
    }

    /**
     * Establece el controlador de descargas.
     *
     * @param descargaUIController instancia de {@link DescargaUIController}
     * @return esta instancia de {@code BrowserXBuilder}
     */
    public BrowserXBuilder setDescargaController(DescargaUIController descargaUIController) {
        this.descargaUIController = descargaUIController;
        return this;
    }

    /**
     * Construye una nueva instancia de {@link BrowserX} con los componentes configurados.
     *
     * @return una instancia de {@link BrowserX}
     */
    public BrowserX build() {
        return new BrowserX(
                navegacionManager,
                historialUIController,
                favoritoUIController,
                descargaUIController
        );
    }
}