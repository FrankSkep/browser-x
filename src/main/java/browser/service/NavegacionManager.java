package browser.service;

import browser.data_structure.LinkedList;
import browser.data_structure.Stack;

import java.util.List;

/**
 * Servicio para gestionar la navegación del navegador.
 */
public class NavegacionManager {
    private LinkedList<String> paginasVisitadas;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;

    /**
     * Constructor que inicializa las estructuras de datos de navegación y carga el historial completo desde el DAO.
     */
    public NavegacionManager() {
        paginasVisitadas = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    /**
     * Agrega una URL al historial temporal de la sesión.
     *
     * @param url La URL a agregar.
     */
    public void agregarUrlNavegacion(String url) {
        if (!paginasVisitadas.isEmpty()) {
            pilaAtras.push(paginasVisitadas.getLast());
        }
        paginasVisitadas.add(url);
        pilaAdelante = new Stack<>(); // limpieza navegacion adelante
    }

    /**
     * Mueve una URL entre las pilas de navegación.
     *
     * @param origen  La pila de origen.
     * @param destino La pila de destino.
     * @return La nueva URL después del movimiento, o null si no hay más URLs en la pila de origen.
     */
    private String mover(Stack<String> origen, Stack<String> destino) {
        if (!origen.isEmpty()) {
            String urlActual = paginasVisitadas.getLast();
            destino.push(urlActual);
            String nuevaUrl = origen.pop();
            paginasVisitadas.removeLast();
            paginasVisitadas.add(nuevaUrl);
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
    public void restablecerNavegacion() {
        paginasVisitadas = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
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
        while (!pilaAtras.isEmpty() && !paginasVisitadas.getLast().equals(url)) {
            pilaAdelante.push(paginasVisitadas.removeLast());
            paginasVisitadas.add(pilaAtras.pop());
        }
    }

    /**
     * Avanza en la navegación hasta una URL específica.
     *
     * @param url La URL hasta la cual avanzar.
     */
    public void irAdelanteHasta(String url) {
        while (!pilaAdelante.isEmpty() && !paginasVisitadas.getLast().equals(url)) {
            pilaAtras.push(paginasVisitadas.removeLast());
            paginasVisitadas.add(pilaAdelante.pop());
        }
    }

    /**
     * Obtiene la URL actual de la navegación.
     *
     * @return La URL actual, o null si no hay URLs en el historial de la sesión.
     */
    public String obtenerUrlActual() {
        if (paginasVisitadas.isEmpty()) {
            return null;
        }
        return paginasVisitadas.getLast();
    }
}