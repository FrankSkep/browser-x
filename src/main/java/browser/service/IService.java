package browser.service;

public interface IService<T, E, E2> {
    void eliminarTodo();
    T obtenerTodo();
    E obtenerElemento();
    void eliminarElemento(E2 elemento);
    void agregarElemento(E2 elemento);
}