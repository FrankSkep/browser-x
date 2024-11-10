package dataStructures;

import java.util.Iterator;

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
    public void addLast(T data) {
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

    // retorna el primer elemento
    public T getFirst() {
        if (isEmpty()) {
            throw new RuntimeException("La lista está vacía");
        }
        return head.data;
    }

    // retorna el último elemento
    public T getLast() {
        if (isEmpty()) {
            throw new RuntimeException("La lista está vacía");
        }
        return tail.data;
    }

    // verifica si la lista esta vacía
    public boolean isEmpty() {
        return size == 0;
    }

    // retorna tamaño de la lista
    public int size() {
        return size;
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
}
