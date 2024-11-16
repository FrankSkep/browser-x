package browser.service;

import browser.data_structures.LinkedList;
import browser.data_structures.Stack;

public class HistorialService {
    private LinkedList<String> historialCompleto;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;

    public HistorialService() {
        historialCompleto = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    public void visitar(String url) {
        if (!historialCompleto.isEmpty()) {
            pilaAtras.push(historialCompleto.getLast());
        }
        historialCompleto.addLast(url);
        pilaAdelante = new Stack<>();
    }

    public String retroceder() {
        if (!pilaAtras.isEmpty()) {
            String urlActual = historialCompleto.getLast();
            pilaAdelante.push(urlActual);
            String urlAnterior = pilaAtras.pop();
            historialCompleto.removeLast();
            historialCompleto.addLast(urlAnterior);
            return urlAnterior;
        }
        return null;
    }

    public String avanzar() {
        if (!pilaAdelante.isEmpty()) {
            String urlActual = historialCompleto.getLast();
            pilaAtras.push(urlActual);
            String urlSiguiente = pilaAdelante.pop();
            historialCompleto.removeLast();
            historialCompleto.addLast(urlSiguiente);
            return urlSiguiente;
        }
        return null;
    }

    public String obtenerURLActual() {
        if (historialCompleto.isEmpty()) {
            return null;
        }
        return historialCompleto.getLast();
    }

    public void deleteHistory() {
        historialCompleto = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    public LinkedList<String> obtenerHistorialList() {
        return historialCompleto;
    }
}