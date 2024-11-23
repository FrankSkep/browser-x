package browser.service;

import browser.dao.HistorialDAO;
import browser.data_structure.LinkedList;
import browser.data_structure.Stack;
import browser.model.EntradaHistorial;
import browser.utils.ValidationTools;

import java.time.LocalDateTime;

public class HistorialService {
    private LinkedList<String> historialSesion;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;
    private LinkedList<EntradaHistorial> historialTotal;

    public HistorialService() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();

        historialTotal = new LinkedList<>();
        historialTotal.addAll(HistorialDAO.obtenerTodo());
    }

    public void visitar(String url) {
        if (!historialSesion.isEmpty()) {
            pilaAtras.push(historialSesion.getLast());
        }
        historialSesion.add(url);

        // guardar en historial general y base de datos
        EntradaHistorial entradaHistorial = new EntradaHistorial(url, ValidationTools.dateFormat(LocalDateTime.now()));
        historialTotal.add(entradaHistorial);
        HistorialDAO.getInstance().guardar(entradaHistorial);

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

    public void eliminarHistorial() {
        historialTotal = new LinkedList<>();
        HistorialDAO.getInstance().eliminarTodo();
        restablecerNavegacion();
    }

    public void eliminarEntradaHistorial(EntradaHistorial entradaHistorial) {
        historialTotal.remove(entradaHistorial);
        HistorialDAO.getInstance().eliminar(entradaHistorial);
    }

    public LinkedList<EntradaHistorial> obtenerHistorial() {
        return historialTotal;
    }

    public boolean puedeRetroceder() {
        return !pilaAtras.isEmpty();
    }

    public boolean puedeAvanzar() {
        return !pilaAdelante.isEmpty();
    }
}