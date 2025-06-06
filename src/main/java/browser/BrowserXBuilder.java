package browser;

import browser.controller.DescargaController;
import browser.controller.FavoritoController;
import browser.controller.HistorialController;
import browser.service.NavegacionManager;

public class BrowserXBuilder {
    private NavegacionManager navegacionManager;
    private HistorialController historialController;
    private FavoritoController favoritoController;
    private DescargaController descargaController;

    public BrowserXBuilder setNavegacionManager(NavegacionManager navegacionManager) {
        this.navegacionManager = navegacionManager;
        return this;
    }

    public BrowserXBuilder setHistorialController(HistorialController historialController) {
        this.historialController = historialController;
        return this;
    }

    public BrowserXBuilder setFavoritoController(FavoritoController favoritoController) {
        this.favoritoController = favoritoController;
        return this;
    }

    public BrowserXBuilder setDescargaController(DescargaController descargaController) {
        this.descargaController = descargaController;
        return this;
    }

    public BrowserX build() {
        return new BrowserX(
                navegacionManager,
                historialController,
                favoritoController,
                descargaController
        );
    }
}
