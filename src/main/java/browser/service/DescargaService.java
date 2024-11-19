package browser.service;

import browser.dao.DescargaDAO;
import browser.model.Descarga;

import java.util.List;

public class DescargaService {

    private final List<Descarga> descargas;

    public DescargaService() {
        descargas = DescargaDAO.getInstance().obtenerTodo();
    }

    public void agregarDescarga(Descarga descarga) {
        descargas.add(descarga);
        DescargaDAO.getInstance().guardar(descarga);
    }

    public void eliminarDescarga(Descarga descarga) {
        descargas.remove(descarga);
        DescargaDAO.getInstance().eliminar(descarga.getNombre());
    }

    public void eliminarDescargas() {
        descargas.clear();
        DescargaDAO.getInstance().eliminarTodo();
    }

    public List<Descarga> obtenerDescargas() {
        return descargas;
    }
}
