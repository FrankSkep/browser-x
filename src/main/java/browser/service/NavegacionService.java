package browser.service;

import browser.dao.Impl.HistorialDAOImpl;
import browser.data_structure.LinkedList;
import browser.data_structure.Stack;
import browser.model.EntradaHistorial;
import browser.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar la navegación del navegador.
 */
public class NavegacionService implements IService<LinkedList<EntradaHistorial>,String,EntradaHistorial>{
    private LinkedList<String> historialSesion;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;
    private LinkedList<EntradaHistorial> historialCompleto;

    /**
     * Constructor que inicializa las estructuras de datos de navegación y carga el historial completo desde el DAO.
     */
    public NavegacionService() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();

        historialCompleto = new LinkedList<>();
        historialCompleto.addAll(HistorialDAOImpl.getInstance().getAll());
    }

    /**
     * Agrega una URL al historial temporal de la sesión.
     *
     * @param url La URL a agregar.
     */
    public void agregarUrlNavegacion(String url) {
        if (!historialSesion.isEmpty()) {
            pilaAtras.push(historialSesion.getLast());
        }
        historialSesion.add(url);
        pilaAdelante = new Stack<>(); // limpieza navegacion adelante
    }

    /**
     * Mueve una URL entre las pilas de navegación.
     *
     * @param origen La pila de origen.
     * @param destino La pila de destino.
     * @return La nueva URL después del movimiento, o null si no hay más URLs en la pila de origen.
     */
    private String mover(Stack<String> origen, Stack<String> destino) {
        if (!origen.isEmpty()) {
            String urlActual = historialSesion.getLast();
            destino.push(urlActual);
            String nuevaUrl = origen.pop();
            historialSesion.removeLast();
            historialSesion.add(nuevaUrl);
            return nuevaUrl;
        }
        return null;
    }

    /**
     * Retrocede en la navegación.
     *
     * @return La nueva URL después de retroceder, o null si no hay más URLs para retroceder.
     */
    public String retroceder() {
        return mover(pilaAtras, pilaAdelante);
    }

    /**
     * Avanza en la navegación.
     *
     * @return La nueva URL después de avanzar, o null si no hay más URLs para avanzar.
     */
    public String avanzar() {
        return mover(pilaAdelante, pilaAtras);
    }


    /**
     * Restablece la navegación, limpiando el historial de la sesión y las pilas de navegación.
     */
    private void restablecerNavegacion() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    /**
     * Guarda una URL en el historial de navegación completo.
     *
     * @param url La URL a guardar.
     */
    public void guardarEnHistorial(String url) {
        //
        EntradaHistorial entradaHistorial = new EntradaHistorial(url, ValidationUtil.dateFormat(LocalDateTime.now()));
        //
        historialCompleto.add(entradaHistorial);
        HistorialDAOImpl.getInstance().save(entradaHistorial);
    }



    /**
     * Verifica si se puede retroceder en la navegación.
     *
     * @return true si se puede retroceder, false en caso contrario.
     */
    public boolean puedeRetroceder() {
        return !pilaAtras.isEmpty();
    }

    /**
     * Verifica si se puede avanzar en la navegación.
     *
     * @return true si se puede avanzar, false en caso contrario.
     */
    public boolean puedeAvanzar() {
        return !pilaAdelante.isEmpty();
    }

    /**
     * Obtiene la pila de navegación atrás como una lista.
     *
     * @return Una lista con las URLs de la pila de navegación atrás.
     */
    public List<String> obtenerPilaAtrasList() {
        return pilaAtras.toList();
    }

    /**
     * Obtiene la pila de navegación adelante como una lista.
     *
     * @return Una lista con las URLs de la pila de navegación adelante.
     */
    public List<String> obtenerPilaAdelanteList() {
        return pilaAdelante.toList();
    }

    /**
     * Retrocede en la navegación hasta una URL específica.
     *
     * @param url La URL hasta la cual retroceder.
     */
    public void irAtrasHasta(String url) {
        while (!pilaAtras.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAdelante.push(historialSesion.removeLast());
            historialSesion.add(pilaAtras.pop());
        }
    }

    /**
     * Avanza en la navegación hasta una URL específica.
     *
     * @param url La URL hasta la cual avanzar.
     */
    public void irAdelanteHasta(String url) {
        while (!pilaAdelante.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAtras.push(historialSesion.removeLast());
            historialSesion.add(pilaAdelante.pop());
        }
    }
    //////////
    /**
     * Elimina todo el historial de navegación.
     */
    @Override
    public void eliminarTodo() {
        historialCompleto = new LinkedList<>();
        HistorialDAOImpl.getInstance().deleteAll();
        restablecerNavegacion();
    }

    /**
     * Obtiene el historial completo de navegación.
     *
     * @return Una lista enlazada con todas las entradas del historial.
     */
    @Override
    public LinkedList<EntradaHistorial> obtenerTodo() {
        return historialCompleto;
    }

    /**
     * Obtiene la URL actual de la navegación.
     *
     * @return La URL actual, o null si no hay URLs en el historial de la sesión.
     */
    @Override
    public String obtenerElemento() {
        if (historialSesion.isEmpty()) {
            return null;
        }
        return historialSesion.getLast();
    }
    /**
     * Elimina una entrada específica del historial de navegación.
     *
     * @param entradaHistorial La entrada del historial a eliminar.
     */
    @Override
    public void eliminarElemento(EntradaHistorial entradaHistorial) {
        historialCompleto.remove(entradaHistorial);
        HistorialDAOImpl.getInstance().delete(entradaHistorial);
    }

    @Override
    public void agregarElemento(EntradaHistorial elemento) {
        historialCompleto.add(elemento);
    }
}