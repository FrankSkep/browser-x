package browser.service.Impl;

import browser.dao.HistorialDAO;
import browser.data_structure.LinkedList;
import browser.model.EntradaHistorial;
import browser.service.IService;
import browser.util.ValidationUtil;

import java.time.LocalDateTime;

public class HistorialServiceImpl implements IService<LinkedList<EntradaHistorial>, EntradaHistorial> {

    private final LinkedList<EntradaHistorial> historial;
    private final HistorialDAO historialDAO;

    public HistorialServiceImpl(HistorialDAO historialDAO) {
        historial = new LinkedList<>();
        this.historialDAO = historialDAO;
        historial.addAll(historialDAO.obtenerTodo());
    }

    /**
     * Agrega una entrada al historial de navegación.
     *
     * @param pagina La entrada del historial a agregar.
     */
    @Override
    public void agregarElemento(EntradaHistorial pagina) {
        EntradaHistorial entradaHistorial = new EntradaHistorial(pagina.getUrl(), ValidationUtil.dateFormat(LocalDateTime.now()));
        historial.add(entradaHistorial);
        historialDAO.guardar(entradaHistorial);
    }

    /**
     * Elimina una entrada específica del historial de navegación.
     *
     * @param entradaHistorial La entrada del historial a eliminar.
     */
    @Override
    public void eliminarElemento(EntradaHistorial entradaHistorial) {
        historial.remove(entradaHistorial);
        historialDAO.eliminar(entradaHistorial);
    }

    /**
     * Obtiene el historial completo de navegación.
     *
     * @return Una lista enlazada con todas las entradas del historial.
     */
    @Override
    public LinkedList<EntradaHistorial> obtenerTodo() {
        return historial;
    }

    /**
     * Elimina todo el historial de navegación.
     */
    @Override
    public void eliminarTodo() {
        historial.clear();
        historialDAO.eliminarTodo();
    }

}
