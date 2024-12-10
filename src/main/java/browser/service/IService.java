package browser.service;

/**
 * Interfaz que define los métodos que deben implementar las clases que manejan
 * la lógica de negocio de la aplicación.
 *
 * @param <T> Tipo de dato que se va a retornar.
 * @param <K> Tipo de dato que se va a manipular.
 */
public interface IService<T, K> {
    
    void agregarElemento(K elemento);

    void eliminarElemento(K elemento);

    void eliminarTodo();

    T obtenerTodo();
}