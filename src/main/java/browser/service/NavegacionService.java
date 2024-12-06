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

    public void agregarUrlNavegacion(String url) {
        if (!historialSesion.isEmpty()) {
            pilaAtras.push(historialSesion.getLast());
        }
        historialSesion.add(url);
        pilaAdelante = new Stack<>(); // limpieza navegacion adelante
    }

    public String retroceder() {
        if (!pilaAtras.isEmpty()) {
            String urlActual = historialSesion.getLast();
            pilaAdelante.push(urlActual);
            String urlAnterior = pilaAtras.pop();
            historialSesion.removeLast();
            historialSesion.add(urlAnterior);
            return urlAnterior;
        }
        return null;
    }

    public String avanzar() {
        if (!pilaAdelante.isEmpty()) {
            String urlActual = historialSesion.getLast();
            pilaAtras.push(urlActual);
            String urlSiguiente = pilaAdelante.pop();
            historialSesion.removeLast();
            historialSesion.add(urlSiguiente);
            return urlSiguiente;
        }
        return null;
    }

    public String obtenerURLActual() {
        if (historialSesion.isEmpty()) {
            return null;
        }
        return historialSesion.getLast();
    }

    private void restablecerNavegacion() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    public void guardarEnHistorial(String url) {
        EntradaHistorial entradaHistorial = new EntradaHistorial(url, ValidationUtil.dateFormat(LocalDateTime.now()));
        historialCompleto.add(entradaHistorial);
        HistorialDAO.getInstance().guardar(entradaHistorial);
    }

    public void eliminarHistorial() {
        historialCompleto = new LinkedList<>();
        HistorialDAO.getInstance().eliminarTodo();
        restablecerNavegacion();
    }

    public void eliminarEntradaHistorial(EntradaHistorial entradaHistorial) {
        historialCompleto.remove(entradaHistorial);
        HistorialDAO.getInstance().eliminar(entradaHistorial);
    }

    public LinkedList<EntradaHistorial> obtenerHistorial() {
        return historialCompleto;
    }

    public boolean puedeRetroceder() {
        return !pilaAtras.isEmpty();
    }

    public boolean puedeAvanzar() {
        return !pilaAdelante.isEmpty();
    }

        public List<String> obtenerPilaAtras() {
        return pilaAtras.toList();
    }

    public List<String> obtenerPilaAdelante() {
        return pilaAdelante.toList();
    }

    public void irAtrasHasta(String url) {
        while (!pilaAtras.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAdelante.push(historialSesion.removeLast());
        }
        if (!historialSesion.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAdelante.push(historialSesion.removeLast());
        }
        historialSesion.add(url);
    }
    
    public void irAdelanteHasta(String url) {
        while (!pilaAdelante.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAtras.push(historialSesion.removeLast());
        }
        if (!historialSesion.isEmpty() && !historialSesion.getLast().equals(url)) {
            pilaAtras.push(historialSesion.removeLast());
        }
        historialSesion.add(url);
    }
}
