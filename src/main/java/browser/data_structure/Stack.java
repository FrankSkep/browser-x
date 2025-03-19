package browser.data_structure;

import java.util.ArrayList;
import java.util.List;

/**
 * La clase Stack representa una pila genérica.
 * Utiliza una estructura de nodos enlazados para almacenar los elementos.
 *
 * @param <T> el tipo de elementos en la pila.
 */
public class Stack<T> {
    private Node<T> top;
    private int size;

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    /**
     * Constructor que inicializa una pila vacía.
     */
    public Stack() {
        top = null;
        size = 0;
    }

    /**
     * Inserta un elemento en la parte superior de la pila.
     *
     * @param data el elemento a insertar.
     */
    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * Elimina y retorna el elemento en la parte superior de la pila.
     *
     * @return el elemento en la parte superior de la pila.
     * @throws RuntimeException si la pila está vacía.
     */
    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("La pila está vacía");
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * Retorna el elemento en la parte superior de la pila sin eliminarlo.
     *
     * @return el elemento en la parte superior de la pila.
     * @throws RuntimeException si la pila está vacía.
     */
    public T top() {
        if (isEmpty()) {
            throw new RuntimeException("La pila está vacía");
        }
        return top.data;
    }

    /**
     * Verifica si la pila está vacía.
     *
     * @return true si la pila está vacía, false en caso contrario.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Retorna el tamaño de la pila.
     *
     * @return el número de elementos en la pila.
     */
    public int size() {
        return size;
    }

    /**
     * Convierte la pila en una lista.
     *
     * @return una lista con los elementos de la pila.
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = top;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}