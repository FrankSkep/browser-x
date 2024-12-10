package browser.service.Impl;

import browser.dao.Impl.HistorialDAOImpl;
import browser.data_structure.LinkedList;
import browser.model.EntradaHistorial;
import browser.service.IService;
import browser.util.ValidationUtil;

import java.time.LocalDateTime;

public class HistorialServiceImpl implements IService<LinkedList<EntradaHistorial>, EntradaHistorial> {

    private final LinkedList<EntradaHistorial> historial;

    public HistorialServiceImpl() {
        historial = new LinkedList<>();
        historial.addAll(HistorialDAOImpl.getInstance().getAll());
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
        HistorialDAOImpl.getInstance().save(entradaHistorial);
    }

    /**
     * Elimina una entrada específica del historial de navegación.
     *
     * @param entradaHistorial La entrada del historial a eliminar.
     */
    @Override
    public void eliminarElemento(EntradaHistorial entradaHistorial) {
        historial.remove(entradaHistorial);
        HistorialDAOImpl.getInstance().delete(entradaHistorial);
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
        HistorialDAOImpl.getInstance().deleteAll();
    }

}
