package browser.service;

import browser.data_structure.LinkedList;
import browser.data_structure.Stack;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;



import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar la navegación del navegador.
 * Implementa el patrón Singleton usando el idiom de "Initialization-on-demand holder".
 */
public class NavegacionManager {
    private LinkedList<String> paginasVisitadas;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;
    private final Map<String, String> cacheTitulos;

    /**
     * Constructor privado para evitar la creación de instancias.
     */
    private NavegacionManager() {
        paginasVisitadas = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
        cacheTitulos = new ConcurrentHashMap<>();
    }

    /**
     * Clase interna estática responsable de mantener la instancia única de NavegacionManager.
     * Esta instancia se crea solo cuando se llama por primera vez a getInstance().
     */
    private static class Holder {
        private static final NavegacionManager INSTANCE = new NavegacionManager();
    }

    /**
     * Obtiene la instancia única de NavegacionManager.
     * Utiliza el patrón Singleton con inicialización perezosa y es seguro para hilos.
     *
     * @return la instancia única de NavegacionManager.
     */
    public static NavegacionManager getInstance() {
        return Holder.INSTANCE;
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
    /**
     * Obtiene el título de la página web a partir de su URL.
     *
     * @param url La URL de la página web.
     * @return El título de la página, o un mensaje de error si no se puede obtener.
     */
    public String obtenerTituloPorUrl(String url, Runnable onTitleLoaded) {
        if (cacheTitulos.containsKey(url)) {
            return cacheTitulos.get(url);
        } else {
            // Lanzar carga en segundo plano
            new Thread(() -> {
                try {
                    Document document = Jsoup.connect(url).get();
                    String titulo = document.title();
                    cacheTitulos.put(url, titulo);
                } catch (Exception e) {
                    cacheTitulos.put(url, "Título no disponible");
                }
                if (onTitleLoaded != null) {
                    onTitleLoaded.run(); // Notificar para refrescar la UI
                }
            }).start();
            return "Cargando título...";
        }
    }
}