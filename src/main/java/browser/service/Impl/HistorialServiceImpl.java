package browser.service.Impl;

import browser.dao.HistorialDAO;
import browser.data_structure.LinkedList;
import browser.model.EntradaHistorial;
import browser.service.IService;

public class HistorialServiceImpl implements IService<LinkedList<EntradaHistorial>, EntradaHistorial> {

    private final LinkedList<EntradaHistorial> historialList;
    private final HistorialDAO historialDAO;

    public HistorialServiceImpl(HistorialDAO historialDAO) {
        historialList = new LinkedList<>();
        this.historialDAO = historialDAO;
        historialList.addAll(historialDAO.obtenerTodo());
    }

    /**
     * Agrega una entrada al historial de navegación.
     *
     * @param pagina La entrada del historial a agregar.
     */
    @Override
    public void agregarElemento(EntradaHistorial pagina) {
        historialList.add(pagina);
        historialDAO.guardar(pagina);
    }

    /**
     * Elimina una entrada específica del historial de navegación.
     *
     * @param entradaHistorial La entrada del historial a eliminar.
     */
    @Override
    public void eliminarElemento(EntradaHistorial entradaHistorial) {
        historialList.remove(entradaHistorial);
        historialDAO.eliminar(entradaHistorial);
    }

    /**
     * Obtiene el historial completo de navegación.
     *
     * @return Una lista enlazada con todas las entradas del historial.
     */
    @Override
    public LinkedList<EntradaHistorial> obtenerTodo() {
        return historialList;
    }

    /**
     * Elimina todo el historial de navegación.
     */
    @Override
    public void eliminarTodo() {
        historialList.clear();
        historialDAO.eliminarTodo();
    }

}
