import data_structures.LinkedList;
import data_structures.Stack;

public class HistorialNavegacion {
    private LinkedList<String> historialCompleto;
    private Stack<String> pilaAtras;
    private Stack<String> pilaAdelante;

    public HistorialNavegacion() {
        historialCompleto = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    // Método para visitar una nueva página
    public void visitar(String url) {
        if (!historialCompleto.isEmpty()) {
            pilaAtras.push(historialCompleto.getLast()); // agrega la página actual a la pila de retroceso
        }
        historialCompleto.addLast(url); // agrega la nueva URL al historial completo
        pilaAdelante = new Stack<>(); // Limpia la pila de avance
    }

    // Método para retroceder en el historial
    public String retroceder() {
        if (!pilaAtras.isEmpty()) {
            String urlActual = historialCompleto.getLast();
            pilaAdelante.push(urlActual); // Mueve la URL actual a la pila de avance
            String urlAnterior = pilaAtras.pop(); // Obtiene la URL anterior de la pila de retroceso
            historialCompleto.removeLast(); // Remueve la última URL del historial completo
            historialCompleto.addLast(urlAnterior); // Establece la URL anterior como actual
            return urlAnterior;
        }
        return null;
    }

    // avanzar en el historial
    public String avanzar() {
        if (!pilaAdelante.isEmpty()) {
            String urlActual = historialCompleto.getLast();
            pilaAtras.push(urlActual); // Mueve la URL actual a la pila de retroceso
            String urlSiguiente = pilaAdelante.pop(); // Obtiene la URL siguiente de la pila de avance
            historialCompleto.removeLast(); // Remueve la última URL del historial completo
            historialCompleto.addLast(urlSiguiente); // Establece la URL siguiente como actual
            return urlSiguiente;
        }
        return null;
    }

    // obtiene la URL actual
    public String obtenerURLActual() {
        if (historialCompleto.isEmpty()) {
            return null;
        }
        return historialCompleto.getLast();
    }

    // obtiene el historial completo como un string para mostrarlo en la interfaz
    public String obtenerHistorialCompleto() {
        StringBuilder historial = new StringBuilder();
        for (String url : historialCompleto) {
            historial.append(url).append("\n");
        }
        return historial.toString();
    }

    public void deleteHistory() {
        historialCompleto = new LinkedList<>();
        pilaAtras = new Stack<>();
        pilaAdelante = new Stack<>();
    }

    public void deleteLastHistory() {
        historialCompleto.removeLast();
    }
}