package browser.service;

public interface IService<T, K> {

    void agregarElemento(K elemento);

    void eliminarElemento(K elemento);

    void eliminarTodo();

    T obtenerTodo();
}