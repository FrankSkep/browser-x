package browser.service;

import browser.dao.DescargaDAO;
import browser.data_structure.LinkedList;
import browser.model.Descarga;

public class DescargaService {

    private final LinkedList<Descarga> descargas;

    public DescargaService() {
        descargas = new LinkedList<>();
        descargas.addAll(DescargaDAO.getInstance().obtenerTodo());
    }

    public void guardarDescarga(Descarga descarga) {
        descargas.add(descarga);
        DescargaDAO.getInstance().guardar(descarga);
    }

    public void eliminarDescarga(Descarga descarga) {
        descargas.remove(descarga);
        DescargaDAO.getInstance().eliminar(descarga.getNombre());
    }

    public void eliminarTodo() {
        descargas.clear();
        DescargaDAO.getInstance().eliminarTodo();
    }

    public LinkedList<Descarga> obtenerTodo() {
        return descargas;
    }
}
