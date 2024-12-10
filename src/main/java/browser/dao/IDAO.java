package browser.dao;

import browser.data_structure.LinkedList;

/**
 * Interfaz para definir las operaciones básicas de un DAO.
 *
 * @param <T> El tipo de entidad que maneja el DAO.
 */
public interface IDAO<T> {

    /**
     * Guarda una entidad en la base de datos.
     *
     * @param entity La entidad a guardar.
     */
    void save(T entity);

    /**
     * Elimina una entidad de la base de datos.
     *
     * @param entity La entidad a eliminar.
     */
    void delete(T entity);

    /**
     * Elimina todas las entidades de la base de datos.
     */
    void deleteAll();

    /**
     * Obtiene todas las entidades de la base de datos.
     *
     * @return Una lista enlazada con todas las entidades.
     */
    LinkedList<T> getAll();
}