package browser.service;

/**
 * Interfaz que define los métodos que deben implementar las clases que manejan
 * la lógica de negocio de la aplicación.
 *
 * @param <R> Tipo de dato que se va a retornar.
 * @param <E> Tipo de dato que se va a manipular.
 * R = Retorno, E = Elemento
 */
public interface IService<R, E> {

    void agregarElemento(E elemento);

    void eliminarElemento(E elemento);

    void eliminarTodo();

    R obtenerTodo();
}