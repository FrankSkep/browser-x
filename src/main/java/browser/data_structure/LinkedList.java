package browser.data_structure;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    // agrega un elemento al final de la lista
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

    // agrega todos los elementos de otra lista al final de la lista
    public void addAll(LinkedList<? extends T> otherList) {
        for (T item : otherList) {
            add(item);
        }
    }

    // elimina un elemento de la lista
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

    // elimina el último elemento de la lista
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

    // obtener un elemento de la lista
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

    // retorna el último elemento
    public T getLast() {
        if (isEmpty()) {
            throw new RuntimeException("La lista está vacía");
        }
        return tail.data;
    }

    // retorna tamaño de la lista
    public int size() {
        return size;
    }

    // verifica si la lista esta vacía
    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    // retorna un iterador para recorrer la lista con un foreach
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

    // método para convertir la lista en un Stream
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

}
