package browser.service;

import browser.dao.HistorialDAO;
import browser.data_structure.LinkedList;
import browser.data_structure.Stack;
import browser.model.EntradaHistorial;
import browser.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class NavegacionService {
    private LinkedList<String> historialSesion;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;
    private LinkedList<EntradaHistorial> historialCompleto;

    public NavegacionService() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();

        historialCompleto = new LinkedList<>();
        historialCompleto.addAll(HistorialDAO.getInstance().obtenerTodo());
    }

    // agregar una URL al historial temporal de la sesión
    public void agregarUrlNavegacion(String url) {
        if (!historialSesion.isEmpty()) {
            pilaAtras.push(historialSesion.getLast());
        }
        historialSesion.add(url);
        pilaAdelante = new Stack<>(); // limpieza navegacion adelante
    }

    // mover entre las pilas de navegación
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

    // retroceder en la navegación
    public String retroceder() {
        return mover(pilaAtras, pilaAdelante);
    }

    // avanzar en la navegación
    public String avanzar() {
        return mover(pilaAdelante, pilaAtras);
    }

    // obtener la URL actual de la navegación
    public String obtenerURLActual() {
        if (historialSesion.isEmpty()) {
            return null;
        }
        return historialSesion.getLast();
    }

    // restablecer la navegación
    private void restablecerNavegacion() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    // guardar una URL en el historial de navegación
    public void guardarEnHistorial(String url) {
        EntradaHistorial entradaHistorial = new EntradaHistorial(url, ValidationUtil.dateFormat(LocalDateTime.now()));
        historialCompleto.add(entradaHistorial);
        HistorialDAO.getInstance().guardar(entradaHistorial);
    }

    // eliminar el historial de navegación
    public void eliminarHistorial() {
        historialCompleto = new LinkedList<>();
        HistorialDAO.getInstance().eliminarTodo();
        restablecerNavegacion();
    }

    // eliminar una entrada del historial
    public void eliminarEntradaHistorial(EntradaHistorial entradaHistorial) {
        historialCompleto.remove(entradaHistorial);
        HistorialDAO.getInstance().eliminar(entradaHistorial);
    }

    // obtener el historial como lista
    public LinkedList<EntradaHistorial> obtenerHistorial() {
        return historialCompleto;
    }

    // verificar si se puede retroceder
    public boolean puedeRetroceder() {
        return !pilaAtras.isEmpty();
    }

    // verificar si se puede avanzar
    public boolean puedeAvanzar() {
        return !pilaAdelante.isEmpty();
    }

    // obtener la pila de navegación atrás como lista
    public List<String> obtenerPilaAtrasList() {
        return pilaAtras.toList();
    }

    //
    public List<String> obtenerPilaAdelanteList() {
        return pilaAdelante.toList();
    }

    // ir atrás hasta una URL específica
    public void irAtrasHasta(String url) {
        while (!pilaAtras.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAdelante.push(historialSesion.removeLast());
            historialSesion.add(pilaAtras.pop());
        }
    }

    // ir adelante hasta una URL específica
    public void irAdelanteHasta(String url) {
        while (!pilaAdelante.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAtras.push(historialSesion.removeLast());
            historialSesion.add(pilaAdelante.pop());
        }
    }
}
