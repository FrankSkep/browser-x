package browser.data_structure;

import java.util.ArrayList;
import java.util.List;

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

    public Stack() {
        top = null;
        size = 0;
    }

    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = top;
        top = newNode;
        size++;
    }

    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("La pila está vacía");
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    public T top() {
        if (isEmpty()) {
            throw new RuntimeException("La pila está vacía");
        }
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

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