package browser.service;

import browser.dao.DescargasDAO;
import browser.model.Descarga;

import java.util.ArrayList;
import java.util.List;

public class DescargaService {

    private List<Descarga> descargas;

    public DescargaService() {
        descargas = new ArrayList<>();
    }

    public void agregarDescarga(Descarga descarga) {
        descargas.addLast(descarga);
        DescargasDAO.getInstance().guardar(descarga);
    }

    public void eliminarDescarga(Descarga descarga) {
        descargas.remove(descarga);
        DescargasDAO.getInstance().eliminar(descarga.getNombre());
    }

    public void eliminarDescargas() {
        descargas.clear();
        DescargasDAO.getInstance().eliminarTodo();
    }

    public List<Descarga> obtenerDescargas() {
        return descargas;
    }
}
