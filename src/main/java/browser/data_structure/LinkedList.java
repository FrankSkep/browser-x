package browser.data_structure;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * La clase LinkedList representa una lista doblemente enlazada genérica.
 * @param <T> el tipo de elementos en la lista.
 */
public class LinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static class Node<T> {
        T data;
        Node<T> next;
        Node<T> prev;

        Node(T data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }

    /**
     * Constructor que inicializa una lista vacía.
     */
    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Agrega un elemento al final de la lista.
     * @param data el elemento a agregar.
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    /**
     * Agrega todos los elementos de otra lista al final de la lista.
     * @param otherList la lista cuyos elementos se agregarán.
     */
    public void addAll(LinkedList<? extends T> otherList) {
        for (T item : otherList) {
            add(item);
        }
    }

    /**
     * Elimina un elemento de la lista.
     * @param data el elemento a eliminar.
     * @return true si el elemento fue eliminado, false en caso contrario.
     */
    public boolean remove(T data) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(data)) {
                if (current == head) {
                    head = head.next;
                    if (head != null) {
                        head.prev = null;
                    }
                } else if (current == tail) {
                    tail = tail.prev;
                    tail.next = null;
                } else {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Elimina el último elemento de la lista.
     * @return el último elemento de la lista.
     * @throws RuntimeException si la lista está vacía.
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new RuntimeException("La lista está vacía");
        }
        T data = tail.data;
        if (head == tail) { // Si solo hay un elemento
            head = null;
            tail = null;
        } else {
            tail = tail.prev;
            tail.next = null;
        }
        size--;
        return data;
    }

    /**
     * Obtiene un elemento de la lista por su índice.
     * @param index el índice del elemento a obtener.
     * @return el elemento en el índice especificado.
     * @throws IndexOutOfBoundsException si el índice está fuera de rango.
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Retorna el último elemento de la lista.
     * @return el último elemento de la lista.
     * @throws RuntimeException si la lista está vacía.
     */
    public T getLast() {
        if (isEmpty()) {
            throw new RuntimeException("La lista está vacía");
        }
        return tail.data;
    }

    /**
     * Retorna el tamaño de la lista.
     * @return el tamaño de la lista.
     */
    public int size() {
        return size;
    }

    /**
     * Verifica si la lista está vacía.
     * @return true si la lista está vacía, false en caso contrario.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Elimina todos los elementos de la lista.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Retorna un iterador para recorrer la lista con un foreach.
     * @return un iterador para la lista.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    /**
     * Convierte la lista en un Stream.
     * @return un Stream de los elementos de la lista.
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}