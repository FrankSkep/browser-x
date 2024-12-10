package browser.service;

public interface IService<T, K, V> {
    void eliminarTodo();

    T obtenerTodo();

    K obtenerElemento();

    void eliminarElemento(V elemento);

    void agregarElemento(V elemento);
}