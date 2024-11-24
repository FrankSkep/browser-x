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
    private LinkedList<EntradaHistorial> historialCompleto;

    public HistorialService() {
        historialSesion = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();

        historialCompleto = new LinkedList<>();
        historialCompleto.addAll(HistorialDAO.getInstance().obtenerTodo());
    }

    public void agregarUrl(String url) {
        if (!historialSesion.isEmpty()) {
            pilaAtras.push(historialSesion.getLast());
        }
        historialSesion.add(url);
        pilaAdelante = new Stack<>(); // limpieza navegacion adelante

        // guardar en historial general y base de datos
        EntradaHistorial entradaHistorial = new EntradaHistorial(url, ValidationTools.dateFormat(LocalDateTime.now()));
        historialCompleto.add(entradaHistorial);
        HistorialDAO.getInstance().guardar(entradaHistorial);
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

    public void eliminar() {
        historialCompleto = new LinkedList<>();
        HistorialDAO.getInstance().eliminarTodo();
        restablecerNavegacion();
    }

    public void eliminarEntrada(EntradaHistorial entradaHistorial) {
        historialCompleto.remove(entradaHistorial);
        HistorialDAO.getInstance().eliminar(entradaHistorial);
    }

    public LinkedList<EntradaHistorial> obtener() {
        return historialCompleto;
    }

    public boolean puedeRetroceder() {
        return !pilaAtras.isEmpty();
    }

    public boolean puedeAvanzar() {
        return !pilaAdelante.isEmpty();
    }
}
